package main;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {
    private static final Logger logger = Logger.getInstance();

    //Reading participants from CSV

    public static List<Participant> readParticipants(String filename) throws FileProcessingException {
        List<Participant> participants = new ArrayList<>();
        File file = new File(filename);

        if (!file.exists()) {
            logger.error("Participants file not found: " + filename, null);
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
            logger.error("Failed to read file: " + filename, e);
            throw new FileProcessingException("Failed to read file: " + filename, e);
        }

        System.out.println("Successfully loaded " + participants.size() + " participants.");
        logger.info("Loaded " + participants.size() + " participants from " + filename);
        return participants;
    }

    //Appending a new member

    public static void appendParticipant(String filename, Participant p) throws FileProcessingException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename, true))) {
            pw.printf("%s,%s,%s,%s,%d,%s,%d,%s%n",
                    p.getId(), p.getName(), p.getEmail(), p.getPreferredGame(),
                    p.getSkillLevel(), p.getPreferredRole(),
                    p.getPersonalityScore(), p.getPersonalityType()
            );
            logger.info("Appended participant " + p.getId() + " to " + filename);
        } catch (IOException e) {
            logger.error("Could not append participant " + p.getId() + " to file", e);
            throw new FileProcessingException("Could not save new member to file", e);
        }
    }

    //Writes the member details to the csv file

    public static void writeTeams(List<Team> teams, String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("TeamNumber,ParticipantID,Name,Game,Role,Personality,Skill");

            int teamCounter = 1;
            for (Team team : teams) {
                String teamName = "Team " + teamCounter;

                for (Participant p : team.getMembers()) {
                    pw.printf("%s,%s,%s,%s,%s,%s,%d%n",
                            teamName,
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
            logger.info("Exported " + teams.size() + " teams to " + filename);
        } catch (IOException e) {
            logger.error("Failed to write teams to " + filename, e);
            System.err.println("Could not write file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}