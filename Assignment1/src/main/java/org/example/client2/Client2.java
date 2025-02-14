package org.example.client2;

import org.example.model.LiftRide;
import java.util.concurrent.*;
import java.io.FileWriter;
import java.io.IOException;


public class Client2 {
    private static final int TOTAL_REQUESTS = 200000;
    private static final int THREADS_INITIAL = 32;
    private static final String BASE_URL = "http://localhost:8080/Assignment1-1.0-SNAPSHOT";
    private static final String OUTPUT_FILE = "output.csv";

    public static void main(String[] args) throws InterruptedException, IOException {
        BlockingQueue<LiftRide> eventQueue = new LinkedBlockingQueue<>();
        for (int i = 0; i < TOTAL_REQUESTS; i++) {
            eventQueue.add(LiftRide.generateRandom());
        }

        ExecutorService executor = Executors.newFixedThreadPool(THREADS_INITIAL);
        long startTime = System.currentTimeMillis();
        FileWriter writer = new FileWriter(OUTPUT_FILE);
        writer.write("start_time,request_type,latency,response_code\n");

        for (int i = 0; i < THREADS_INITIAL; i++) {
            executor.execute(new RequestTaskWithMetrics(eventQueue, BASE_URL, writer));
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);
        writer.close();

        long endTime = System.currentTimeMillis();
        System.out.printf("Total Time: %d ms%n", endTime - startTime);
        System.out.printf("Throughput: %.2f requests/sec%n",
                (double) TOTAL_REQUESTS / (endTime - startTime) * 1000);

      //  PerformanceAnalyzer.analyze("output.csv");
    }
}