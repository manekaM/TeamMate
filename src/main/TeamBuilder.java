package main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TeamBuilder {

    public static List<Team> buildTeams(List<Participant> participants, int teamSize) {
        int numAttempts = 4;
        List<List<Team>> allCandidates = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(numAttempts);

        for (int i = 0; i < numAttempts; i++) {
            executor.submit(new TeamFormationTask(participants, teamSize, allCandidates));
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Pick the best set (most teams + lowest skill variance)
        List<Team> best = null;
        double bestScore = Double.MAX_VALUE;

        for (List<Team> candidate : allCandidates) {
            if (candidate.isEmpty()) continue;

            double totalSkillVariance = candidate.stream()
                    .mapToDouble(t -> {
                        double avg = t.getAverageSkill();
                        return t.getMembers().stream()
                                .mapToDouble(p -> Math.pow(p.getSkillLevel() - avg, 2))
                                .average().orElse(0);
                    })
                    .sum();

            double score = totalSkillVariance / candidate.size(); // average variance per team

            // Bonus: prefer solutions that use more participants
            int used = candidate.stream().mapToInt(Team::getSize).sum();
            double finalScore = score - (used * 0.001); // small tie-breaker

            if (best == null || finalScore < bestScore) {
                bestScore = finalScore;
                best = candidate;
            }
        }

        if (best == null) {
            best = new ArrayList<>();
        }

        // Renumber teams properly
        for (int i = 0; i < best.size(); i++) {
            best.get(i).setTeamNumber(i + 1);
        }

        return best;
    }
}