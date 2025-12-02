package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final String CSV_FILE = "data/participants_sample.csv";
    private static List<Participant> participants = new ArrayList<>();
    private static final Scanner scanner = new Scanner(System.in);
    private static final Logger logger = Logger.getInstance();

    public static void main(String[] args) {
        logger.info("=== TeamMate Application STARTED ===");

        System.out.println(" TeamMate – Gaming Club Team Formation ");

        try {
            participants = FileHandler.readParticipants(CSV_FILE);
        } catch (FileProcessingException e) {
            logger.error("FATAL: Cannot load participant data", e);
            System.err.println("FATAL ERROR: Cannot load participant data!");
            System.err.println("→ " + e.getMessage());
            System.err.println("Please check that 'data/participants_sample.csv' exists.");
            System.exit(1);
        }

        while (true) {
            displayMenu();
            int choice = safeReadInt("Choose option (1-4): ");

            switch (choice) {
                case 1 -> {
                    logger.info("User selected: Add new club member");
                    addNewMemberWithSurvey();
                }
                case 2 -> {
                    logger.info("User selected: Form balanced teams");
                    formTeamsSafely();
                }
                case 3 -> {
                    logger.info("User selected: Show club statistics");
                    showStatistics();
                }
                case 4 -> {
                    logger.info("User selected: Exit application");
                    System.out.println("Thank you for using TeamMate! Goodbye!");
                    scanner.close();
                    logger.close();
                    logger.info("=== TeamMate Application EXITED ===");
                    return;
                }
                default -> System.out.println("Invalid option. Please enter 1–4.");
            }
        }
    }

    private static void displayMenu() {
        System.out.println("\n MAIN MENU ");
        System.out.println("1. Add new club member (Take Survey)");
        System.out.println("2. Form balanced teams");
        System.out.println("3. Show club statistics");
        System.out.println("4. Exit");
    }

    private static void formTeamsSafely() {
        if (participants.isEmpty()) {
            System.out.println("No participants available. Please add members first.");
            return;
        }

        int teamSize = safeReadPositiveInt("\nEnter desired team size (e.g. 5): ");

        if (teamSize > participants.size()) {
            System.out.printf("Note: Team size (%d) > total participants (%d). Creating 1 team with everyone.%n",
                    teamSize, participants.size());
        }

        System.out.println("\nForming balanced teams using multi-threading...");

        try {
            long start = System.currentTimeMillis();
            List<Team> teams = TeamBuilder.buildTeams(new ArrayList<>(participants), teamSize);
            long time = System.currentTimeMillis() - start;

            int totalUsed = teams.stream().mapToInt(Team::getSize).sum();
            System.out.println("\nTEAM FORMATION COMPLETE!");
            System.out.printf("Created %,d team(s) using %,d participants in %,d ms%n%n",
                    teams.size(), totalUsed, time);

            logger.info(String.format("Teams formed: %d teams, %d participants, %d ms", teams.size(), totalUsed, time));

            for (Team team : teams) {
                System.out.println(team);
            }

            FileHandler.writeTeams(teams, "formed_teams.csv");
            System.out.println("Teams exported to 'formed_teams.csv'");

        } catch (Exception e) {
            logger.error("Team formation failed", e);
            System.err.println("Error during team formation: " + e.getMessage());
            e.printStackTrace();
        }
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
        try {
            FileHandler.appendParticipant(CSV_FILE, newMember);
            System.out.println("Welcome, " + newMember.getName() + "! Your data has been saved.");
            logger.info("New member added: " + id + " | " + name + " | " + game + " | Skill: " + skill);
        } catch (FileProcessingException e) {
            logger.error("Failed to save new member to file", e);
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    private static String chooseGame() {
        System.out.println("\nChoose your preferred game:");
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
        return total * 4; // Scale to 0-100 range
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

        logger.info(String.format("Statistics shown: %d members | Leaders:%d Balanced:%d Thinkers:%d AvgSkill:%.2f",
                participants.size(), leaders, balanced, thinkers, avgSkill));
    }

    // SAFE INPUT METHODS(So numbers will only be calculated)
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
            if (value >= min && value <= max) return value;
            System.out.printf("Please enter a number between %d and %d.\n", min, max);
        }
    }

    private static String safeReadString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int safeReadPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Error: Empty input. Please enter a positive number.");
                continue;
            }
            try {
                int value = Integer.parseInt(input);
                if (value <= 0) {
                    System.out.println("Error: Team size must be greater than 0.");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: '" + input + "' is not a valid number.");
            }
        }
    }
}