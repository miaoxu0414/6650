package org.example.constant;

/**
 * Holds constant values used throughout the application.
 */
public class SkierConstants {
    // Skier ID constraints
    public static final int MIN_SKIER_ID = 1;
    public static final int MAX_SKIER_ID = 100000;

    // Resort ID constraints
    public static final int MIN_RESORT_ID = 1;
    public static final int MAX_RESORT_ID = 10;

    // Lift ID constraints
    public static final int MIN_LIFT_ID = 1;
    public static final int MAX_LIFT_ID = 40;

    // Season and Day constants
    public static final int SEASON_ID = 2025;
    public static final int DAY_ID = 1;

    // Time range (minutes in a day)
    public static final int MIN_TIME = 1;
    public static final int MAX_TIME = 360;

    // Response messages
    public static final String SUCCESS_MESSAGE = "{\"message\": \"Lift ride recorded\"}";
}
