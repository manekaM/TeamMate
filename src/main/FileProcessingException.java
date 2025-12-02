package main;

//Exceptions for file processing errors
public class FileProcessingException extends Exception {
    public FileProcessingException(String message, Throwable cause) {
        super("File Error: " + message, cause);
    }
}
