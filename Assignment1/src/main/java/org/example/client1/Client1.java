package org.example.client1;

import org.example.model.LiftRide;
import com.google.gson.Gson;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Client1 {
    private static final int TOTAL_REQUESTS = 200000;
    private static final int INITIAL_THREADS = 32;
    private static final int MAX_RETRIES = 5;
    private static final String SERVER_URL = "http://34.217.28.9:8080/Assignment1-1.0-SNAPSHOT/skiers";

    private static final AtomicInteger successfulRequests = new AtomicInteger(0);
    private static final AtomicInteger failedRequests = new AtomicInteger(0);
    private static final Gson gson = new Gson();
    private static final BlockingQueue<LiftRide> queue = new LinkedBlockingQueue<>(TOTAL_REQUESTS);

    public static void main(String[] args) throws InterruptedException {
        long startTime = System.currentTimeMillis();

        // Generate LiftRide events
        generateLiftRideEvents();

        // Create thread pool
        ExecutorService executor = Executors.newFixedThreadPool(INITIAL_THREADS);

        // Submit worker threads
        for (int i = 0; i < INITIAL_THREADS; i++) {
            executor.submit(Client1::processRequests);
        }

        // Shutdown executor and wait for completion
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        // End time and performance stats
        long endTime = System.currentTimeMillis();
        long totalRunTime = endTime - startTime;
        double throughput = (double) successfulRequests.get() / (totalRunTime / 1000.0);

        // Print results
        System.out.println("Successful requests: " + successfulRequests.get());
        System.out.println("Failed requests: " + failedRequests.get());
        System.out.println("Total run time: " + totalRunTime + " ms");
        System.out.println("Total throughput: " + throughput + " requests/sec");
    }

    /**
     * Generates random LiftRide events and adds them to the queue.
     */
    private static void generateLiftRideEvents() {
        Random random = new Random();
        for (int i = 0; i < TOTAL_REQUESTS; i++) {
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

            int statusCode = sendRequest(liftRide);
            if (statusCode == 201) {
                successfulRequests.incrementAndGet();
            } else {
                failedRequests.incrementAndGet();
            }
        }
    }

    /**
     * Sends a POST request with the given LiftRide object.
     * Implements retries up to 5 times in case of failure.
     */
    private static int sendRequest(LiftRide liftRide) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(SERVER_URL);
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
                if (responseCode == 201) {
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
}