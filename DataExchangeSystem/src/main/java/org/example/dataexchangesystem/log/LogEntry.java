package org.example.dataexchangesystem.log;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    private LocalDateTime timestamp;

    public LogEntry() {
    }

    public LogEntry(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // Gettery i Settery
}
