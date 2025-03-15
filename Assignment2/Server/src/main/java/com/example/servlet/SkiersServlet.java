package com.example.servlet;

import com.example.model.LiftRide;
import com.google.gson.Gson;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Channel;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import com.example.util.Constants;

@WebServlet("/skiers/*")
public class SkiersServlet extends HttpServlet {

    private Connection rabbitMQConnection;
    private final Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(Constants.RABBITMQ_HOST);
            factory.setPort(Constants.RABBITMQ_PORT);
            factory.setUsername(Constants.RABBITMQ_USERNAME);
            factory.setPassword(Constants.RABBITMQ_PASSWORD);
            this.rabbitMQConnection = factory.newConnection();
        } catch (Exception e) {
            throw new ServletException("Failed to initialize RabbitMQ connection.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String urlPath = req.getPathInfo();
        if (urlPath == null || urlPath.isEmpty()) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing URL path.");
            return;
        }

        String[] urlParts = urlPath.split("/");
        if (urlParts.length != 8) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid URL format. Expected: /{resortID}/seasons/{seasonID}/day/{dayID}/skier/{skierID}");
            return;
        }

        try {
            Integer.parseInt(urlParts[1]);
            Integer.parseInt(urlParts[3]);
            Integer.parseInt(urlParts[5]);
            Integer.parseInt(urlParts[7]);
        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "URL parameters must be numeric.");
            return;
        }

        LiftRide liftRide;
        try {
            liftRide = gson.fromJson(req.getReader(), LiftRide.class);
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON in request body.");
            return;
        }

        String validationMsg = validateLiftRide(liftRide);
        if (validationMsg != null) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, validationMsg);
            return;
        }

        try (Channel channel = rabbitMQConnection.createChannel()) {
            channel.queueDeclare(Constants.QUEUE_NAME, true, false, false, null);
            String message = gson.toJson(liftRide);
            channel.basicPublish("", Constants.QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to enqueue message.");
            return;
        }

        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.getWriter().write("Lift ride successfully recorded.");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/plain");
        resp.getWriter().write("Welcome to the Skiers API! Use POST to record a lift ride.");
    }

    @Override
    public void destroy() {
        try {
            if (rabbitMQConnection != null) {
                rabbitMQConnection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // === Helper Methods ===

    private void sendError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        resp.setStatus(statusCode);
        resp.getWriter().write(message);
    }

    private String validateLiftRide(LiftRide ride) {
        if (ride == null) return "Lift ride data is missing.";
        if (ride.getSkierID() < Constants.MIN_SKIER_ID || ride.getSkierID() > Constants.MAX_SKIER_ID)
            return "Invalid skierID.";
        if (ride.getResortID() < Constants.MIN_RESORT_ID || ride.getResortID() > Constants.MAX_RESORT_ID)
            return "Invalid resortID.";
        if (ride.getLiftID() < Constants.MIN_LIFT_ID || ride.getLiftID() > Constants.MAX_LIFT_ID)
            return "Invalid liftID.";
        if (ride.getTime() < Constants.MIN_TIME || ride.getTime() > Constants.MAX_TIME)
            return "Invalid time.";
        return null;
    }
}