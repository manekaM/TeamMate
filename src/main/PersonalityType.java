package main;

public enum PersonalityType {
    LEADER("Leader", 90, 100),
    BALANCED("Balanced", 70, 89),
    THINKER("Thinker", 50, 69);

    private final String name;
    private final int min;
    private final int max;

    PersonalityType(String name, int min, int max) {
        this.name = name;
        this.min = min;
        this.max = max;
    }

    public static PersonalityType fromScore(int score) {
        if (score >= 90) return LEADER;
        if (score >= 70) return BALANCED;
        return THINKER;
    }

    @Override
    public String toString() { return name; }
}

