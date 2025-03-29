package com.example.model;

/**
 * Represents a skier's lift ride event, containing details about the skier,
 * resort, lift used, and timing information.
 * This model class is used to store and transfer lift ride data throughout the system.
 */
public class LiftRide {
  private int skierID;      // Unique identifier for the skier
  private int resortID;     // ID of the resort where the lift ride occurred
  private int liftID;       // Identifier for the specific lift used
  private int seasonID;     // ID representing the ski season
  private int dayID;        // Day number within the season (1-365)
  private int time;         // Timestamp or hour of the day when the ride occurred

  /**
   * Constructs a new LiftRide with all required parameters.
   * 
   * @param skierID  Unique identifier for the skier
   * @param resortID ID of the resort where the lift ride occurred
   * @param liftID   Identifier for the specific lift used
   * @param seasonID ID representing the ski season
   * @param dayID    Day number within the season
   * @param time     Timestamp or hour of the day when the ride occurred
   */
  public LiftRide(int skierID, int resortID, int liftID, int seasonID, int dayID, int time) {
    this.skierID = skierID;
    this.resortID = resortID;
    this.liftID = liftID;
    this.seasonID = seasonID;
    this.dayID = dayID;
    this.time = time;
  }

  // Getter and setter methods with descriptive comments

  /**
   * @return The skier's unique identifier
   */
  public int getSkierID() {
    return skierID;
  }

  /**
   * @param skierID Sets the skier's unique identifier
   */
  public void setSkierID(int skierID) {
    this.skierID = skierID;
  }

  /**
   * @return The resort identifier where the lift ride occurred
   */
  public int getResortID() {
    return resortID;
  }

  /**
   * @param resortID Sets the resort identifier
   */
  public void setResortID(int resortID) {
    this.resortID = resortID;
  }

  /**
   * @return The day number within the season (1-365)
   */
  public int getDayID() {
    return dayID;
  }

  /**
   * @param dayID Sets the day number within the season
   */
  public void setDayID(int dayID) {
    this.dayID = dayID;
  }

  /**
   * @return The timestamp or hour when the lift ride occurred
   */
  public int getTime() {
    return time;
  }

  /**
   * @param time Sets the timestamp or hour of the lift ride
   */
  public void setTime(int time) {
    this.time = time;
  }

  /**
   * @return The lift identifier used in this ride
   */
  public int getLiftID() {
    return liftID;
  }

  /**
   * @param liftID Sets the lift identifier
   */
  public void setLiftID(int liftID) {
    this.liftID = liftID;
  }

  /**
   * @return The season identifier for this lift ride
   */
  public int getSeasonID() {
    return seasonID;
  }

  /**
   * @param seasonID Sets the season identifier
   */
  public void setSeasonID(int seasonID) {
    this.seasonID = seasonID;
  }
}
