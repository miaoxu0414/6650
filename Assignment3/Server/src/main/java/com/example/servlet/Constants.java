package com.example.servlet;

public class Constants {
    public static final String QUEUE_NAME = "liftRideQueue";
    public static final String RABBITMQ_HOST = "14.44.63.75";
    public static final int RABBITMQ_PORT = 5672;
    public static final String RABBITMQ_USER = "miao";
    public static final String RABBITMQ_PASSWORD = "940430";

    public static final String SUCCESS_MESSAGE = "Lift ride recorded successfully.";
    public static final String INVALID_URL_MESSAGE = "Invalid URL format. Expected format: /{resortID}/seasons/{seasonID}/day/{dayID}/skier/{skierID}";
    public static final String INVALID_JSON_MESSAGE = "Invalid JSON format in request body.";
    public static final String INVALID_LIFT_RIDE_MESSAGE = "Invalid lift ride details. Please check skierID, resortID, liftID, and time.";
    public static final String SERVER_ERROR_MESSAGE = "Internal server error. Please try again later.";
}

