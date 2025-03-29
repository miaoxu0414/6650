package com.example.clientPart2;

public class Constant {
    public static final int TOTAL_REQUESTS = 200000;
    public static final int INITIAL_THREADS = 32;
    public static final int REQUESTS_PER_THREAD = 1000;

    // Server URLs
    public static final String SERVER_URL = "http://19.654.13.256:8080/Assignment3-1.0-SNAPSHOT/skiers/9/seasons/2025/day/1/skier/20";

    // CSV file
    public static final String CSV_FILE = "latency.csv";

    // HTTP Headers
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
}
