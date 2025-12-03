package main;

import java.util.*;

public class TeamFormationTask implements Runnable {
    // Variables
    private final List<Participant> participants;
    private final int teamSize;
    private final List<List<Team>> results;
    private final int maxTeams;

    // Constructors
    public TeamFormationTask(List<Participant> participants, int teamSize, List<List<Team>> results, int maxTeams) {
        this.participants = new ArrayList<>(participants);
        this.teamSize = teamSize;
        this.results = results;
        this.maxTeams = maxTeams;
    }

    @Override
    public void run() {
        List<Team> teams = formBalancedTeams();
        synchronized (results) {
            results.add(teams);
        }
    }

    //Main method
    private List<Team> formBalancedTeams() {
        Collections.shuffle(participants, new Random());
        List<Participant> availablePeople = new ArrayList<>(participants);
        List<Team> teams = new ArrayList<>();

        int teamNumber = 1;
        while (!availablePeople.isEmpty() && (maxTeams == 0 || teams.size() < maxTeams)) {
            if (availablePeople.size() < teamSize && !teams.isEmpty()) {
                break;
            }
            Team newTeam = new Team(teamNumber);
            teamNumber++;
            int targetSize = Math.min(teamSize, availablePeople.size());
            buildTeamWithRules(newTeam, availablePeople, targetSize);
            if (newTeam.getSize() > 0) {
                teams.add(newTeam);
            }
        }

        return teams;
    }

    private void buildTeamWithRules(Team team, List<Participant> availablePeople, int targetSize) {

        //Add exactly 1 Leader
        addPeopleByPersonality(team, availablePeople, PersonalityType.LEADER, 1);

        //Add 1-2 Thinkers
        int thinkersNeeded = (targetSize > 3) ? 2 : 1;
        addPeopleByPersonality(team, availablePeople, PersonalityType.THINKER, thinkersNeeded);

        //Fill remaining spots with best matching people
        while (team.getSize() < targetSize && !availablePeople.isEmpty()) {

            Participant bestPerson = findBestPersonToAdd(team, availablePeople, targetSize);

            if (bestPerson != null) {
                team.addMember(bestPerson);
                availablePeople.remove(bestPerson);
            } else {
                break;
            }
        }
    }

    private void addPeopleByPersonality(Team team, List<Participant> availablePeople,
                                        PersonalityType wantedType, int howMany) {
        int addedCount = 0;
        Iterator<Participant> iterator = availablePeople.iterator();
        while (iterator.hasNext() && addedCount < howMany) {
            Participant person = iterator.next();

            if (person.getPersonalityType() == wantedType) {
                team.addMember(person);
                iterator.remove();  // Remove from available list
                addedCount++;
            }
        }
    }

    private Participant findBestPersonToAdd(Team team, List<Participant> availablePeople, int targetSize) {
        Participant bestPerson = null;
        double bestScore = -999999;
        for (Participant candidate : availablePeople) {
            double score = scoreCandidate(team, candidate, targetSize);
            if (score > bestScore) {
                bestScore = score;
                bestPerson = candidate;
            }
        }

        return bestPerson;
    }

    private double scoreCandidate(Team team, Participant candidate, int targetSize) {
        double score = 0;
        int sameGameCount = countPeopleWithSameGame(team, candidate.getPreferredGame());

        if (sameGameCount >= 2) {
            return -999999;
        }

        if (sameGameCount == 0) {
            score += 20;
        } else if (sameGameCount == 1) {
            score += 10;
        }

        //Role Diversity
        boolean teamAlreadyHasThisRole = doesTeamHaveRole(team, candidate.getPreferredRole());

        if (!teamAlreadyHasThisRole) {
            score += 15;
        }

        if (targetSize > 5 && team.getSize() >= 3) {
            int uniqueRoles = countUniqueRoles(team);
            if (uniqueRoles < 3 && !teamAlreadyHasThisRole) {
                score += 10;
            }
        }

        //Personality Mix
        score += scorePersonalityFit(team, candidate, targetSize);

        //Skill Balance
        double skillImbalance = calculateSkillImbalance(team, candidate);
        score -= skillImbalance * 2;

        // Random Fairness
        score += Math.random() * 3;

        return score;
    }

    private int countPeopleWithSameGame(Team team, String game) {
        int count = 0;
        for (Participant member : team.getMembers()) {
            if (member.getPreferredGame().equalsIgnoreCase(game)) {
                count++;
            }
        }
        return count;
    }

    private boolean doesTeamHaveRole(Team team, Role role) {
        for (Participant member : team.getMembers()) {
            if (member.getPreferredRole() == role) {
                return true;
            }
        }
        return false;
    }

    private int countUniqueRoles(Team team) {
        Set<Role> uniqueRoles = new HashSet<>();
        for (Participant member : team.getMembers()) {
            uniqueRoles.add(member.getPreferredRole());
        }
        return uniqueRoles.size();
    }

    private double scorePersonalityFit(Team team, Participant candidate, int targetSize) {
        int leaderCount = 0;
        int thinkerCount = 0;
        int balancedCount = 0;

        for (Participant member : team.getMembers()) {
            PersonalityType type = member.getPersonalityType();
            if (type == PersonalityType.LEADER) leaderCount++;
            else if (type == PersonalityType.THINKER) thinkerCount++;
            else if (type == PersonalityType.BALANCED) balancedCount++;
        }

        PersonalityType candidateType = candidate.getPersonalityType();

        if (candidateType == PersonalityType.LEADER) {
            if (leaderCount == 0) return 12;
            else return -20;
        }
        else if (candidateType == PersonalityType.THINKER) {
            if (thinkerCount == 0) return 10;
            else if (thinkerCount == 1 && targetSize > 3) return 8;
            else return -10;
        }
        else {
            if (leaderCount > 0 && thinkerCount > 0) return 6;
            else if (team.getSize() >= targetSize - 2) return 4;
            else return 2;
        }
    }

    private double calculateSkillImbalance(Team team, Participant candidate) {
        if (team.getMembers().isEmpty()) {
            return 0;
        }

        List<Integer> allSkills = new ArrayList<>();
        for (Participant member : team.getMembers()) {
            allSkills.add(member.getSkillLevel());
        }
        allSkills.add(candidate.getSkillLevel());

        double sum = 0;
        for (int skill : allSkills) {
            sum += skill;
        }
        double average = sum / allSkills.size();
        double sumOfSquares = 0;
        for (int skill : allSkills) {
            double difference = skill - average;
            sumOfSquares += difference * difference;
        }
        double variance = sumOfSquares / allSkills.size();

        return Math.sqrt(variance);
    }
}