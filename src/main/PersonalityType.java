package main;

public enum PersonalityType {
    LEADER("Leader", 90, 100),
    BALANCED("Balanced", 70, 89),
    THINKER("Thinker", 50, 69);

    private final String name;

    PersonalityType(String name, int min, int max) {
        this.name = name;
    }

    public static PersonalityType fromScore(int score) {
        if (score >= 90) return LEADER;
        if (score >= 70) return BALANCED;
        return THINKER;
    }

    @Override
    public String toString() { return name; }
}

