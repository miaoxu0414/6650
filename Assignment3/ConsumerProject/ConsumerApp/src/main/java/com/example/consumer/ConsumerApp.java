package com.example.consumer;

import com.example.model.LiftRide;
import com.google.gson.Gson;
import com.rabbitmq.client.*;
import redis.clients.jedis.Jedis;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class ConsumerApp {
  private static final ConcurrentHashMap<Integer, Integer> skierLiftCounter = new ConcurrentHashMap<>();

  public static void main(String[] args) {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(Constant.RABBITMQ_HOST);
    factory.setUsername(Constant.RABBITMQ_USER);
    factory.setPassword(Constant.RABBITMQ_PASS);

    try {
      Connection connection = factory.newConnection();
      ExecutorService executor = Executors.newFixedThreadPool(Constant.THREAD_POOL_SIZE);

      for (int i = 0; i < Constant.THREAD_POOL_SIZE; i++) {
        executor.submit(new LiftRideConsumer(connection));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  static class LiftRideConsumer implements Runnable {
    private final Connection connection;
    private final Gson gson = new Gson();
    private final Jedis redisClient;

    LiftRideConsumer(Connection connection) {
      this.connection = connection;
      this.redisClient = new Jedis(Constant.REDIS_HOST, Constant.REDIS_PORT);
    }

    @Override
    public void run() {
      try {
        Channel channel = connection.createChannel();
        channel.queueDeclare(Constant.QUEUE_NAME, true, false, false, null);
        channel.basicQos(10);

        DeliverCallback callback = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
          try {
            LiftRide liftRide = gson.fromJson(message, LiftRide.class);
            processLiftRide(liftRide);
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
          } catch (Exception e) {
            e.printStackTrace();
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
          }
        };

        channel.basicConsume(Constant.QUEUE_NAME, false, callback, consumerTag -> {});
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    private void processLiftRide(LiftRide ride) {
      String dayKey = ride.getSeasonID() + "-" + ride.getDayID();
      String skierDaysKey = "skier:" + ride.getSkierID() + ":days";
      String skierVerticalKey = "skier:" + ride.getSkierID() + ":vertical";
      String skierLiftsKey = "skier:" + ride.getSkierID() + ":day:" + dayKey + ":lifts";
      String resortSkiersKey = "resort:" + ride.getResortID() + ":day:" + dayKey + ":skiers";

      redisClient.sadd(skierDaysKey, dayKey);
      redisClient.hincrBy(skierVerticalKey, dayKey, ride.getLiftID() * 10);
      redisClient.sadd(skierLiftsKey, String.valueOf(ride.getLiftID()));
      redisClient.sadd(resortSkiersKey, String.valueOf(ride.getSkierID()));

      skierLiftCounter.merge(ride.getSkierID(), 1, Integer::sum);
    }
  }
}
