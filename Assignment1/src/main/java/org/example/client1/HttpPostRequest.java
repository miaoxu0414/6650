//package org.example.client1;
//
//import org.example.model.LiftRide;
//import org.example.util.JsonUtil;
//
//import java.io.OutputStream;
//import java.net.HttpURLConnection;
//import java.net.URL;
//
//public class HttpPostRequest {
//
//    /**
//     * Sends an HTTP POST request to the specified URL with the given LiftRide object as JSON payload.
//     *
//     * @param url            The target URL for the POST request.
//     * @param liftRide       The LiftRide object to send in the request body.
//     * @param metricsRecorder The MetricsRecorder object to record response times.
//     * @throws Exception If an error occurs during the request.
//     */
//    public static void sendPostRequest(String url, LiftRide liftRide, MetricsRecorder metricsRecorder) throws Exception {
//        // Create a URL object
//        URL apiUrl = new URL(url);
//        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
//
//        // Set up the connection
//        connection.setRequestMethod("POST");
//        connection.setRequestProperty("Content-Type", "application/json");
//        connection.setDoOutput(true);
//
//        // Convert LiftRide object to JSON
//        String jsonInputString = JsonUtil.toJson(liftRide);
//
//        // Send the request
//        try (OutputStream os = connection.getOutputStream()) {
//            byte[] input = jsonInputString.getBytes("utf-8");
//            os.write(input, 0, input.length);
//        }
//
//        // Record the response time
//        long startTime = System.currentTimeMillis();
//        int responseCode = connection.getResponseCode();
//        long endTime = System.currentTimeMillis();
//        metricsRecorder.recordResponseTime(endTime - startTime);
//
//        // Print the response code (for debugging)
//        System.out.println("Response Code: " + responseCode);
//
//        // Disconnect the connection
//        connection.disconnect();
//    }
//}
