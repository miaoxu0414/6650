package org.example.client2;

import java.util.List;

public class PerformanceAnalyzer {
    public static void analyze(List<Long> responseTimes) {
        responseTimes.sort(Long::compareTo);

        long totalTime = responseTimes.stream().mapToLong(Long::longValue).sum();
        double mean = (double) totalTime / responseTimes.size();
        double median = responseTimes.get(responseTimes.size() / 2);
        long p99 = responseTimes.get((int) (responseTimes.size() * 0.99));
        long min = responseTimes.get(0);
        long max = responseTimes.get(responseTimes.size() - 1);

        System.out.println("Performance Analysis:");
        System.out.printf("Mean Response Time: %.2f ms%n", mean);
        System.out.printf("Median Response Time: %.2f ms%n", median);
        System.out.printf("P99 Response Time: %d ms%n", p99);
        System.out.printf("Min Response Time: %d ms%n", min);
        System.out.printf("Max Response Time: %d ms%n", max);
    }
}