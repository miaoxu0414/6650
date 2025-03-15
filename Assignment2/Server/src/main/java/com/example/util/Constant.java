package com.example.util;

public class Constants {
    // RabbitMQ
    public static final String RABBITMQ_HOST = "15.461.233.12";
    public static final int RABBITMQ_PORT = 5672;
    public static final String RABBITMQ_USERNAME = "miaoxu0414";
    public static final String RABBITMQ_PASSWORD = "0414";
    public static final String QUEUE_NAME = "liftRideQueue";


    // Request Configuration
    public static final int TOTAL_REQUESTS = 200000;
    public static final int INITIAL_THREADS = 32;
    public static final int REQUESTS_PER_THREAD = 1000;
    public static final int REMAINING_TASKS = TOTAL_REQUESTS - (INITIAL_THREADS * REQUESTS_PER_THREAD);

    // Server URLs
    public static final String SERVER_URL = "http://assignment2-alb-2153864557.us-west-2.elb.amazonaws.com/Assignment2-1.0-SNAPSHOT/skiers/9/seasons/2025/day/1/skier/20";
    public static final String SINGLE_SERVER_URL = "http://11.31.23.101:8080/Assignment2-1.0-SNAPSHOT/skiers/9/seasons/2025/day/1/skier/20";

    // HTTP Related
    public static final String CONTENT_TYPE = "application/json";
    public static final int SUCCESS_STATUS_CODE = 201;

    // CSV Configuration
    public static final String CSV_FILE_PATH = "latency.csv";

    // Random Value Ranges
    public static final int MAX_SKIER_ID = 100000;
    public static final int MAX_LIFT_ID = 40;
    public static final int MAX_RESORT_ID = 10;
    public static final int MAX_TIME = 360;


    public static final int TOTAL_REQUESTS = 100_000;
    public static final int INITIAL_THREADS = 32;
    public static final int REQUESTS_PER_THREAD = 1000;

    public static final int SKIER_ID_MIN = 1;
    public static final int SKIER_ID_MAX = 100000;
    public static final int RESORT_ID_MIN = 1;
    public static final int RESORT_ID_MAX = 10;
    public static final int LIFT_ID_MIN = 1;
    public static final int LIFT_ID_MAX = 40;
    public static final int SEASON_ID = 2025;
    public static final int DAY_ID = 1;
    public static final int TIME_MIN = 1;
    public static final int TIME_MAX = 360;

    public static final String CSV_FILE = "latency.csv";


    // Skier validation ranges
    public static final int MIN_SKIER_ID = 1;
    public static final int MAX_SKIER_ID = 100000;

    public static final int MIN_RESORT_ID = 1;
    public static final int MAX_RESORT_ID = 10;

    public static final int MIN_LIFT_ID = 1;
    public static final int MAX_LIFT_ID = 40;

    public static final int MIN_TIME = 1;
    public static final int MAX_TIME = 360;
}
