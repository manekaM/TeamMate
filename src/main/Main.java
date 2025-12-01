package main;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== TeamMate - Gaming Club Team Formation System ===\n");

        List<Participant> participants = FileHandler.readParticipants("data/participants_sample.csv");

        System.out.print("Enter desired team size (How many members in a team): ");
        int teamSize = scanner.nextInt();

        if (teamSize < 5) {
            System.out.println("Team size should be greater than 5. Please enter a valid team size");
            teamSize = 5;
        }

        System.out.println("\nForming teams.Please wait...\n");

        long start = System.currentTimeMillis();
        List<Team> teams = TeamBuilder.buildTeams(participants, teamSize);
        long time = System.currentTimeMillis() - start;

        System.out.printf("Formed %d teams in %d ms\n\n", teams.size(), time);

        // Show results
        for (Team team : teams) {
            System.out.println(team);
        }

        // Save to file
        FileHandler.writeTeams(teams, "formed_teams.csv");

        System.out.println("All done! Check formed_teams.csv");
        scanner.close();
    }
}
