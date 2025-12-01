package main;

public class TeamFormationException extends Exception {
    public TeamFormationException(String message) {
        super("Team Formation Failed: " + message);
    }
}