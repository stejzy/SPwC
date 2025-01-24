package org.example.dataexchangesystem.log;

import org.example.dataexchangesystem.log.LogEntry;
import org.example.dataexchangesystem.repository.LogEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class LogBookService {

    @Autowired
    private LogEntryRepository logEntryRepository;

    private static final String LOG_FILE_PATH = "logbook.txt";

    public void log(String message) {
        logToFile(message);

        logToDatabase(message);
    }

    private void logToFile(String message) {
        String timestamp = LocalDateTime.now().toString();
        String logEntry = timestamp + " - " + message + System.lineSeparator();
        try (FileWriter writer = new FileWriter(LOG_FILE_PATH, true)) {
            writer.write(logEntry);
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }

    private void logToDatabase(String message) {
        LogEntry logEntry = new LogEntry(message);
        logEntryRepository.save(logEntry);
    }
}
