package main;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {

    public static List<Participant> readParticipants(String filename) throws FileProcessingException {
        List<Participant> participants = new ArrayList<>();
        File file = new File(filename);

        if (!file.exists()) {
            throw new FileProcessingException("Participants file not found: " + filename, null);
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // skip header
            if (line == null) throw new FileProcessingException("CSV file is empty", null);

            int lineNumber = 1;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 8) {
                    System.err.println("Warning: Skipping malformed line " + lineNumber + ": " + line);
                    continue;
                }

                try {
                    Participant p = new Participant(
                            parts[0].trim(),
                            parts[1].trim(),
                            parts[2].trim(),
                            parts[3].trim(),
                            Integer.parseInt(parts[4].trim()),
                            parts[5].trim(),
                            Integer.parseInt(parts[6].trim())
                    );
                    participants.add(p);
                } catch (NumberFormatException e) {
                    throw new InvalidParticipantDataException("Invalid number on line " + lineNumber + ": " + line, e);
                } catch (IllegalArgumentException e) {
                    throw new InvalidParticipantDataException("Invalid role on line " + lineNumber + ": " + line, (NumberFormatException) e);
                }
            }
        } catch (IOException | InvalidParticipantDataException e) {
            throw new FileProcessingException("Failed to read file: " + filename, e);
        }

        System.out.println("Successfully loaded " + participants.size() + " participants.");
        return participants;
    }

    public static void appendParticipant(String filename, Participant p) throws FileProcessingException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename, true))) {
            pw.printf("%s,%s,%s,%s,%d,%s,%d,%s%n",
                    p.getId(), p.getName(), p.getEmail(), p.getPreferredGame(),
                    p.getSkillLevel(), p.getPreferredRole(),
                    p.getPersonalityScore(), p.getPersonalityType()
            );
        } catch (IOException e) {
            throw new FileProcessingException("Could not save new member to file", e);
        }
    }

    public static void writeTeams(List<Team> teams, String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            // Header
            pw.println("TeamNumber,ParticipantID,Name,Game,Role,Personality,Skill");

            int teamCounter = 1;
            for (Team team : teams) {
                String teamName = "Team " + teamCounter;  // This is what the lecturer wants

                for (Participant p : team.getMembers()) {
                    pw.printf("%s,%s,%s,%s,%s,%s,%d%n",
                            teamName,                     // ← Team 1, Team 1, Team 1...
                            p.getId(),
                            p.getName(),
                            p.getPreferredGame(),
                            p.getPreferredRole(),
                            p.getPersonalityType(),
                            p.getSkillLevel()
                    );
                }
                teamCounter++;
            }
            System.out.println("Teams successfully saved to " + filename);
        } catch (IOException e) {
            System.err.println("Could not write file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}