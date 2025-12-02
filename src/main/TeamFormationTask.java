package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

//Forming teams by parallel threads
public class TeamFormationTask implements Runnable {
    private final List<Participant> participants;
    private final int teamSize;
    private final List<List<Team>> results;
    private final int maxTeams; // 0 means create as many as possible

    //Constructor for creating as many teams as possible
    public TeamFormationTask(List<Participant> participants, int teamSize, List<List<Team>> results) {
        this(participants, teamSize, results, 0);
    }

    //Constructor with specific number of teams
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

    //Forms balanced teams
    private List<Team> formBalancedTeams() {
        Collections.shuffle(participants, new Random());
        List<Participant> pool = new ArrayList<>(participants);
        List<Team> teams = new ArrayList<>();
        int teamNum = 1;

        // If maxTeams is set, only create that many teams
        while (!pool.isEmpty() && (maxTeams == 0 || teams.size() < maxTeams)) {
            // Stop if we don't have enough participants for another team
            if (pool.size() < teamSize && !teams.isEmpty()) {
                break;
            }

            Team team = new Team(teamNum++);
            int target = Math.min(teamSize, pool.size());

            // Try to add a leader
            pool.removeIf(p -> {
                if (p.getPersonalityType() == PersonalityType.LEADER && team.getSize() < target) {
                    team.addMember(p);
                    return true;
                }
                return false;
            });

            // Fill rest with diversity
            while (team.getSize() < target && !pool.isEmpty()) {
                Participant best = null;
                int bestScore = -1;

                for (Participant p : pool) {
                    int score = 0;
                    boolean sameGame = team.getMembers().stream()
                            .anyMatch(m -> m.getPreferredGame().equals(p.getPreferredGame()));
                    boolean sameRole = team.getMembers().stream()
                            .anyMatch(m -> m.getPreferredRole().equals(p.getPreferredRole()));

                    if (!sameGame) score += 4;
                    if (!sameRole) score += 3;
                    if (p.getPersonalityType() == PersonalityType.THINKER) score += 1;

                    if (score > bestScore) {
                        bestScore = score;
                        best = p;
                    }
                }

                team.addMember(best);
                pool.remove(best);
            }

            // Only add the team if it has members
            if (team.getSize() > 0) {
                teams.add(team);
            }
        }

        return teams;
    }
}