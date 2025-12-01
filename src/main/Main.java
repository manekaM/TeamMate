package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final String CSV_FILE = "data/participants_sample.csv";
    private static List<Participant> participants = new ArrayList<>();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("TeamMate – Gaming Club Team Formation");

        // Load existing participants at startup
        participants = FileHandler.readParticipants(CSV_FILE);

        while (true) {
            System.out.println("\n--- MENU ---");
            System.out.println("1. Add new club member (Take Survey)");
            System.out.println("2. Form balanced teams");
            System.out.println("3. Exit");
            System.out.print("Choose option (1-3): ");

            int choice = readInt();

            if (choice == 1) {
                addNewMember();
            } else if (choice == 2) {
                formTeams();
            } else if (choice == 3) {
                System.out.println("Goodbye! See you at the tournament!");
                break;
            } else {
                System.out.println("Invalid option. Try again.");
            }
        }
        scanner.close();
    }

    private static void addNewMember() {
        System.out.println("\n=== NEW MEMBER SURVEY ===");

        System.out.print("Enter your name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) name = "Participant_" + (participants.size() + 1);

        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();
        if (email.isEmpty()) email = "user" + (participants.size() + 1) + "@university.edu";

        System.out.println("\nChoose your preferred game:");
        System.out.println("1. FIFA  2. Valorant  3. CS:GO  4. DOTA 2  5. Basketball  6. Chess  7. Badminton");
        int gameChoice = readIntBounded(1, 7);
        String game = switch (gameChoice) {
            case 1 -> "FIFA";
            case 2 -> "Valorant";
            case 3 -> "CS:GO";
            case 4 -> "DOTA 2";
            case 5 -> "Basketball";
            case 6 -> "Chess";
            case 7 -> "Badminton";
            default -> "FIFA";
        };

        System.out.println("\nChoose your preferred role:");
        System.out.println("1. Strategist  2. Attacker  3. Defender  4. Supporter  5. Coordinator");
        int roleChoice = readIntBounded(1, 5);
        String role = switch (roleChoice) {
            case 1 -> "Strategist";
            case 2 -> "Attacker";
            case 3 -> "Defender";
            case 4 -> "Supporter";
            case 5 -> "Coordinator";
            default -> "Supporter";
        };

        System.out.println("\nRate these statements (1 = Strongly Disagree, 5 = Strongly Agree)");
        int total = 0;
        String[] questions = {
                "I enjoy taking the lead and guiding others during group activities.",
                "I prefer analyzing situations and coming up with strategic solutions.",
                "I work well with others and enjoy collaborative teamwork.",
                "I am calm under pressure and can help maintain team morale.",
                "I like making quick decisions and adapting in dynamic situations."
        };

        for (int i = 0; i < 5; i++) {
            System.out.printf("Q%d: %s%n>> ", i+1, questions[i]);
            int answer = readIntBounded(1, 5);
            total += answer;
        }

        int score = total * 4; // Scale 5–25 → 20–100
        String personality = PersonalityType.fromScore(score).toString();

        // Generate ID
        String id = String.format("P%03d", participants.size() + 1);

        // Skill level (1–10)
        System.out.print("Rate your overall skill level (1–10): ");
        int skill = readIntBounded(1, 10);

        // Create and add participant
        Participant newMember = new Participant(id, name, email, game, skill, role, score);
        participants.add(newMember);

        // Save to CSV immediately (persistent!)
        FileHandler.appendParticipant(CSV_FILE, newMember);

        System.out.println("\nSUCCESS! Welcome to the club, " + name + "!");
        System.out.println("→ You are a " + personality + " with score " + score);
        System.out.println("→ Added as " + id);
    }

    private static void formTeams() {
        if (participants.isEmpty()) {
            System.out.println("No participants yet. Add some members first!");
            return;
        }

        System.out.print("Enter team size (recommended 5): ");
        int teamSize = readInt();
        if (teamSize < 3 || teamSize > 10) {
            System.out.println("Invalid size. Using 5.");
            teamSize = 5;
        }

        System.out.println("\nForming balanced teams using multiple threads...\n");
        long start = System.currentTimeMillis();
        List<Team> teams = TeamBuilder.buildTeams(new ArrayList<>(participants), teamSize);
        long time = System.currentTimeMillis() - start;

        for (Team t : teams) {
            System.out.println(t);
        }

        FileHandler.writeTeams(teams, "formed_teams.csv");
        System.out.printf("Done in %d ms! %d teams formed → check formed_teams.csv%n", time, teams.size());
    }

    // Helper methods
    private static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (Exception e) {
                System.out.print("Please enter a number: ");
            }
        }
    }

    private static int readIntBounded(int min, int max) {
        while (true) {
            int val = readInt();
            if (val >= min && val <= max) return val;
            System.out.printf("Please enter a number between %d and %d: ", min, max);
        }
    }
}