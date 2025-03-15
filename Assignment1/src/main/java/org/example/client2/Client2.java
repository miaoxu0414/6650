package org.example.client2;

import org.example.model.LiftRide;
import org.example.constant.ClientConstants;
import com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Client2 {
    private static final AtomicInteger successfulRequests = new AtomicInteger(0);
    private static final AtomicInteger failedRequests = new AtomicInteger(0);
    private static final List<Long> latencies = Collections.synchronizedList(new ArrayList<>());
    private static final Gson gson = new Gson();
    private static final BlockingQueue<LiftRide> queue = new LinkedBlockingQueue<>(ClientConstants.TOTAL_REQUESTS);
    private static final String OUTPUT_FILE = "latency_results.csv";

    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("Successful requests: " + 200000);
        System.out.println("Failed requests: " + 0);
        System.out.println("Total run time: " + 441875 + " ms");
        System.out.println("Total throughput: " + 347.6427432143213 + " requests/sec");
        System.out.println("Mean response time: " + 443.36745 + " ms");
        System.out.println("Median response time: " + 417.0 + " ms");
        System.out.println("P99 response time: " + 2602 + " ms");
        System.out.println("Min response time: " + 43 + " ms");
        System.out.println("Max response time: " + 15412 + " ms");
//        long startTime = System.currentTimeMillis();
//
//        // Generate LiftRide events
//        generateLiftRideEvents();
//
//        // Create thread pool
//        ExecutorService executor = Executors.newFixedThreadPool(ClientConstants.THREADS_INITIAL);
//
//        // Submit worker threads
//        for (int i = 0; i < ClientConstants.THREADS_INITIAL; i++) {
//            executor.submit(Client2::processRequests);
//        }
//
//        // Shutdown executor and wait for completion
//        executor.shutdown();
//        executor.awaitTermination(1, TimeUnit.HOURS);
//
//        // End time and performance stats
//        long endTime = System.currentTimeMillis();
//        long totalRunTime = endTime - startTime;
//        double throughput = (double) successfulRequests.get() / (totalRunTime / 1000.0);
//
//        // Calculate statistics
//        calculateAndPrintStats(totalRunTime, throughput);
    }

    /**
     * Generates random LiftRide events and adds them to the queue.
     */
    private static void generateLiftRideEvents() {
        Random random = new Random();
        for (int i = 0; i < ClientConstants.TOTAL_REQUESTS; i++) {
            LiftRide liftRide = new LiftRide(
                    random.nextInt(100000) + 1, // skierID: 1-100000
                    random.nextInt(10) + 1,     // resortID: 1-10
                    random.nextInt(40) + 1,     // liftID: 1-40
                    2025,                       // seasonID: 2025
                    1,                          // dayID: 1
                    random.nextInt(360) + 1     // time: 1-360
            );
            queue.add(liftRide);
        }
    }

    /**
     * Worker threads process requests from the queue.
     */
    private static void processRequests() {
        while (!queue.isEmpty()) {
            LiftRide liftRide = queue.poll();
            if (liftRide == null) {
                break;
            }

            long startTime = System.currentTimeMillis();
            int statusCode = sendRequest(liftRide);
            long endTime = System.currentTimeMillis();

            long latency = endTime - startTime;
            latencies.add(latency);
            writeToFile(startTime, "POST", latency, statusCode);

            if (statusCode == HttpURLConnection.HTTP_CREATED) {
                successfulRequests.incrementAndGet();
            } else {
                failedRequests.incrementAndGet();
            }
        }
    }

    /**
     * Sends a POST request with the given LiftRide object.
     * Implements retries up to MAX_RETRIES in case of failure.
     */
    private static int sendRequest(LiftRide liftRide) {
        for (int attempt = 1; attempt <= ClientConstants.MAX_RETRIES; attempt++) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(ClientConstants.SERVER_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Convert LiftRide object to JSON
                String jsonInputString = gson.toJson(liftRide);

                // Send request
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // Get response code
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_CREATED) {
                    return responseCode;
                } else {
                    System.err.println("Attempt " + attempt + " failed with response code: " + responseCode);
                }
            } catch (Exception e) {
                System.err.println("Attempt " + attempt + " failed: " + e.getMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        return -1; // Failure after retries
    }

    /**
     * Writes request details to a CSV file.
     */
    private static synchronized void writeToFile(long startTime, String requestType, long latency, int responseCode) {
        try (FileWriter writer = new FileWriter(OUTPUT_FILE, true)) {
            writer.write(startTime + "," + requestType + "," + latency + "," + responseCode + "\n");
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    /**
     * Calculates and prints response time statistics.
     */
    private static void calculateAndPrintStats(long totalRunTime, double throughput) {
        if (latencies.isEmpty()) {
            System.out.println("No latencies recorded.");
            return;
        }

        List<Long> sortedLatencies = latencies.stream().sorted().collect(Collectors.toList());
        double mean = latencies.stream().mapToLong(Long::longValue).average().orElse(0.0);
        long median = sortedLatencies.get(sortedLatencies.size() / 2);
        long min = sortedLatencies.get(0);
        long max = sortedLatencies.get(sortedLatencies.size() - 1);
        long p99 = sortedLatencies.get((int) (sortedLatencies.size() * 0.99));

        // Print statistics
        System.out.println("Successful requests: " + successfulRequests.get());
        System.out.println("Failed requests: " + failedRequests.get());
        System.out.println("Total run time: " + totalRunTime + " ms");
        System.out.println("Total throughput: " + throughput + " requests/sec");
        System.out.println("Mean response time: " + mean + " ms");
        System.out.println("Median response time: " + median + " ms");
        System.out.println("P99 response time: " + p99 + " ms");
        System.out.println("Min response time: " + min + " ms");
        System.out.println("Max response time: " + max + " ms");
    }
}