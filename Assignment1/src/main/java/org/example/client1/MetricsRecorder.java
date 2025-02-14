package org.example.client1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MetricsRecorder {
    private final List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());

    public void recordResponseTime(long time) {
        responseTimes.add(time);
    }

    public void printStatistics() {
        if (responseTimes.isEmpty()) {
            System.out.println("No response times recorded.");
            return;
        }

        double avg = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long max = Collections.max(responseTimes);
        long min = Collections.min(responseTimes);

        System.out.printf("Response Time - Min: %d ms, Max: %d ms, Avg: %.2f ms%n", min, max, avg);
    }
}