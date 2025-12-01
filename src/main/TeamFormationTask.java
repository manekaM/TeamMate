package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TeamFormationTask implements Runnable {
    private final List<Participant> participants;
    private final int teamSize;
    private final List<Team> resultTeams;

    public TeamFormationTask(List<Participant> participants, int teamSize, List<Team> resultTeams) {
        this.participants = new ArrayList<>(participants);
        this.teamSize = teamSize;
        this.resultTeams = resultTeams;
    }

    @Override
    public void run() {
        System.out.println("Thread " + Thread.currentThread().getName() + " is forming teams...");
        List<Team> teams = formBalancedTeams(participants, teamSize);
        synchronized (resultTeams) {
            resultTeams.addAll(teams);
        }
    }

    // Simple but effective balancing algorithm
    private List<Team> formBalancedTeams(List<Participant> all, int size) {
        Collections.shuffle(all, new Random());
        List<Participant> pool = new ArrayList<>(all);
        List<Team> teams = new ArrayList<>();

        int teamNum = 1;
        while (pool.size() >= size) {
            Team team = new Team(teamNum++);
            // Sort by rarity to pick diverse first
            pool.sort((a, b) -> {
                int countA = (int) pool.stream().filter(p -> p.getPreferredGame().equals(a.getPreferredGame())).count();
                int countB = (int) pool.stream().filter(p -> p.getPreferredGame().equals(b.getPreferredGame())).count();
                return Integer.compare(countA, countB);
            });

            // Try to get 1 Leader
            pool.stream()
                    .filter(p -> p.getPersonalityType() == PersonalityType.LEADER && team.getSize() < size)
                    .findFirst()
                    .ifPresent(p -> { team.addMember(p); pool.remove(p); });

            // Fill the rest with variety
            while (team.getSize() < size && !pool.isEmpty()) {
                Participant best = null;
                int bestScore = -1;

                for (Participant candidate : pool) {
                    int score = 0;
                    boolean sameGame = team.getMembers().stream()
                            .anyMatch(m -> m.getPreferredGame().equals(candidate.getPreferredGame()));
                    boolean sameRole = team.getMembers().stream()
                            .anyMatch(m -> m.getPreferredRole().equals(candidate.getPreferredRole()));

                    if (!sameGame) score += 3;
                    if (!sameRole) score += 2;
                    if (candidate.getPersonalityType() == PersonalityType.THINKER) score += 1;

                    if (score > bestScore) {
                        bestScore = score;
                        best = candidate;
                    }
                }
                if (best != null) {
                    team.addMember(best);
                    pool.remove(best);
                } else {
                    break;
                }
            }
            if (team.getSize() >= size * 0.8) { // accept nearly full teams
                teams.add(team);
            }
        }
        return teams;
    }
}

