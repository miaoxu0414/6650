package com.example.clientPart2;

import com.example.model.LiftRide;
import com.google.gson.Gson;
import java.io.PrintWriter;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class LiftRideClientPart2 {
  private static final AtomicInteger successfulRequests = new AtomicInteger(0);
  private static final AtomicInteger failedRequests = new AtomicInteger(0);
  private static final List<Long> latencies = Collections.synchronizedList(new ArrayList<>());

  public static void main(String[] args) {
    ExecutorService executorService = Executors.newCachedThreadPool();
    BlockingQueue<LiftRide> queue = new LinkedBlockingQueue<>();

    try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(Constant.CSV_FILE));
         PrintWriter csvWriter = new PrintWriter(bufferedWriter)) {

      csvWriter.println("StartTime,RequestType,Latency,ResponseCode");

      long startTime = System.currentTimeMillis();

      new Thread(() -> generateLiftRides(queue)).start();

      for (int i = 0; i < Constant.INITIAL_THREADS; i++) {
        executorService.submit(() -> sendRequests(queue, csvWriter));
      }

      int remainingRequests = Constant.TOTAL_REQUESTS - (Constant.INITIAL_THREADS * Constant.REQUESTS_PER_THREAD);
      int additionalTasks = remainingRequests / Constant.REQUESTS_PER_THREAD;
      for (int i = 0; i < additionalTasks; i++) {
        executorService.submit(() -> sendRequests(queue, csvWriter));
      }

      executorService.shutdown();
      executorService.awaitTermination(1, TimeUnit.HOURS);

      long totalRunTime = System.currentTimeMillis() - startTime;
      printMetrics(totalRunTime);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void generateLiftRides(BlockingQueue<LiftRide> queue) {
    Random random = new Random();
    for (int i = 0; i < Constant.TOTAL_REQUESTS; i++) {
      try {
        LiftRide liftRide = new LiftRide(
                random.nextInt(100000) + 1,
                random.nextInt(10) + 1,
                random.nextInt(40) + 1,
                2025,
                1,
                random.nextInt(360) + 1
        );
        queue.put(liftRide);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private static void sendRequests(BlockingQueue<LiftRide> queue, PrintWriter csvWriter) {
    for (int i = 0; i < Constant.REQUESTS_PER_THREAD; i++) {
      try {
        LiftRide liftRide = queue.take();
        long startTime = System.currentTimeMillis();
        int statusCode = sendHttpPost(liftRide);
        long latency = System.currentTimeMillis() - startTime;

        synchronized (latencies) {
          latencies.add(latency);
        }

        if (statusCode == 201) {
          successfulRequests.incrementAndGet();
        } else {
          failedRequests.incrementAndGet();
        }

        synchronized (csvWriter) {
          csvWriter.println(startTime + ",POST," + latency + "," + statusCode);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private static int sendHttpPost(LiftRide liftRide) {
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      HttpPost httpPost = new HttpPost(Constant.SERVER_URL);
      httpPost.setHeader(Constant.CONTENT_TYPE, Constant.APPLICATION_JSON);

      String json = new Gson().toJson(liftRide);
      httpPost.setEntity(new StringEntity(json));

      try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
        return response.getCode();
      }
    } catch (Exception e) {
      return -1;
    }
  }

  private static void printMetrics(long totalRunTime) {
    Collections.sort(latencies);
    long totalLatency = latencies.stream().mapToLong(Long::longValue).sum();
    double throughput = (double) successfulRequests.get() / (totalRunTime / 1000.0);
    double meanLatency = (double) totalLatency / latencies.size();
    double medianLatency = latencies.get(latencies.size() / 2);
    long p99Latency = latencies.get((int) (latencies.size() * 0.99));
    long maxLatency = latencies.get(latencies.size() - 1);
    long minLatency = latencies.get(0);

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
