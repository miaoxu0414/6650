package com.example.consumer;

import com.example.model.LiftRide;
import com.google.gson.Gson;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class ConsumerApp {

  private static final Logger logger = LoggerFactory.getLogger(ConsumerApp.class);
  private static final ConcurrentHashMap<Integer, Integer> skierRideCount = new ConcurrentHashMap<>();

  public static void main(String[] args) {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(Constants.RABBITMQ_HOST);
    factory.setPort(Constants.RABBITMQ_PORT);
    factory.setUsername(Constants.USERNAME);
    factory.setPassword(Constants.PASSWORD);

    try {
      Connection connection = factory.newConnection();
      ExecutorService executorService = Executors.newFixedThreadPool(Constants.THREAD_COUNT);

      for (int i = 0; i < Constants.THREAD_COUNT; i++) {
        executorService.submit(new ConsumerTask(connection));
      }

      ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
      scheduler.scheduleAtFixedRate(() -> {
        logger.info("------ Skier Ride Counts ------");
        skierRideCount.forEach((skierId, count) -> logger.info("Skier {}: {}", skierId, count));
      }, 1, 10, TimeUnit.SECONDS);

    } catch (Exception e) {
      logger.error("Failed to start consumer app", e);
    }
  }

  static class ConsumerTask implements Runnable {
    private final Connection connection;
    private final Gson gson = new Gson();

    ConsumerTask(Connection connection) {
      this.connection = connection;
    }

    @Override
    public void run() {
      try (Channel channel = connection.createChannel()) {
        channel.queueDeclare(Constants.QUEUE_NAME, true, false, false, null);
        channel.basicQos(1);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
          try {
            LiftRide liftRide = gson.fromJson(message, LiftRide.class);
            if (liftRide != null && liftRide.getSkierID() != null) {
              skierRideCount.merge(liftRide.getSkierID(), 1, Integer::sum);
              channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            } else {
              logger.warn("Invalid message received: {}", message);
              channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
            }
          } catch (Exception e) {
            logger.error("Error processing message: {}", message, e);
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
          }
        };

        channel.basicConsume(Constants.QUEUE_NAME, false, deliverCallback, consumerTag -> {});
        Thread.currentThread().join();

      } catch (IOException | InterruptedException e) {
        logger.error("ConsumerTask failed", e);
        Thread.currentThread().interrupt();
      }
    }
  }
}

