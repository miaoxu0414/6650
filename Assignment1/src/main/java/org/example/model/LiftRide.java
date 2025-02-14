package org.example.model;

import com.google.gson.Gson;

public class LiftRide {
    private int skierID;
    private int liftID;
    private int resortID;
    private int seasonID;
    private int dayID;
    private int time;

    private static final Gson gson = new Gson();

    // ✅ Added Constructor for direct object creation
    public LiftRide(int skierID, int resortID, int liftID, int seasonID, int dayID, int time) {
        this.skierID = skierID;
        this.resortID = resortID;
        this.liftID = liftID;
        this.seasonID = seasonID;
        this.dayID = dayID;
        this.time = time;
    }

    public String toJson() {
        return gson.toJson(this);
    }

    public int getSkierID() {
        return skierID;
    }

    public int getLiftID() {
        return liftID;
    }

    public int getResortID() {
        return resortID;
    }

    public int getSeasonID() {
        return seasonID;
    }

    public int getDayID() {
        return dayID;
    }

    public int getTime() {
        return time;
    }

    // ✅ Keep generateRandom() to create random instances
    public static LiftRide generateRandom() {
        return new LiftRide(
                (int) (Math.random() * 100000) + 1, // skierID: 1-100000
                1,                                  // resortID (fixed)
                (int) (Math.random() * 40) + 1,     // liftID: 1-40
                2024,                               // seasonID
                1,                                  // dayID
                (int) (Math.random() * 360) + 1     // time: 1-360
        );
    }
}