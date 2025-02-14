package org.example.client1;

import org.example.model.LiftRide;
import com.google.gson.Gson;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

public class RequestTask implements Runnable {
    private final BlockingQueue<LiftRide> eventQueue;
    private final MetricsRecorder metricsRecorder;
    private final String serverUrl;
    private final Gson gson = new Gson();
    private static final int MAX_RETRIES = 5;

    public RequestTask(BlockingQueue<LiftRide> eventQueue, MetricsRecorder metricsRecorder, String serverUrl) {
        this.eventQueue = eventQueue;
        this.metricsRecorder = metricsRecorder;
        this.serverUrl = serverUrl;
    }

    @Override
    public void run() {
        while (true) {
            try {
                LiftRide liftRide = eventQueue.poll();
                if (liftRide == null) {
                    return; // No more tasks, exit loop
                }
                sendPostRequest(liftRide);
            } catch (Exception e) {
                System.err.println("Error processing request: " + e.getMessage());
            }
        }
    }

    private void sendPostRequest(LiftRide liftRide) throws Exception {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            attempt++;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(serverUrl + "/skiers");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Convert LiftRide to JSON
                String jsonInputString = gson.toJson(liftRide);

                // Send request
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // Record response time
                long startTime = System.currentTimeMillis();
                int responseCode = connection.getResponseCode();
                long endTime = System.currentTimeMillis();
                metricsRecorder.recordResponseTime(endTime - startTime);

                if (responseCode >= 200 && responseCode < 300) {
                    return; // Success, exit retry loop
                } else {
                    System.out.println("Attempt " + attempt + ": Received response code " + responseCode);
                }
            } catch (Exception e) {
                System.err.println("Attempt " + attempt + " failed: " + e.getMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        System.err.println("Failed to process request after " + MAX_RETRIES + " retries: " + gson.toJson(liftRide));
    }
}