package com.example.model;

/**
 * Represents a skier's lift ride record containing all relevant information
 * about a single lift usage event. This model class serves as the data transfer
 * object for skier activity tracking in a ski resort management system.
 */
public class LiftRide {
    /** Unique identifier of the skier who took the lift ride */
    private int skierID;
    
    /** ID of the resort where the lift ride occurred */
    private int resortID;
    
    /** Identifier of the specific lift used */
    private int liftID;
    
    /** Numeric identifier representing the ski season */
    private int seasonID;
    
    /** Day number within the season (typically 1-365) */
    private int dayID;
    
    /** Time of day when the lift was taken (in minutes or hour format) */
    private int time;

    /**
     * Constructs a complete LiftRide record with all required parameters.
     * 
     * @param skierID  Unique identifier of the skier (positive integer)
     * @param resortID ID of the resort (positive integer)
     * @param liftID   Specific lift used (positive integer)
     * @param seasonID Season identifier (positive integer)
     * @param dayID    Day number in season (1-365)
     * @param time     Time of ride (in minutes from resort opening)
     * @throws IllegalArgumentException if any ID parameter is negative
     */
    public LiftRide(int skierID, int resortID, int liftID, int seasonID, int dayID, int time) {
        if (skierID < 0 || resortID < 0 || liftID < 0 || seasonID < 0 || dayID < 1) {
            throw new IllegalArgumentException("IDs must be positive and dayID must be ≥1");
        }
        this.skierID = skierID;
        this.resortID = resortID;
        this.liftID = liftID;
        this.seasonID = seasonID;
        this.dayID = dayID;
        this.time = time;
    }

    // Standard getters and setters with parameter validation

    public int getSkierID() {
        return skierID;
    }

    public void setSkierID(int skierID) {
        if (skierID < 0) {
            throw new IllegalArgumentException("SkierID must be positive");
        }
        this.skierID = skierID;
    }

    public int getResortID() {
        return resortID;
    }

    public void setResortID(int resortID) {
        if (resortID < 0) {
            throw new IllegalArgumentException("ResortID must be positive");
        }
        this.resortID = resortID;
    }

    public int getDayID() {
        return dayID;
    }

    public void setDayID(int dayID) {
        if (dayID < 1) {
            throw new IllegalArgumentException("DayID must be ≥1");
        }
        this.dayID = dayID;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getLiftID() {
        return liftID;
    }

    public void setLiftID(int liftID) {
        if (liftID < 0) {
            throw new IllegalArgumentException("LiftID must be positive");
        }
        this.liftID = liftID;
    }

    public int getSeasonID() {
        return seasonID;
    }

    public void setSeasonID(int seasonID) {
        if (seasonID < 0) {
            throw new IllegalArgumentException("SeasonID must be positive");
        }
        this.seasonID = seasonID;
    }

    /**
     * Returns a string representation of the lift ride in JSON-like format.
     * @return String containing all lift ride properties
     */
    @Override
    public String toString() {
        return String.format(
            "LiftRide{skierID=%d, resortID=%d, liftID=%d, seasonID=%d, dayID=%d, time=%d}",
            skierID, resortID, liftID, seasonID, dayID, time);
    }
}
