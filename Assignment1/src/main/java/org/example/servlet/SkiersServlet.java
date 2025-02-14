package org.example.servlet;

import com.google.gson.Gson;
import org.example.model.LiftRide;
import org.example.exception.InvalidRequestException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/skiers/*")
public class SkiersServlet extends HttpServlet {
    private static final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // Parse request body
            LiftRide liftRide = gson.fromJson(req.getReader(), LiftRide.class);

            // Validate request data
            validateLiftRide(liftRide);

            // Log event (for debugging)
            System.out.println("Recorded Lift Ride: " + liftRide);

            // Return success response
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"message\": \"Lift ride recorded successfully\"}");
        } catch (InvalidRequestException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private void validateLiftRide(LiftRide ride) throws InvalidRequestException {
        if (ride.getSkierID() < 1 || ride.getSkierID() > 100000) {
            throw new InvalidRequestException("Invalid skierID: " + ride.getSkierID());
        }
        if (ride.getResortID() < 1 || ride.getResortID() > 10) {
            throw new InvalidRequestException("Invalid resortID: " + ride.getResortID());
        }
        if (ride.getLiftID() < 1 || ride.getLiftID() > 40) {
            throw new InvalidRequestException("Invalid liftID: " + ride.getLiftID());
        }
        if (ride.getSeasonID() != 2025) {
            throw new InvalidRequestException("Invalid seasonID: " + ride.getSeasonID());
        }
        if (ride.getDayID() != 1) {
            throw new InvalidRequestException("Invalid dayID: " + ride.getDayID());
        }
        if (ride.getTime() < 1 || ride.getTime() > 360) {
            throw new InvalidRequestException("Invalid time: " + ride.getTime());
        }
    }
}

