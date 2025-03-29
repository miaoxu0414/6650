package com.example.clientPart2;

import com.example.model.LiftRide;
import com.google.gson.Gson;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LiftRideClientPart2 {
    // Atomic counters to track the number of successful and failed requests
    private static final AtomicInteger successfulRequests = new AtomicInteger(0);
    private static final AtomicInteger failedRequests = new AtomicInteger(0);

    // A thread-safe list to store latencies of all requests
    private static final List<Long> latencies = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        // Thread pool for managing request execution
        ExecutorService executorService = Executors.newCachedThreadPool();
        // Blocking queue to store lift ride requests before sending
        BlockingQueue<LiftRide> queue = new LinkedBlockingQueue<>();

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(Constant.CSV_FILE));
             PrintWriter csvWriter = new PrintWriter(bufferedWriter)) {

            // Write CSV header for logging request details
            csvWriter.println("StartTime,RequestType,Latency,ResponseCode");

            long startTime = System.currentTimeMillis();

            // Start a separate thread to generate lift ride requests
            new Thread(() -> generateLiftRides(queue)).start();

            // Submit initial set of worker threads to send requests
            for (int i = 0; i < Constant.INITIAL_THREADS; i++) {
                executorService.submit(() -> sendRequests(queue, csvWriter));
            }

            // Calculate remaining requests to be sent by additional threads
            int remainingRequests = Constant.TOTAL_REQUESTS - (Constant.INITIAL_THREADS * Constant.REQUESTS_PER_THREAD);
            int additionalTasks = remainingRequests / Constant.REQUESTS_PER_THREAD;
            for (int i = 0; i < additionalTasks; i++) {
                executorService.submit(() -> sendRequests(queue, csvWriter));
            }

            // Shutdown the executor and wait for all tasks to complete
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.HOURS);

            long totalRunTime = System.currentTimeMillis() - startTime;
            printMetrics(totalRunTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates random LiftRide requests and adds them to the blocking queue.
     */
    private static void generateLiftRides(BlockingQueue<LiftRide> queue) {
        Random random = new Random();
        for (int i = 0; i < Constant.TOTAL_REQUESTS; i++) {
            try {
                LiftRide liftRide = new LiftRide(
                        random.nextInt(100000) + 1, // Skier ID (1 - 100000)
                        random.nextInt(10) + 1, // Resort ID (1 - 10)
                        random.nextInt(40) + 1, // Lift ID (1 - 40)
                        2025, // Season year
                        1, // Day ID (static value for simplicity)
                        random.nextInt(360) + 1 // Time (1 - 360 minutes)
                );
                queue.put(liftRide); // Add to queue for processing
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Takes requests from the queue and sends them to the server.
     */
    private static void sendRequests(BlockingQueue<LiftRide> queue, PrintWriter csvWriter) {
        for (int i = 0; i < Constant.REQUESTS_PER_THREAD; i++) {
            try {
                LiftRide liftRide = queue.take(); // Retrieve a lift ride request
                long startTime = System.currentTimeMillis(); // Record start time
                int statusCode = sendHttpPost(liftRide); // Send HTTP POST request
                long latency = System.currentTimeMillis() - startTime; // Compute latency

                // Store latency in a synchronized manner
                synchronized (latencies) {
                    latencies.add(latency);
                }

                // Update request success or failure count
                if (statusCode == 201) {
                    successfulRequests.incrementAndGet();
                } else {
                    failedRequests.incrementAndGet();
                }

                // Log request details into the CSV file
                synchronized (csvWriter) {
                    csvWriter.println(startTime + ",POST," + latency + "," + statusCode);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Sends an HTTP POST request with the given LiftRide data.
     *
     * @param liftRide The LiftRide object to send
     * @return HTTP response status code, or -1 in case of failure
     */
    private static int sendHttpPost(LiftRide liftRide) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(Constant.SERVER_URL);
            httpPost.setHeader(Constant.CONTENT_TYPE, Constant.APPLICATION_JSON);

            // Convert LiftRide object to JSON and set it as the request body
            String json = new Gson().toJson(liftRide);
            httpPost.setEntity(new StringEntity(json));

            // Execute the HTTP request and return the response code
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                return response.getCode();
            }
        } catch (Exception e) {
            return -1; // Indicate failure
        }
    }

    /**
     * Computes and prints performance metrics such as throughput and latencies.
     *
     * @param totalRunTime The total execution time in milliseconds
     */
    private static void printMetrics(long totalRunTime) {
        // Sort latencies for percentile calculations
        Collections.sort(latencies);
        long totalLatency = latencies.stream().mapToLong(Long::longValue).sum();

        // Compute throughput (requests per second)
        double throughput = (double) successfulRequests.get() / (totalRunTime / 1000.0);
        // Compute mean latency
        double meanLatency = (double) totalLatency / latencies.size();
        // Compute median latency
        double medianLatency = latencies.get(latencies.size() / 2);
        // Compute 99th percentile latency
        long p99Latency = latencies.get((int) (latencies.size() * 0.99));
        // Compute max and min latency
        long maxLatency = latencies.get(latencies.size() - 1);
        long minLatency = latencies.get(0);

        // Print metrics to console
        System.out.println("Successful requests: " + successfulRequests.get());
        System.out.println("Failed requests: " + failedRequests.get());
        System.out.println("Total run time: " + totalRunTime + " ms");
        System.out.println("Total throughput: " + throughput + " requests/sec");
        System.out.println("Mean latency: " + meanLatency + " ms");
        System.out.println("Median latency: " + medianLatency + " ms");
        System.out.println("p99 latency: " + p99Latency + " ms");
        System.out.println("Max latency: " + maxLatency + " ms");
        System.out.println("Min latency: " + minLatency + " ms");
    }
}

