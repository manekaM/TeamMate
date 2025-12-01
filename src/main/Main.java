package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final String CSV_FILE = "data/participants_sample.csv";
    private static List<Participant> participants = new ArrayList<>();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println(" TeamMate – Gaming Club Team Formation ");

        // Load participants at startup with full exception handling
        try {
            participants = FileHandler.readParticipants(CSV_FILE);
        } catch (FileProcessingException e) {
            System.err.println("FATAL ERROR: Cannot load participant data!");
            System.err.println("→ " + e.getMessage());
            System.err.println("The application cannot continue without the CSV file.");
            System.err.println("Please check that 'data/participants_sample.csv' exists.");
            System.exit(1);
        }

        // Main menu loop
        while (true) {
            displayMenu();
            int choice = safeReadInt("Choose option (1-4): ");

            switch (choice) {
                case 1 -> addNewMemberWithSurvey();
                case 2 -> formTeamsSafely();
                case 3 -> showStatistics();
                case 4 -> {
                    System.out.println("Thank you for using TeamMate! Goodbye!");
                    scanner.close();
                    return;
                }
                default -> System.out.println("Invalid option. Please enter 1–4.");
            }
        }
    }

    private static void displayMenu() {
        System.out.println(" MAIN MENU ");
        System.out.println("1. Add new club member (Take Survey)");
        System.out.println("2. Form balanced teams");
        System.out.println("3. Show club statistics");
        System.out.println("4. Exit");
    }

    private static void addNewMemberWithSurvey() {
        System.out.println("\n=== NEW MEMBER REGISTRATION SURVEY ===");

        String name = safeReadString("Enter your name (or press Enter for default): ");
        if (name.isEmpty()) name = "Participant_" + (participants.size() + 101);

        String email = safeReadString("Enter email: ");
        if (email.isEmpty()) email = "user" + (participants.size() + 101) + "@rgu.ac.uk";

        String game = chooseGame();
        String role = chooseRole();

        int personalityScore = conductPersonalitySurvey();
        PersonalityType type = PersonalityType.fromScore(personalityScore);

        int skill = safeReadIntBounded("\nRate your overall skill level (1=Beginner, 10=Pro): ", 1, 10);

        String id = String.format("P%03d", participants.size() + 1);

        Participant newMember = new Participant(id, name, email, game, skill, role, personalityScore);

        participants.add(newMember);

        // Save to file with exception handling
        try {
            FileHandler.appendParticipant(CSV_FILE, newMember);
            System.out.println("\nSUCCESS! " + name + " has been added and saved permanently!");
            System.out.println("→ ID: " + id + " | Game: " + game + " | Role: " + role);
            System.out.println("→ Personality: " + type + " (Score: " + personalityScore + ")");
            System.out.println("→ Skill Level: " + skill + "/10");
        } catch (FileProcessingException e) {
            System.err.println("Could not save member to file: " + e.getMessage());
            System.err.println("Member added to current session but will be lost on exit.");
        }
    }

    private static void formTeamsSafely() {
        if (participants.isEmpty()) {
            System.out.println("No participants available. Please add members first.");
            return;
        }

        int teamSize = safeReadIntBounded("Enter desired team size (3–10, recommended 5): ", 3, 10);

        System.out.println("\nForming balanced teams using multi-threading...");

        try {
            long start = System.currentTimeMillis();
            List<Team> teams = TeamBuilder.buildTeams(new ArrayList<>(participants), teamSize);
            long time = System.currentTimeMillis() - start;

            System.out.println("\nTEAM FORMATION COMPLETE!");
            System.out.printf("Created %d teams in %d ms\n\n", teams.size(), time);

            for (Team team : teams) {
                System.out.println(team);
            }

            FileHandler.writeTeams(teams, "formed_teams.csv");
            System.out.println("Teams exported to 'formed_teams.csv'");

        } catch (Exception e) {
            System.err.println("Unexpected error during team formation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void showStatistics() {
        if (participants.isEmpty()) {
            System.out.println("No data available yet.");
            return;
        }

        System.out.println("\n=== CLUB STATISTICS ===");
        System.out.println("Total members: " + participants.size());

        long leaders = participants.stream().filter(p -> p.getPersonalityType() == PersonalityType.LEADER).count();
        long balanced = participants.stream().filter(p -> p.getPersonalityType() == PersonalityType.BALANCED).count();
        long thinkers = participants.stream().filter(p -> p.getPersonalityType() == PersonalityType.THINKER).count();

        System.out.println("Leaders: " + leaders);
        System.out.println("Balanced: " + balanced);
        System.out.println("Thinkers: " + thinkers);

        double avgSkill = participants.stream().mapToInt(Participant::getSkillLevel).average().orElse(0);
        System.out.printf("Average skill level: %.2f/10\n", avgSkill);
    }

    // Helper methods with validation
    private static String chooseGame() {
        System.out.println("\nChoose preferred game:");
        String[] games = {"FIFA", "Valorant", "CS:GO", "DOTA 2", "Basketball", "Chess", "Badminton"};
        for (int i = 0; i < games.length; i++) {
            System.out.println((i + 1) + ". " + games[i]);
        }
        int choice = safeReadIntBounded("Select (1–7): ", 1, 7);
        return games[choice - 1];
    }

    private static String chooseRole() {
        System.out.println("\nChoose preferred role:");
        String[] roles = {"Strategist", "Attacker", "Defender", "Supporter", "Coordinator"};
        for (int i = 0; i < roles.length; i++) {
            System.out.println((i + 1) + ". " + roles[i]);
        }
        int choice = safeReadIntBounded("Select (1–5): ", 1, 5);
        return roles[choice - 1];
    }

    private static int conductPersonalitySurvey() {
        System.out.println("\nPersonality Survey (1 = Strongly Disagree → 5 = Strongly Agree)");
        String[] questions = {
                "I enjoy taking the lead and guiding others during group activities.",
                "I prefer analyzing situations and coming up with strategic solutions.",
                "I work well with others and enjoy collaborative teamwork.",
                "I am calm under pressure and can help maintain team morale.",
                "I like making quick decisions and adapting in dynamic situations."
        };

        int total = 0;
        for (int i = 0; i < questions.length; i++) {
            System.out.println("Q" + (i + 1) + ": " + questions[i]);
            total += safeReadIntBounded("Your answer (1–5): ", 1, 5);
        }
        return total * 4; // Scale 5–25 → 20–100
    }

    // Safe input methods (never crash)
    private static int safeReadInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static int safeReadIntBounded(String prompt, int min, int max) {
        while (true) {
            int value = safeReadInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            System.out.printf("Please enter a number between %d and %d.\n", min, max);
        }
    }

    private static String safeReadString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}