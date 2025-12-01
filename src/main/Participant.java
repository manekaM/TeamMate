package main;

public class Participant {
    private final String id;
    private final String name;
    private final String email;
    private final String preferredGame;
    private final int skillLevel;           // 1-10
    private final Role preferredRole;
    private final int personalityScore;
    private final PersonalityType personalityType;

    public Participant(String id, String name, String email, String preferredGame,
                       int skillLevel, String roleStr, int personalityScore) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.preferredGame = preferredGame;
        this.skillLevel = skillLevel;
        this.preferredRole = Role.fromString(roleStr);
        this.personalityScore = personalityScore;
        this.personalityType = PersonalityType.fromScore(personalityScore);
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPreferredGame() { return preferredGame; }
    public int getSkillLevel() { return skillLevel; }
    public Role getPreferredRole() { return preferredRole; }
    public PersonalityType getPersonalityType() { return personalityType; }
    public int getPersonalityScore() { return personalityScore; }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s | Role: %s | Skill: %d | %s",
                name, preferredGame, personalityType, preferredRole, skillLevel, id);
    }
}