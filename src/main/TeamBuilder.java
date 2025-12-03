package main;

import java.util.*;
import java.util.concurrent.*;

public class TeamBuilder {
    private static final Logger logger = Logger.getInstance();

    //Creates as many teams as possible with given team size
    public static List<Team> buildTeams(List<Participant> participants, int teamSize) {
        // Validation
        if (teamSize <= 0) teamSize = 5;
        if (participants.isEmpty()) return new ArrayList<>();

        logger.info("Starting team formation: " + participants.size() + " participants, target team size = " + teamSize);

        int numberOfAttempts = 4;
        List<List<Team>> allAttempts = new ArrayList<>();

        // Create a thread pool to run attempts in parallel
        ExecutorService threadPool = Executors.newFixedThreadPool(numberOfAttempts);

        for (int i = 0; i < numberOfAttempts; i++) {
            threadPool.submit(new TeamFormationTask(participants, teamSize, allAttempts, 0));
        }
        threadPool.shutdown();

        try {
            threadPool.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("Thread pool interrupted during team formation", e);
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Pick the best result from all attempts
        List<Team> bestTeams = pickBestTeamSet(allAttempts);

        for (int i = 0; i < bestTeams.size(); i++) {
            bestTeams.get(i).setTeamNumber(i + 1);
        }

        logger.info("Team formation completed: " + bestTeams.size() + " teams created");
        logDetailedStatistics(bestTeams);
        return bestTeams;
    }

    //Creates specific number of teams with given team size
    public static List<Team> buildSpecificNumberOfTeams(List<Participant> participants,
                                                        int teamSize,
                                                        int numberOfTeams) {
        // Validation
        if (teamSize <= 0) teamSize = 5;
        if (numberOfTeams <= 0) numberOfTeams = 1;
        if (participants.isEmpty()) return new ArrayList<>();

        logger.info("Starting team formation: " + participants.size() + " participants, " +
                numberOfTeams + " teams of size " + teamSize);

        // Check if we have enough people
        int totalNeeded = numberOfTeams * teamSize;
        if (totalNeeded > participants.size()) {
            logger.info("Warning: Not enough participants for requested teams.");
        }

        int numberOfAttempts = 4;
        List<List<Team>> allAttempts = new ArrayList<>();

        ExecutorService threadPool = Executors.newFixedThreadPool(numberOfAttempts);

        for (int i = 0; i < numberOfAttempts; i++) {
            threadPool.submit(new TeamFormationTask(participants, teamSize, allAttempts, numberOfTeams));
        }

        threadPool.shutdown();
        try {
            threadPool.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("Thread pool interrupted during team formation", e);
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        List<Team> bestTeams = pickBestTeamSet(allAttempts);

        for (int i = 0; i < bestTeams.size(); i++) {
            bestTeams.get(i).setTeamNumber(i + 1);
        }

        int totalUsed = 0;
        for (Team team : bestTeams) {
            totalUsed += team.getSize();
        }

        logger.info("Team formation completed: " + bestTeams.size() + " teams created with " +
                totalUsed + " participants");
        logDetailedStatistics(bestTeams);
        return bestTeams;
    }

    /**
     * Picks the best team set from all attempts
     * "Best" means: balanced skills, good diversity, follows all rules
     */
    private static List<Team> pickBestTeamSet(List<List<Team>> allAttempts) {
        List<Team> bestTeamSet = null;
        double bestScore = -999999;

        for (List<Team> attempt : allAttempts) {
            if (attempt.isEmpty()) continue;  // Skip empty attempts

            double score = calculateQualityScore(attempt);

            if (score > bestScore) {
                bestScore = score;
                bestTeamSet = attempt;
            }
        }

        if (bestTeamSet == null) {
            bestTeamSet = new ArrayList<>();
        }

        return bestTeamSet;
    }

    private static double calculateQualityScore(List<Team> teams) {
        double totalScore = 0;

        //Skill Balance Across Teams
        totalScore += scoreSkillBalance(teams) * 20;

        //Role Diversity
        totalScore += scoreGameVariety(teams) * 15;
        totalScore += scoreRoleDiversity(teams) * 12;

        // Personality Mix
        totalScore += scorePersonalityMix(teams) * 10;

        int totalPeopleUsed = 0;
        for (Team team : teams) {
            totalPeopleUsed += team.getSize();
        }
        totalScore += totalPeopleUsed * 0.5;

        return totalScore;
    }

    private static double scoreSkillBalance(List<Team> teams) {
        List<Double> teamAverages = new ArrayList<>();
        for (Team team : teams) {
            teamAverages.add(team.getAverageSkill());
        }

        double sum = 0;
        for (double avg : teamAverages) {
            sum += avg;
        }
        double overallAverage = sum / teamAverages.size();

        double sumOfSquares = 0;
        for (double avg : teamAverages) {
            double difference = avg - overallAverage;
            sumOfSquares += difference * difference;
        }
        double variance = sumOfSquares / teamAverages.size();
        return Math.max(0, 100 - variance);
    }

    private static double scoreGameVariety(List<Team> teams) {
        double totalScore = 0;

        for (Team team : teams) {
            Map<String, Integer> gameCounts = new HashMap<>();

            for (Participant member : team.getMembers()) {
                String game = member.getPreferredGame();
                gameCounts.put(game, gameCounts.getOrDefault(game, 0) + 1);
            }

            // Checks if there are more than 2 same game in one team
            boolean hasViolation = false;
            for (int count : gameCounts.values()) {
                if (count > 2) {
                    hasViolation = true;
                    break;
                }
            }
            if (hasViolation) {
                totalScore += 0;
            } else {
                int uniqueGames = gameCounts.size();
                int totalMembers = team.getSize();
                double diversityRatio = (double) uniqueGames / totalMembers;
                totalScore += diversityRatio * 100;
            }
        }
        return totalScore / teams.size();
    }

    private static double scoreRoleDiversity(List<Team> teams) {
        double totalScore = 0;

        for (Team team : teams) {
            Set<Role> uniqueRoles = new HashSet<>();
            for (Participant member : team.getMembers()) {
                uniqueRoles.add(member.getPreferredRole());
            }

            int uniqueRoleCount = uniqueRoles.size();
            int teamSize = team.getSize();

            if (teamSize > 5 && uniqueRoleCount < 3) {
                totalScore += 30;  // Penalty: didn't meet requirement
            } else {
                double diversityRatio = (double) uniqueRoleCount / teamSize;
                totalScore += diversityRatio * 100;
            }
        }

        return totalScore / teams.size();
    }

    private static double scorePersonalityMix(List<Team> teams) {
        double totalScore = 0;

        for (Team team : teams) {
            // Count each personality type
            int leaderCount = 0;
            int thinkerCount = 0;
            int balancedCount = 0;

            for (Participant member : team.getMembers()) {
                PersonalityType type = member.getPersonalityType();
                if (type == PersonalityType.LEADER) leaderCount++;
                else if (type == PersonalityType.THINKER) thinkerCount++;
                else if (type == PersonalityType.BALANCED) balancedCount++;
            }

            double teamScore = getTeamScore(leaderCount, thinkerCount);

            totalScore += teamScore;
        }

        return totalScore / teams.size();
    }

    private static double getTeamScore(int leaderCount, int thinkerCount) {
        double teamScore = 50;

        //Exactly 1 leader
        if (leaderCount == 1) {
            teamScore += 30;
        } else if (leaderCount == 0) {
            teamScore += 10;
        } else {
            teamScore += 0;
        }

        //1-2 thinkers
        if (thinkerCount >= 1 && thinkerCount <= 2) {
            teamScore += 20;
        } else if (thinkerCount == 0) {
            teamScore += 10;
        }
        return teamScore;
    }

    private static void logDetailedStatistics(List<Team> teams) {
        logger.info("========== DETAILED TEAM STATISTICS ==========");

        for (Team team : teams) {
            Map<String, Integer> gameCounts = new HashMap<>();
            for (Participant member : team.getMembers()) {
                String game = member.getPreferredGame();
                gameCounts.put(game, gameCounts.getOrDefault(game, 0) + 1);
            }

            Map<Role, Integer> roleCounts = new HashMap<>();
            for (Participant member : team.getMembers()) {
                Role role = member.getPreferredRole();
                roleCounts.put(role, roleCounts.getOrDefault(role, 0) + 1);
            }

            // Count personalities
            Map<PersonalityType, Integer> personalityCounts = new HashMap<>();
            for (Participant member : team.getMembers()) {
                PersonalityType type = member.getPersonalityType();
                personalityCounts.put(type, personalityCounts.getOrDefault(type, 0) + 1);
            }

            // Log everything
            logger.info(String.format("Team %d: Size=%d, AvgSkill=%.2f",
                    team.getTeamNumber(), team.getSize(), team.getAverageSkill()));
            logger.info("  Games: " + gameCounts);
            logger.info("  Roles: " + roleCounts);
            logger.info("  Personalities: " + personalityCounts);
        }

        logger.info("=============================================");
    }
}