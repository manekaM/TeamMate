package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final String CSV_FILE = "data/participants_sample.csv";
    private static List<Participant> participants = new ArrayList<>();
    private static final Scanner scanner = new Scanner(System.in);
    private static final Logger logger = Logger.getInstance();

    //Main Method
    public static void main(String[] args) {
        logger.info("=== WELCOME TO TEAMMATE ===");

        System.out.println(" Welcome to TeamMate – University Gaming Club Team Formation ");

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
            int choice = safeReadInt("Choose option (1-3): ");

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
                    logger.info("User selected: Exit Program");
                    System.out.println("Thank you for using TeamMate!");
                    scanner.close();
                    logger.close();
                    logger.info("=== TeamMate Application EXITED ===");
                    return;
                }
                default -> System.out.println("Invalid option. Please enter 1–3.");
            }
        }
    }

    //Displays the Main Menu Options
    private static void displayMenu() {
        System.out.println("\n === MAIN MENU === ");
        System.out.println("1. Add new member (Take Survey)");
        System.out.println("2. Form Teams");
        System.out.println("3. Exit");
    }

    //Form teams by getting the team size
    private static void formTeamsSafely() {
        if (participants.isEmpty()) {
            System.out.println("No participants available. Please add members first.");
            return;
        }

        int teamSize = safeReadPositiveInt();

        if (teamSize > participants.size()) {
            System.out.printf("Note: Team size (%d) > total participants (%d). Creating 1 team with everyone.%n",
                    teamSize, participants.size());
        }

        try {
            long start = System.currentTimeMillis();
            List<Team> teams = TeamBuilder.buildTeams(new ArrayList<>(participants), teamSize);
            long time = System.currentTimeMillis() - start;

            int totalUsed = teams.stream().mapToInt(Team::getSize).sum();
            System.out.println("\nTEAM FORMATION COMPLETE! \n");
            System.out.printf("Created %,d teams using %,d participants \n",
                    teams.size(), totalUsed);

            logger.info(String.format("Teams formed: %d teams, %d participants \n" , teams.size(), totalUsed));

            for (Team team : teams) {
                System.out.println(team);
            }

            FileHandler.writeTeams(teams, "formed_teams.csv");

        } catch (Exception e) {
            logger.error("Team formation failed", e);
            System.err.println("Error during team formation: " + e.getMessage());
            e.printStackTrace();
        }
    }


    //Adding a new member by getting details through a survey
    private static void addNewMemberWithSurvey() {
        System.out.println("\n=== NEW MEMBER REGISTRATION SURVEY ===");

        String name = "";
        while (name.trim().isEmpty()) {
            name = safeReadString("Enter your name (required): ");
            if (name.trim().isEmpty()) {
                System.out.println("Error: Name cannot be empty. Please try again.");
            }
        }

        String email = "";
        while (email.trim().isEmpty() || !email.contains("@")) {
            email = safeReadString("Enter your email (must contain @): ");
            if (email.trim().isEmpty()) {
                System.out.println("Error: Email cannot be empty.");
            } else if (!email.contains("@")) {
                System.out.println("Error: Please enter a valid email address containing '@'.");
            }
        }
        String game = chooseGame();
        String role = chooseRole();

        int personalityScore = conductPersonalitySurvey();

        int skill = safeReadIntBounded("\nEnter your skill level (1=Beginner, 10=Pro): ", 10);

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

    //Asking the user to enter tge preferred game from the given list
    private static String chooseGame() {
        System.out.println("\nChoose your preferred game:");
        String[] games = {"FIFA", "Valorant", "CS:GO", "DOTA 2", "Basketball", "Chess", "Badminton"};
        for (int i = 0; i < games.length; i++) {
            System.out.println((i + 1) + ". " + games[i]);
        }
        int choice = safeReadIntBounded("Select (1–7): ", 7);
        return games[choice - 1];
    }

    //Asking the user to enter tge preferred role from the given list
    private static String chooseRole() {
        System.out.println("\nChoose preferred role:");
        String[] roles = {"Strategist", "Attacker", "Defender", "Supporter", "Coordinator"};
        for (int i = 0; i < roles.length; i++) {
            System.out.println((i + 1) + ". " + roles[i]);
        }
        int choice = safeReadIntBounded("Select (1–5): ", 5);
        return roles[choice - 1];
    }

    //Asking the Personality Questions
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
            total += safeReadIntBounded("Your answer (1–5): ", 5);
        }
        return total * 4; // Scale to 0-100 range
    }

    // Safe Input Methods to prevent invalid inputs from user
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

    private static int safeReadIntBounded(String prompt, int max) {
        while (true) {
            int value = safeReadInt(prompt);
            if (value >= 1 && value <= max) return value;
            System.out.printf("Please enter a number between %d and %d.", 1, max);
        }
    }

    private static String safeReadString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int safeReadPositiveInt() {
        while (true) {
            System.out.print("\nEnter desired team size: ");
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