package com.example.clientPart2;

import com.example.model.LiftRide;
import com.google.gson.Gson;
import java.io.PrintWriter;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import com.example.util.Constants;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.example.util.Constants;

package com.example.clientPart2;

public class LiftRideClientPart2 {
  private static final AtomicInteger successfulRequests = new AtomicInteger(0);
  private static final AtomicInteger failedRequests = new AtomicInteger(0);
  private static final List<Long> latencies = Collections.synchronizedList(new ArrayList<>());

  public static void main(String[] args) throws InterruptedException, IOException {
    ExecutorService executor = Executors.newCachedThreadPool();
    BlockingQueue<LiftRide> queue = new LinkedBlockingQueue<>();
    PrintWriter csvWriter = initCsvWriter();

    long startTime = System.currentTimeMillis();

    generateRequests(queue);
    submitInitialThreads(executor, queue, csvWriter);
    submitRemainingTasks(executor, queue, csvWriter);

    shutdownExecutor(executor);
    csvWriter.close();

    calculateAndPrintMetrics(startTime);
  }

  /**
   * Initializes the CSV writer and writes the header row.
   */
  private static PrintWriter initCsvWriter() throws IOException {
    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(Constants.CSV_FILE_PATH));
    PrintWriter csvWriter = new PrintWriter(bufferedWriter);
    csvWriter.println("StartTime,RequestType,Latency,ResponseCode");
    return csvWriter;
  }

  /**
   * Generates random LiftRide requests and adds them to the queue.
   */
  private static void generateRequests(BlockingQueue<LiftRide> queue) {
    new Thread(() -> {
      Random random = new Random();
      for (int i = 0; i < Constants.TOTAL_REQUESTS; i++) {
        queue.add(new LiftRide(
                random.nextInt(Constants.MAX_SKIER_ID) + 1,
                random.nextInt(Constants.MAX_RESORT_ID) + 1,
                random.nextInt(Constants.MAX_LIFT_ID) + 1,
                2025,
                1,
                random.nextInt(Constants.MAX_TIME) + 1
        ));
      }
    }).start();
  }

  /**
   * Submits initial batch of threads, each processing a fixed number of requests.
   */
  private static void submitInitialThreads(ExecutorService executor, BlockingQueue<LiftRide> queue, PrintWriter csvWriter) {
    for (int i = 0; i < Constants.INITIAL_THREADS; i++) {
      executor.submit(() -> processRequests(Constants.REQUESTS_PER_THREAD, queue, csvWriter));
    }
  }

  /**
   * Submits the remaining tasks that were not covered by the initial threads.
   */
  private static void submitRemainingTasks(ExecutorService executor, BlockingQueue<LiftRide> queue, PrintWriter csvWriter) {
    int remainingTasks = Constants.REMAINING_TASKS / Constants.REQUESTS_PER_THREAD;
    for (int i = 0; i < remainingTasks; i++) {
      executor.submit(() -> processRequests(Constants.REQUESTS_PER_THREAD, queue, csvWriter));
    }
  }

  /**
   * Shuts down the executor service and waits for all tasks to complete.
   */
  private static void shutdownExecutor(ExecutorService executor) throws InterruptedException {
    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.HOURS);
  }

  /**
   * Processes a batch of requests from the queue.
   */
  private static void processRequests(int numRequests, BlockingQueue<LiftRide> queue, PrintWriter csvWriter) {
    for (int i = 0; i < numRequests; i++) {
      processRequest(queue, csvWriter);
    }
  }

  /**
   * Processes a single LiftRide request.
   */
  private static void processRequest(BlockingQueue<LiftRide> queue, PrintWriter csvWriter) {
    try {
      LiftRide liftRide = queue.take();
      long requestStartTime = System.currentTimeMillis();
      int statusCode = sendRequest(liftRide);
      long requestEndTime = System.currentTimeMillis();
      long latency = requestEndTime - requestStartTime;

      synchronized (latencies) {
        latencies.add(latency);
      }

      if (statusCode == Constants.SUCCESS_STATUS_CODE) {
        successfulRequests.incrementAndGet();
      } else {
        failedRequests.incrementAndGet();
      }

      synchronized (csvWriter) {
        csvWriter.println(requestStartTime + ",POST," + latency + "," + statusCode);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Sends an HTTP POST request with the given LiftRide data.
   */
  private static int sendRequest(LiftRide liftRide) {
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      HttpPost httpPost = new HttpPost(Constants.SERVER_URL);
      httpPost.setHeader("Content-Type", Constants.CONTENT_TYPE);

      Gson gson = new Gson();
      String json = gson.toJson(liftRide);
      httpPost.setEntity(new StringEntity(json));

      try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
        return response.getCode();
      }
    } catch (Exception e) {
      return -1;
    }
  }

  /**
   * Calculates and prints the performance metrics.
   */
  private static void calculateAndPrintMetrics(long startTime) {
    long endTime = System.currentTimeMillis();
    long totalRunTime = endTime - startTime;
    double throughput = (double) successfulRequests.get() / (totalRunTime / 1000.0);

    Collections.sort(latencies);
    long totalLatency = latencies.stream().mapToLong(Long::longValue).sum();
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