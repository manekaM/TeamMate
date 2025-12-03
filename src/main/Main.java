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

    //Form teams by getting the team size and number of teams
    private static void formTeamsSafely() {
        if (participants.isEmpty()) {
            System.out.println("No participants available. Please add members first.");
            return;
        }

        // Show current participant count
        System.out.println("\nTotal participants available: " + participants.size());

        // Ask for number of teams and team size
        int numberOfTeams = safeReadPositiveInt("\nEnter number of teams to create: ");
        int teamSize = safeReadPositiveInt("Enter number of members per team: ");

        int totalNeeded = numberOfTeams * teamSize;

        // Check if we have enough participants
        if (totalNeeded > participants.size()) {
            System.out.println(" ===INSUFFICIENT MEMBERS!===\n");
            System.out.printf("You need: %d participants (%d teams × %d members)%n", totalNeeded, numberOfTeams, teamSize);
            System.out.printf("Available: %d participants%n", participants.size());
            System.out.println("\nPlease add more members or reduce the number of teams.");
            System.out.println("Returning to main menu...\n");
            logger.info("Team formation cancelled: Insufficient participants");
            return;
        }

        //Check if we have enough of each personality type
        if (!checkPersonalityAvailability(numberOfTeams, teamSize)) {
            System.out.println("Cannot create " + numberOfTeams + " balanced teams.");
            System.out.println("\nReturning to main menu...\n");
            logger.info("Team formation cancelled: Insufficient personality distribution \n");
            return;
        }

        try {
            long start = System.currentTimeMillis();
            List<Team> teams = TeamBuilder.buildSpecificNumberOfTeams(new ArrayList<>(participants), teamSize, numberOfTeams);
            long time = System.currentTimeMillis() - start;

            int totalUsed = teams.stream().mapToInt(Team::getSize).sum();
            int remainingParticipants = participants.size() - totalUsed;

            System.out.println(" ===TEAM FORMATION COMPLETE!===\n");
            System.out.printf("Created: %d teams%n", teams.size());
            System.out.printf("Total participants used: %d%n", totalUsed);

            if (remainingParticipants > 0) {
                System.out.println("\nNote: " + remainingParticipants + " members are remaining without teams.\n");
            }

            logger.info(String.format("Teams formed: %d teams, %d participants used, %d remaining" ,
                    teams.size(), totalUsed, remainingParticipants));

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

    private static boolean checkPersonalityAvailability(int numberOfTeams, int teamSize) {
        // Count how many of each personality type we have
        int leaderCount = 0;
        int thinkerCount = 0;
        int balancedCount = 0;

        for (Participant p : participants) {
            PersonalityType type = p.getPersonalityType();
            if (type == PersonalityType.LEADER) {
                leaderCount++;
            } else if (type == PersonalityType.THINKER) {
                thinkerCount++;
            } else if (type == PersonalityType.BALANCED) {
                balancedCount++;
            }
        }

        // Calculate what we need for balanced teams
        int leadersNeeded = numberOfTeams;

        // 1-2 thinkers per team
        int thinkersNeeded;
        if (teamSize > 3) {
            thinkersNeeded = numberOfTeams * 2;
        } else {
            thinkersNeeded = numberOfTeams;
        }

        // For balanced: fill the rest (team size - leader - thinkers)
        int thinkersPerTeam = (teamSize > 3) ? 2 : 1;
        int balancedPerTeam = teamSize - 1 - thinkersPerTeam;
        int balancedNeeded = numberOfTeams * balancedPerTeam;

        // Check if we have enough of each type
        boolean hasEnoughLeaders = leaderCount >= leadersNeeded;
        boolean hasEnoughThinkers = thinkerCount >= thinkersNeeded;
        boolean hasEnoughBalanced = balancedCount >= balancedNeeded;

        // Log the results
        logger.info(String.format("Personality check: Leaders %d/%d, Thinkers %d/%d, Balanced %d/%d",
                leaderCount, leadersNeeded, thinkerCount, thinkersNeeded, balancedCount, balancedNeeded));

        // Return true only if we have enough of ALL personality types
        return hasEnoughLeaders && hasEnoughThinkers && hasEnoughBalanced;
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

    //Asking the user to enter the preferred game from the given list
    private static String chooseGame() {
        System.out.println("\nChoose your preferred game:");
        String[] games = {"FIFA", "Valorant", "CS:GO", "DOTA 2", "Basketball", "Chess", "Badminton"};
        for (int i = 0; i < games.length; i++) {
            System.out.println((i + 1) + ". " + games[i]);
        }
        int choice = safeReadIntBounded("Select (1–7): ", 7);
        return games[choice - 1];
    }

    //Asking the user to enter the preferred role from the given list
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
        return total * 4;
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
            System.out.printf("Please enter a number between %d and %d.%n", 1, max);
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
                    System.out.println("Error: Must be greater than 0.");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: '" + input + "' is not a valid number.");
            }
        }
    }
}