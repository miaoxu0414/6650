package org.example.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parses the JSON body of an HTTP request into a Java object.
     *
     * @param request The HTTP request.
     * @param clazz   The class of the object to parse into.
     * @param <T>     The type of the object.
     * @return The parsed object.
     * @throws IOException If an error occurs during parsing.
     */
    public static <T> T parseJson(HttpServletRequest request, Class<T> clazz) throws IOException {
        return objectMapper.readValue(request.getInputStream(), clazz);
    }

    /**
     * Converts a Java object to a JSON string.
     *
     * @param obj The object to convert.
     * @return The JSON string.
     * @throws IOException If an error occurs during conversion.
     */
    public static String toJson(Object obj) throws IOException {
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * Sends a JSON response to the client.
     *
     * @param response The HTTP response.
     * @param message  The message to send as JSON.
     * @throws IOException If an error occurs during writing.
     */
    public static void sendJsonResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(message);
    }
}