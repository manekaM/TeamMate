package main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TeamBuilder {

    public static List<Team> buildTeams(List<Participant> participants, int teamSize) {
        if (teamSize <= 0) teamSize = 5;
        if (participants.isEmpty()) return new ArrayList<>();

        int numThreads = 4;
        List<List<Team>> candidates = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(new TeamFormationTask(participants, teamSize, candidates));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Choose best candidate
        List<Team> best = null;
        double bestScore = Double.MAX_VALUE;

        for (List<Team> candidate : candidates) {
            if (candidate.isEmpty()) continue;

            double variance = candidate.stream()
                    .mapToDouble(t -> {
                        double avg = t.getAverageSkill();
                        return t.getMembers().stream()
                                .mapToDouble(p -> Math.pow(p.getSkillLevel() - avg, 2))
                                .sum();
                    }).sum() / candidate.size();

            int totalUsed = candidate.stream().mapToInt(Team::getSize).sum();
            double score = variance - (totalUsed * 0.0001);

            if (best == null || score < bestScore) {
                bestScore = score;
                best = candidate;
            }
        }

        if (best == null) best = new ArrayList<>();

        // Renumber teams
        for (int i = 0; i < best.size(); i++) {
            best.get(i).setTeamNumber(i + 1);
        }

        return best;
    }
}