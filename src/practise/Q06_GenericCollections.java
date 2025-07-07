package practise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Q06_GenericCollections {

    public static double sum(List<? extends Number>numbers){
        if (numbers == null || numbers.isEmpty()) {
            return 0.0;
        }
        double count = 0;
        for (Number number :numbers){
            count = count + number.doubleValue();
        }
        return count;
    }

    // Enhanced version with streams
    public static double sumSafe(List<? extends Number>numbers){
        return numbers==null ? 0.0 :
                numbers.stream()
                        .filter(Objects::nonNull)
                        .mapToDouble(Number::doubleValue)
                        .sum();
    }

    // Additional statistical methods
    public static double average(List<? extends Number>numbers){
        if(numbers.isEmpty()) return 0.0;
      return numbers.stream()
                .filter(Objects::nonNull)
                .mapToDouble(Number::doubleValue)
                .average()
              .orElse(0.0);
    }

    public static double max(List<? extends Number> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            return 0.0;
        }

        return numbers.stream()
                .filter(Objects::nonNull)
                .mapToDouble(Number::doubleValue)
                .max()
                .orElse(0.0);
    }

    public static double min(List<? extends Number> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            return 0.0;
        }

        return numbers.stream()
                .filter(Objects::nonNull)
                .mapToDouble(Number::doubleValue)
                .min()
                .orElse(0.0);
    }

    // Statistics container
    public static class NumberStatistics {
        private final int count;
        private final double sum;
        private final double average;
        private final double min;
        private final double max;

        public NumberStatistics(int count, double sum, double average, double min, double max) {
            this.count = count;
            this.sum = sum;
            this.average = average;
            this.min = min;
            this.max = max;
        }

        public int getCount() { return count; }
        public double getSum() { return sum; }
        public double getAverage() { return average; }
        public double getMin() { return min; }
        public double getMax() { return max; }

        @Override
        public String toString() {
            return String.format("Stats{count=%d, sum=%.2f, avg=%.2f, min=%.2f, max=%.2f}",
                    count, sum, average, min, max);
        }
    }

    // Comprehensive statistics method
    public static NumberStatistics calculateStatistics(List<? extends Number> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            return new NumberStatistics(0, 0.0, 0.0, 0.0, 0.0);
        }

        List<Double> validNumbers = numbers.stream()
                .filter(Objects::nonNull)
                .map(Number::doubleValue)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        if (validNumbers.isEmpty()) {
            return new NumberStatistics(0, 0.0, 0.0, 0.0, 0.0);
        }

        double sum = validNumbers.stream().mapToDouble(Double::doubleValue).sum();
        double avg = sum / validNumbers.size();
        double min = validNumbers.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        double max = validNumbers.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);

        return new NumberStatistics(validNumbers.size(), sum, avg, min, max);
    }


    public static void main(String[]args){
        System.out.println("=== Q6: Generic Collections ===");

        // Test with different number types
        List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5);
        List<Double> doubles = Arrays.asList(1.5, 2.5, 3.5, 4.5, 5.5);
        List<Float> floats = Arrays.asList(1.1f, 2.2f, 3.3f, 4.4f, 5.5f);
        List<Long> longs = Arrays.asList(10L, 20L, 30L, 40L, 50L);

        System.out.println("--- Basic Sum Operations ---");
        System.out.println("Integer sum: " + sum(integers));
        System.out.println("Double sum: " + sum(doubles));
        System.out.println("Float sum: " + sum(floats));
        System.out.println("Long sum: " + sum(longs));

        // Test with mixed number list
        List<Number> mixed = new ArrayList<>();
        mixed.add(10);        // Integer
        mixed.add(20.5);      // Double
        mixed.add(15.5f);     // Float
        mixed.add(100L);      // Long

        System.out.println("Mixed numbers sum: " + sum(mixed));

        // Test with null values
        List<Integer> withNulls = Arrays.asList(1, null, 3, null, 5);
        System.out.println("\n--- Null Handling ---");
        System.out.println("Sum with nulls (safe): " + sumSafe(withNulls));
        System.out.println("Count of valid numbers: " +
                withNulls.stream().filter(Objects::nonNull).count());

        // Test statistics
        System.out.println("\n--- Statistical Operations ---");
        NumberStatistics stats = calculateStatistics(doubles);
        System.out.println("Double list statistics: " + stats);

        System.out.println("Average of integers: " + average(integers));
        System.out.println("Max of floats: " + max(floats));
        System.out.println("Min of longs: " + min(longs));

        // Demonstrate wildcard flexibility
        System.out.println("\n--- Wildcard Flexibility ---");
        System.out.println("This method accepts any List<? extends Number>:");
        System.out.println("- List<Integer>: " + sum(integers));
        System.out.println("- List<Double>: " + sum(doubles));
        System.out.println("- List<Float>: " + sum(floats));
        System.out.println("- List<Number>: " + sum(mixed));

        // Empty and null lists
        System.out.println("\n--- Edge Cases ---");
        System.out.println("Empty list sum: " + sum(new ArrayList<>()));
        System.out.println("Null list sum: " + sum(null));

        System.out.println("✅ Q6 Completed!\n");
    }
}
