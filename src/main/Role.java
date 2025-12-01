package main;

public enum Role {
    STRATEGIST("Strategist"),
    ATTACKER("Attacker"),
    DEFENDER("Defender"),
    SUPPORTER("Supporter"),
    COORDINATOR("Coordinator");

    private final String name;
    Role(String name) { this.name = name; }

    public static Role fromString(String text) {
        for (Role r : Role.values()) {
            if (r.name.equalsIgnoreCase(text)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + text);
    }

    @Override
    public String toString() { return name; }
}
