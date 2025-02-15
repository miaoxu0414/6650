package org.example.servlet;

import com.google.gson.Gson;
import org.example.model.LiftRide;
import org.example.exception.InvalidRequestException;
import org.example.constant.SkierConstants;

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
            resp.getWriter().write(SkierConstants.SUCCESS_MESSAGE);
        } catch (InvalidRequestException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private void validateLiftRide(LiftRide ride) throws InvalidRequestException {
        if (ride.getSkierID() < SkierConstants.MIN_SKIER_ID || ride.getSkierID() > SkierConstants.MAX_SKIER_ID) {
            throw new InvalidRequestException("Invalid skierID: " + ride.getSkierID());
        }
        if (ride.getResortID() < SkierConstants.MIN_RESORT_ID || ride.getResortID() > SkierConstants.MAX_RESORT_ID) {
            throw new InvalidRequestException("Invalid resortID: " + ride.getResortID());
        }
        if (ride.getLiftID() < SkierConstants.MIN_LIFT_ID || ride.getLiftID() > SkierConstants.MAX_LIFT_ID) {
            throw new InvalidRequestException("Invalid liftID: " + ride.getLiftID());
        }
        if (ride.getSeasonID() != SkierConstants.SEASON_ID) {
            throw new InvalidRequestException("Invalid seasonID: " + ride.getSeasonID());
        }
        if (ride.getDayID() != SkierConstants.DAY_ID) {
            throw new InvalidRequestException("Invalid dayID: " + ride.getDayID());
        }
        if (ride.getTime() < SkierConstants.MIN_TIME || ride.getTime() > SkierConstants.MAX_TIME) {
            throw new InvalidRequestException("Invalid time: " + ride.getTime());
        }
    }
}

