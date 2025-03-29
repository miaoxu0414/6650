package com.example.servlet;

import com.example.model.LiftRide;
import com.google.gson.Gson;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Channel;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * Servlet implementation for handling skier lift ride data submissions.
 * This servlet processes POST requests containing lift ride information,
 * validates the data, and enqueues valid records to RabbitMQ for asynchronous processing.
 * 
 * Endpoint: /skiers/*
 * Methods Supported: POST, GET
 */
@WebServlet("/skiers/*")
public class SkiersServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection rabbitMQConnection;

    /**
     * Initializes servlet and establishes RabbitMQ connection.
     * @throws ServletException if RabbitMQ connection fails
     */
    @Override
    public void init() throws ServletException {
        try {
            initializeRabbitMQ();
        } catch (Exception e) {
            throw new ServletException("Failed to initialize RabbitMQ connection", e);
        }
    }

    /**
     * Configures and establishes connection to RabbitMQ server.
     * Uses connection parameters from Constants class.
     * @throws IOException if network connection fails
     * @throws TimeoutException if connection times out
     */
    private void initializeRabbitMQ() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(Constants.RABBITMQ_HOST);
        factory.setPort(Constants.RABBITMQ_PORT);
        factory.setUsername(Constants.RABBITMQ_USER);
        factory.setPassword(Constants.RABBITMQ_PASSWORD);
        factory.setVirtualHost(Constants.RABBITMQ_VHOST);
        factory.setConnectionTimeout(Constants.RABBITMQ_TIMEOUT);
        
        rabbitMQConnection = factory.newConnection();
    }

    /**
     * Handles POST requests for lift ride submissions.
     * Validates URL path and request body before enqueueing to RabbitMQ.
     * @param req HttpServletRequest containing lift ride data
     * @param resp HttpServletResponse for returning status
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        resp.setContentType("application/json");
        
        try {
            String urlPath = req.getPathInfo();
            if (!isValidUrl(urlPath)) {
                respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, 
                    Constants.INVALID_URL_MESSAGE);
                return;
            }

            LiftRide liftRide = parseRequestBody(req);
            if (liftRide == null || !isValidLiftRide(liftRide)) {
                respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    Constants.INVALID_LIFT_RIDE_MESSAGE);
                return;
            }

            if (!enqueueLiftRide(liftRide)) {
                respondWithError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Constants.QUEUE_ERROR_MESSAGE);
                return;
            }

            respondWithSuccess(resp);
        } catch (Exception e) {
            log("Error processing request", e);
            respondWithError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                Constants.SERVER_ERROR_MESSAGE);
        }
    }

    /**
     * Validates URL path format for lift ride submissions.
     * Expected format: /{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
     * @param urlPath the path info from request
     * @return true if valid format, false otherwise
     */
    private boolean isValidUrl(String urlPath) {
        if (urlPath == null || urlPath.isEmpty() || urlPath.equals("/")) {
            return false;
        }

        String[] urlParts = urlPath.split("/");
        if (urlParts.length != 8) {
            return false;
        }

        try {
            // Validate all numeric components
            Integer.parseInt(urlParts[1]); // resortID
            if (!"seasons".equals(urlParts[2])) return false;
            Integer.parseInt(urlParts[3]); // seasonID
            if (!"days".equals(urlParts[4])) return false;
            Integer.parseInt(urlParts[5]); // dayID
            if (!"skiers".equals(urlParts[6])) return false;
            Integer.parseInt(urlParts[7]); // skierID
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    /**
     * Parses JSON request body into LiftRide object.
     * @param req HttpServletRequest containing JSON body
     * @return LiftRide object or null if parsing fails
     * @throws IOException if reading request fails
     */
    private LiftRide parseRequestBody(HttpServletRequest req) throws IOException {
        try {
            return new Gson().fromJson(req.getReader(), LiftRide.class);
        } catch (Exception e) {
            log("Failed to parse request body", e);
            return null;
        }
    }

    /**
     * Validates LiftRide object field values against business rules.
     * @param liftRide the lift ride data to validate
     * @return true if all fields are valid, false otherwise
     */
    private boolean isValidLiftRide(LiftRide liftRide) {
        return liftRide != null &&
               liftRide.getSkierID() >= Constants.SKIER_ID_MIN && 
               liftRide.getSkierID() <= Constants.SKIER_ID_MAX &&
               liftRide.getResortID() >= Constants.RESORT_ID_MIN && 
               liftRide.getResortID() <= Constants.RESORT_ID_MAX &&
               liftRide.getLiftID() >= Constants.LIFT_ID_MIN && 
               liftRide.getLiftID() <= Constants.LIFT_ID_MAX &&
               liftRide.getTime() >= Constants.TIME_MIN && 
               liftRide.getTime() <= Constants.TIME_MAX;
    }

    /**
     * Publishes lift ride data to RabbitMQ queue.
     * @param liftRide the validated lift ride data
     * @return true if successful, false otherwise
     */
    private boolean enqueueLiftRide(LiftRide liftRide) {
        try (Channel channel = rabbitMQConnection.createChannel()) {
            channel.queueDeclare(Constants.QUEUE_NAME, true, false, false, null);
            String message = new Gson().toJson(liftRide);
            channel.basicPublish("", Constants.QUEUE_NAME, null, 
                message.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (Exception e) {
            log("Failed to enqueue lift ride", e);
            return false;
        }
    }

    /**
     * Sends error response with specified status code and message.
     */
    private void respondWithError(HttpServletResponse resp, int statusCode, String message) 
            throws IOException {
        resp.setStatus(statusCode);
        resp.getWriter().write(String.format("{\"error\":\"%s\"}", message));
    }

    /**
     * Sends success response (201 Created).
     */
    private void respondWithSuccess(HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.getWriter().write(Constants.SUCCESS_MESSAGE);
    }

    /**
     * Handles GET requests (for service testing/verification).
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        resp.setContentType("application/json");
        resp.getWriter().write("{\"message\":\"Skiers API is operational. Use POST to submit lift rides.\"}");
    }

    /**
     * Cleans up RabbitMQ connection when servlet is destroyed.
     */
    @Override
    public void destroy() {
        if (rabbitMQConnection != null && rabbitMQConnection.isOpen()) {
            try {
                rabbitMQConnection.close();
            } catch (Exception e) {
                log("Error closing RabbitMQ connection", e);
            }
        }
    }
}
