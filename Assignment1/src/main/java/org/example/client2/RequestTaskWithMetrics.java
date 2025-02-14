package org.example.client2;

import org.example.model.LiftRide;
import org.example.util.JsonUtil;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.BlockingQueue;

public class RequestTaskWithMetrics implements Runnable {
    private final BlockingQueue<LiftRide> eventQueue;
    private final String baseUrl;
    private final FileWriter writer;

    public RequestTaskWithMetrics(BlockingQueue<LiftRide> eventQueue, String baseUrl, FileWriter writer) {
        this.eventQueue = eventQueue;
        this.baseUrl = baseUrl;
        this.writer = writer;
    }

    @Override
    public void run() {
        while (!eventQueue.isEmpty()) {
            try {
                LiftRide liftRide = eventQueue.poll();
                if (liftRide == null) continue;
                long start = System.currentTimeMillis();

                int responseCode = sendPostRequest(liftRide);

                long latency = System.currentTimeMillis() - start;
                synchronized (writer) {
                    writer.write(String.format("%d,POST,%d,%d\n", start, latency, responseCode));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int sendPostRequest(LiftRide liftRide) throws IOException {
        URL url = new URL(baseUrl + "/skiers");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.getOutputStream().write(liftRide.toJson().getBytes());
        return conn.getResponseCode();
    }
}
