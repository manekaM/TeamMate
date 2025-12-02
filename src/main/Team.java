package main;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private int teamNumber;
    private final List<Participant> members = new ArrayList<>();

    //Constructor
    public Team(int teamNumber) {
        this.teamNumber = teamNumber;
    }

    //Sets team number
    public void setTeamNumber(int teamNumber) {
        this.teamNumber = teamNumber;
    }

    public int getTeamNumber() {
        return teamNumber;
    }

    //Adding a member to the team
    public void addMember(Participant p) {
        members.add(p);
    }

    //Team members list
    public List<Participant> getMembers() {
        return members;
    }

    //Size of the team
    public int getSize() {
        return members.size();
    }

    //Average skill level
    public double getAverageSkill() {
        if (members.isEmpty()) return 0;
        return members.stream().mapToInt(Participant::getSkillLevel).average().orElse(0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== TEAM ").append(teamNumber).append(" (")
                .append(members.size()).append(" members) ===\n");
        for (Participant p : members) {
            sb.append("  • ").append(p).append("\n");
        }
        sb.append("Average skill: ").append(String.format("%.2f", getAverageSkill())).append("\n\n");
        return sb.toString();
    }
}