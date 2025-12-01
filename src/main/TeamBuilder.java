package main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TeamBuilder {

    public static List<Team> buildTeams(List<Participant> participants, int teamSize) {
        List<Team> allTeams = new ArrayList<>();

        // Use multiple threads (required for the module!)
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // Split participants into 4 chunks for parallel processing
        int chunk = participants.size() / 4;
        for (int i = 0; i < 4; i++) {
            int start = i * chunk;
            int end = (i == 3) ? participants.size() : start + chunk;
            List<Participant> sublist = participants.subList(start, end);
            executor.submit(new TeamFormationTask(new ArrayList<>(sublist), teamSize, allTeams));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Number the teams properly
        for (int i = 0; i < allTeams.size(); i++) {
            try {
                java.lang.reflect.Field field = Team.class.getDeclaredField("teamNumber");
                field.setAccessible(true);
                field.set(allTeams.get(i), i + 1);
            } catch (Exception ignored) {}
        }

        return allTeams;
    }
}

