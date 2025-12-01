package main;

public class InvalidSurveyInputException extends Exception {
    public InvalidSurveyInputException(String message) {
        super("Survey Error: " + message);
    }
}
