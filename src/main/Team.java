package main;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private int teamNumber;
    private final List<Participant> members = new ArrayList<>();

    public Team(int teamNumber) {
        this.teamNumber = teamNumber;
    }

    public void setTeamNumber(int teamNumber) {
        this.teamNumber = teamNumber;
    }

    public int getTeamNumber() {
        return teamNumber;
    }

    public void addMember(Participant p) {
        members.add(p);
    }

    public List<Participant> getMembers() {
        return members;
    }

    public int getSize() {
        return members.size();
    }

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