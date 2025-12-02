package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TeamFormationTask implements Runnable {
    private final List<Participant> participants;
    private final int teamSize;
    private final List<List<Team>> results;

    public TeamFormationTask(List<Participant> participants, int teamSize, List<List<Team>> results) {
        this.participants = new ArrayList<>(participants);
        this.teamSize = teamSize;
        this.results = results;
    }

    @Override
    public void run() {
        List<Team> teams = formBalancedTeams();
        synchronized (results) {
            results.add(teams);
        }
    }

    private List<Team> formBalancedTeams() {
        Collections.shuffle(participants, new Random());
        List<Participant> pool = new ArrayList<>(participants);
        List<Team> teams = new ArrayList<>();
        int teamNum = 1;

        while (pool.size() >= teamSize || (!pool.isEmpty() && teams.isEmpty())) {
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
                    boolean sameGame = team.getMembers().stream().anyMatch(m -> m.getPreferredGame().equals(p.getPreferredGame()));
                    boolean sameRole = team.getMembers().stream().anyMatch(m -> m.getPreferredRole().equals(p.getPreferredRole()));

                    if (!sameGame) score += 4;
                    if (!sameRole) score += 3;
                    if (p.getPersonalityType() == PersonalityType.THINKER) score += 1;

                    if (score > bestScore) {
                        bestScore = score;
                        best = p;
                    }
                }
                if (best != null) {
                    team.addMember(best);
                    pool.remove(best);
                }
            }
            teams.add(team);
        }

        // Add any stragglers to last team
        if (!pool.isEmpty() && !teams.isEmpty()) {
            teams.get(teams.size() - 1).getMembers().addAll(pool);
        }

        return teams;
    }
}