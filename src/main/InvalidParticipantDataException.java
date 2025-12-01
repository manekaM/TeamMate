package main;

public class InvalidParticipantDataException extends Exception {
    public InvalidParticipantDataException(String message, NumberFormatException e) {
        super("Invalid Participant Data: " + message);
    }
}