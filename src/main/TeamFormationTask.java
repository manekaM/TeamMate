package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TeamFormationTask implements Runnable {
    private final List<Participant> participants;
    private final int teamSize;
    private final List<List<Team>> allCandidates;  // Shared list to collect results

    public TeamFormationTask(List<Participant> participants, int teamSize, List<List<Team>> allCandidates) {
        this.participants = new ArrayList<>(participants);
        this.teamSize = teamSize;
        this.allCandidates = allCandidates;
    }

    @Override
    public void run() {
        System.out.println("Thread " + Thread.currentThread().getName() + " starting team formation...");
        List<Team> teams = formBalancedTeams(new ArrayList<>(participants), teamSize);

        synchronized (allCandidates) {
            allCandidates.add(teams);
        }
    }

    private List<Team> formBalancedTeams(List<Participant> source, int size) {
        Collections.shuffle(source, new Random());
        List<Participant> pool = new ArrayList<>(source);
        List<Team> teams = new ArrayList<>();
        int teamNum = 1;

        while (pool.size() >= size) {
            Team team = new Team(teamNum++);

            // Prefer one Leader per team if possible
            pool.stream()
                    .filter(p -> p.getPersonalityType() == PersonalityType.LEADER)
                    .findFirst()
                    .ifPresent(p -> {
                        team.addMember(p);
                        pool.remove(p);
                    });

            // Fill team with diversity
            while (team.getSize() < size && !pool.isEmpty()) {
                Participant best = null;
                int bestScore = -1;

                for (Participant p : pool) {
                    int score = 0;
                    boolean hasSameGame = team.getMembers().stream()
                            .anyMatch(m -> m.getPreferredGame().equals(p.getPreferredGame()));
                    boolean hasSameRole = team.getMembers().stream()
                            .anyMatch(m -> m.getPreferredRole().equals(p.getPreferredRole()));

                    if (!hasSameGame) score += 4;
                    if (!hasSameRole) score += 3;
                    if (p.getPersonalityType() == PersonalityType.THINKER) score += 1;
                    if (p.getSkillLevel() >= 7) score += 1; // slight bias toward skilled players

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

            if (team.getSize() >= size * 0.8) {
                teams.add(team);
            }
        }

        // Add leftover participants to last team if reasonable
        if (!pool.isEmpty() && !teams.isEmpty()) {
            Team last = teams.get(teams.size() - 1);
            while (last.getSize() < size && !pool.isEmpty()) {
                last.addMember(pool.remove(0));
            }
        }

        return teams;
    }
}