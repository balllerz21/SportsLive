package org.example.sportslivev1.utils;
import java.time.Instant;
public class Utilities {
    public static Instant convertToInstant(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return null;
        }
        else if (dateTime.equalsIgnoreCase("daily")) {
            return Instant.now().minusSeconds(24 * 60 * 60); // Subtract 1 day
        }
        else if (dateTime.equalsIgnoreCase("weekly")) {
            return Instant.now().minusSeconds(7 * 24 * 60 * 60); // Subtract 7 days
        }
        else if (dateTime.equalsIgnoreCase("monthly")) {
            return Instant.now().minusSeconds(30 * 24 * 60 * 60); // Subtract 30 days
        }
        else if (dateTime.equalsIgnoreCase("yearly")) {
            return Instant.now().minusSeconds(365 * 24 * 60 * 60); // Subtract 365 days
        }
        else {
            try {
                return Instant.parse(dateTime);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid date format. Please provide a valid timeframe keyword.");
            }
        }
    }
}
