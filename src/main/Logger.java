package main;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private static Logger instance;
    private final PrintWriter writer;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private Logger() {
        try {
            writer = new PrintWriter(new FileWriter("teammate.log", true));
            info("Logger initialized");
        } catch (IOException e) {
            System.err.println("CRITICAL: Could not create log file 'teammate.log'");
            throw new RuntimeException(e);
        }
    }

    public static synchronized Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public synchronized void info(String message) {
        log("INFO ", message);
    }

    public synchronized void error(String message, Throwable t) {
        log("ERROR", message);
        if (t != null) {
            t.printStackTrace(writer);
        }
    }

    private void log(String level, String message) {
        if (writer != null) {
            writer.printf("[%s] %s %s%n", sdf.format(new Date()), level, message);
            writer.flush();
        }
    }

    public synchronized void close() {
        if (writer != null) {
            info("Logger shutting down");
            writer.close();
        }
    }
}