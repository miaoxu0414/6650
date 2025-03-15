package com.example.model;

ublic class LiftRide {
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

  public void setSkierID(int skierID) {
    this.skierID = skierID;
  }

  public void setResortID(int resortID) {
    this.resortID = resortID;
  }

  public void setLiftID(int liftID) {
    this.liftID = liftID;
  }

  public void setSeasonID(int seasonID) {
    this.seasonID = seasonID;
  }

  public void setDayID(int dayID) {
    this.dayID = dayID;
  }

  public void setTime(int time) {
    this.time = time;
  }

}