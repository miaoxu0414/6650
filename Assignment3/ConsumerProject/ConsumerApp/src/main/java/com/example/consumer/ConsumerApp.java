package com.example.consumer;

import com.example.model.LiftRide;
import com.google.gson.Gson;
import com.rabbitmq.client.*;
import redis.clients.jedis.Jedis;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class ConsumerApp {
  // A concurrent hash map to keep track of the number of lift rides for each skier
  private static final ConcurrentHashMap<Integer, Integer> skierLiftCounter = new ConcurrentHashMap<>();

  public static void main(String[] args) {
    // Set up RabbitMQ connection factory
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(Constant.RABBITMQ_HOST);
    factory.setUsername(Constant.RABBITMQ_USER);
    factory.setPassword(Constant.RABBITMQ_PASS);

    try {
      // Establish a connection to RabbitMQ
      Connection connection = factory.newConnection();
      
      // Create a fixed thread pool to handle concurrent message processing
      ExecutorService executor = Executors.newFixedThreadPool(Constant.THREAD_POOL_SIZE);

      // Launch multiple consumer threads for parallel processing
      for (int i = 0; i < Constant.THREAD_POOL_SIZE; i++) {
        executor.submit(new LiftRideConsumer(connection));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Consumer class responsible for retrieving messages from the RabbitMQ queue
   * and processing LiftRide data.
   */
  static class LiftRideConsumer implements Runnable {
    private final Connection connection;
    private final Gson gson = new Gson();
    private final Jedis redisClient; // Redis client for storing processed lift ride data

    LiftRideConsumer(Connection connection) {
      this.connection = connection;
      this.redisClient = new Jedis(Constant.REDIS_HOST, Constant.REDIS_PORT);
    }

    @Override
    public void run() {
      try {
        // Create a channel for communication with RabbitMQ
        Channel channel = connection.createChannel();
        channel.queueDeclare(Constant.QUEUE_NAME, true, false, false, null);
        
        // Limit the number of unacknowledged messages to 10 per consumer
        channel.basicQos(10);

        // Define the callback to handle incoming messages
        DeliverCallback callback = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
          try {
            // Convert the JSON message into a LiftRide object
            LiftRide liftRide = gson.fromJson(message, LiftRide.class);
            
            // Process the received lift ride data
            processLiftRide(liftRide);
            
            // Acknowledge successful processing of the message
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
          } catch (Exception e) {
            e.printStackTrace();
            
            // Reject and requeue the message in case of processing failure
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
          }
        };

        // Start consuming messages from the queue (manual acknowledgment mode)
        channel.basicConsume(Constant.QUEUE_NAME, false, callback, consumerTag -> {});
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    /**
     * Processes a LiftRide event by storing relevant data in Redis.
     * 
     * @param ride The LiftRide object containing skier, lift, and resort details.
     */
    private void processLiftRide(LiftRide ride) {
      // Generate Redis keys for tracking skier activity and resort usage
      String dayKey = ride.getSeasonID() + "-" + ride.getDayID();
      String skierDaysKey = "skier:" + ride.getSkierID() + ":days"; // Tracks days the skier has skied
      String skierVerticalKey = "skier:" + ride.getSkierID() + ":vertical"; // Tracks vertical feet skied
      String skierLiftsKey = "skier:" + ride.getSkierID() + ":day:" + dayKey + ":lifts"; // Tracks lifts used on a specific day
      String resortSkiersKey = "resort:" + ride.getResortID() + ":day:" + dayKey + ":skiers"; // Tracks skiers visiting a resort on a given day

      // Store the skiing day for the skier
      redisClient.sadd(skierDaysKey, dayKey);
      
      // Increment the skier's vertical feet skied for the day (each lift ride contributes liftID * 10 feet)
      redisClient.hincrBy(skierVerticalKey, dayKey, ride.getLiftID() * 10);
      
      // Store the lift ID used by the skier on that day
      redisClient.sadd(skierLiftsKey, String.valueOf(ride.getLiftID()));
      
      // Track the skier as having skied at this resort on that day
      redisClient.sadd(resortSkiersKey, String.valueOf(ride.getSkierID()));

      // Update the in-memory counter for skier lift usage
      skierLiftCounter.merge(ride.getSkierID(), 1, Integer::sum);
    }
  }
}

