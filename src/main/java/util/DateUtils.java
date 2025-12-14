package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Small helper class for working with dates and times throughout the app.
 * Keeps all date formatting in one place so the rest of the code stays clean
 * and consistent (especially when talking to MySQL's DATETIME columns).
 */
public class DateUtils {
    
    // Format expected by MySQL DATETIME fields (e.g. 2025-06-15 14:30:22)
    private static final DateTimeFormatter DB_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Same format we use when showing timestamps to users
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Converts a LocalDateTime object into the string format MySQL expects
     * for DATETIME columns.
     *
     * @param dateTime the LocalDateTime to convert (can be null)
     * @return formatted string like "2025-06-15 14:30:22", or null if input was null
     */
    public static String toDatabaseFormat(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DB_FORMATTER);
    }
    
    /**
     * Takes a DATETIME string coming from the database and turns it back
     * into a LocalDateTime object.
     *
     * @param dateTimeStr the string from MySQL, e.g. "2025-06-15 14:30:22"
     * @return LocalDateTime object, or null if the string is empty/null
     */
    public static LocalDateTime fromDatabaseFormat(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return null;
        return LocalDateTime.parse(dateTimeStr, DB_FORMATTER);
    }
    
    /**
     * Formats a LocalDateTime for showing to the user in tables, logs, etc.
     * Right now it's the same as the DB format, but having a separate method
     * makes it easy to change later without touching the DB code.
     *
     * @param dateTime the date/time to format
     * @return human-readable string, or "N/A" if the value is null
     */
    public static String formatForDisplay(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DISPLAY_FORMATTER);
    }
    
    /**
     * Simple wrapper to get the current date and time.
     * Makes unit testing easier if we ever need to mock "now".
     *
     * @return the current LocalDateTime
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
}

