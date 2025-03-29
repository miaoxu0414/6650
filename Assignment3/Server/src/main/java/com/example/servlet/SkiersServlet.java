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

@WebServlet("/skiers/*")
public class SkiersServlet extends HttpServlet {

    private Connection rabbitMQConnection;

    @Override
    public void init() throws ServletException {
        initializeRabbitMQ();
    }

    /**
     * Initializes RabbitMQ connection.
     */
    private void initializeRabbitMQ() throws ServletException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(Constants.RABBITMQ_HOST);
        factory.setPort(Constants.RABBITMQ_PORT);
        factory.setUsername(Constants.RABBITMQ_USER);
        factory.setPassword(Constants.RABBITMQ_PASSWORD);

        try {
            rabbitMQConnection = factory.newConnection();
        } catch (Exception e) {
            throw new ServletException("Failed to establish RabbitMQ connection", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String urlPath = req.getPathInfo();

        if (!isValidUrl(urlPath)) {
            respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, Constants.INVALID_URL_MESSAGE);
            return;
        }

        LiftRide liftRide = parseRequestBody(req, resp);
        if (liftRide == null || !isValidLiftRide(liftRide)) {
            respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, Constants.INVALID_LIFT_RIDE_MESSAGE);
            return;
        }

        if (!enqueueLiftRide(liftRide, resp)) {
            return;
        }

        respondWithSuccess(resp);
    }

    /**
     * Validates the URL format.
     */
    private boolean isValidUrl(String urlPath) {
        if (urlPath == null || urlPath.isEmpty()) {
            return false;
        }

        String[] urlParts = urlPath.split("/");
        if (urlParts.length != 8) {
            return false;
        }

        try {
            Integer.parseInt(urlParts[1]); // Resort ID
            Integer.parseInt(urlParts[3]); // Season ID
            Integer.parseInt(urlParts[5]); // Day ID
            Integer.parseInt(urlParts[7]); // Skier ID
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    /**
     * Parses JSON request body into LiftRide object.
     */
    private LiftRide parseRequestBody(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            return new Gson().fromJson(req.getReader(), LiftRide.class);
        } catch (Exception e) {
            respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, Constants.INVALID_JSON_MESSAGE);
            return null;
        }
    }

    /**
     * Checks if the given LiftRide object contains valid values.
     */
    private boolean isValidLiftRide(LiftRide liftRide) {
        return liftRide.getSkierID() >= 1 && liftRide.getSkierID() <= 100000 &&
                liftRide.getResortID() >= 1 && liftRide.getResortID() <= 10 &&
                liftRide.getLiftID() >= 1 && liftRide.getLiftID() <= 40 &&
                liftRide.getTime() >= 1 && liftRide.getTime() <= 360;
    }

    /**
     * Sends the lift ride data to RabbitMQ.
     */
    private boolean enqueueLiftRide(LiftRide liftRide, HttpServletResponse resp) throws IOException {
        try (Channel channel = rabbitMQConnection.createChannel()) {
            channel.queueDeclare(Constants.QUEUE_NAME, true, false, false, null);
            String message = new Gson().toJson(liftRide);
            channel.basicPublish("", Constants.QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            respondWithError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Constants.SERVER_ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Sends an error response with the given status and message.
     */
    private void respondWithError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        resp.setStatus(statusCode);
        resp.getWriter().write(message);
    }

    /**
     * Sends a success response.
     */
    private void respondWithSuccess(HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.getWriter().write(Constants.SUCCESS_MESSAGE);
    }

    @Override
    public void destroy() {
        closeRabbitMQConnection();
    }

    /**
     * Closes RabbitMQ connection safely.
     */
    private void closeRabbitMQConnection() {
        try {
            if (rabbitMQConnection != null) {
                rabbitMQConnection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain");
        resp.getWriter().write("Welcome to the Skiers API! Use POST to record a lift ride.");
    }
}

