package main;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {

    public static List<Participant> readParticipants(String filename) {
        List<Participant> participants = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length < 8) {
                    System.err.println("Skipping bad line: " + line);
                    continue;
                }
                Participant p = new Participant(
                        parts[0].trim(), parts[1].trim(), parts[2].trim(),
                        parts[3].trim(), Integer.parseInt(parts[4].trim()),
                        parts[5].trim(), Integer.parseInt(parts[6].trim())
                );
                participants.add(p);
            }
            System.out.println("Successfully loaded " + participants.size() + " participants.");
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + filename);
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error reading file: " + e.getMessage());
            e.printStackTrace();
        }
        return participants;
    }

    public static void writeTeams(List<Team> teams, String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("TeamNumber,ParticipantID,Name,Game,Role,Personality,Skill");
            for (Team team : teams) {
                for (Participant p : team.getMembers()) {
                    pw.printf("%d,%s,%s,%s,%s,%s,%d%n",
                            team.getMembers().indexOf(p) + 1 + (team.getMembers().size() * (teams.indexOf(team))),
                            p.getId(), p.getName(), p.getPreferredGame(),
                            p.getPreferredRole(), p.getPersonalityType(), p.getSkillLevel());
                }
            }
            System.out.println("Teams saved to " + filename);
        } catch (IOException e) {
            System.err.println("Could not write file: " + e.getMessage());
        }
    }
}