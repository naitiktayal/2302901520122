package org.example.affordmedapi;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PriorityInbox {

    static class Notification {
        String id;
        String type;
        String message;
        String timestamp;

        public Notification(String id, String type, String message, String timestamp) {
            this.id = id;
            this.type = type;
            this.message = message;
            this.timestamp = timestamp;
        }
    }

    private static int getWeight(String type) {
        switch (type) {
            case "Placement":
                return 3;
            case "Result":
                return 2;
            default:
                return 1;
        }
    }

    public static void main(String[] args) {

        List<Notification> notifications = Arrays.asList(
                new Notification("1", "Result", "mid-sem", "2026-04-22 17:51:18"),
                new Notification("2", "Placement", "CSX Corporation hiring", "2026-04-22 17:51:18"),
                new Notification("3", "Event", "farewell", "2026-04-22 17:51:06"),
                new Notification("4", "Result", "project-review", "2026-04-22 17:50:42"),
                new Notification("5", "Placement", "AMD hiring", "2026-04-22 17:49:42"),
                new Notification("6", "Result", "external", "2026-04-22 17:50:30"),
                new Notification("7", "Event", "tech-fest", "2026-04-22 17:50:06"),
                new Notification("8", "Result", "project-review", "2026-04-22 17:49:54")
        );

        notifications.sort((a, b) -> {
            int weightCompare = Integer.compare(
                    getWeight(b.type),
                    getWeight(a.type)
            );

            if (weightCompare != 0)
                return weightCompare;

            return b.timestamp.compareTo(a.timestamp);
        });

        System.out.println("===== TOP PRIORITY NOTIFICATIONS =====");

        int limit = Math.min(10, notifications.size());

        for (int i = 0; i < limit; i++) {
            Notification n = notifications.get(i);

            System.out.println(
                    (i + 1) + ". " +
                            n.type + " | " +
                            n.message + " | " +
                            n.timestamp
            );
        }
    }
}