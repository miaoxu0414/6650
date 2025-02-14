package org.example.constant;

public class ClientConstants {
    public static final String SERVER_URL = "http://your-ec2-ip:8080/Assignment1/skiers";

    public static final int TOTAL_REQUESTS = 200000;
    public static final int THREADS_INITIAL = 32;
    public static final int REQUESTS_PER_THREAD = 1000;
    public static final int MAX_RETRIES = 5;

    // ✅ Add This
    public static final String CSV_FILE = "output/latency_results.csv";
    public static final String PLOT_FILE = "output/throughput_plot.png";
}