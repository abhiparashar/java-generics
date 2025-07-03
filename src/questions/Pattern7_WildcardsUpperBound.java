package questions;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Pattern7_WildcardsUpperBound {
    public static void main(String[] args){
        System.out.println("=== PATTERN 7: Wildcards Upper Bound (? extends) ===");

        List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5);
        List<Double> doubles = Arrays.asList(1.1, 2.2, 3.3, 4.4);
        List<Float> floats = Arrays.asList(1.1f, 2.2f, 3.3f);

        // All work with ? extends Number
        System.out.println("Sum of integers: " + sum(integers));
        System.out.println("Sum of doubles: " + sum(doubles));
        System.out.println("Sum of floats: " + sum(floats));

        System.out.print("Integers: ");
        printNumbers(integers);

        System.out.print("Doubles: ");
        printNumbers(doubles);

        // Find max values
        System.out.println("Max integer: " + findMax(integers));
        System.out.println("Max double: " + findMax(doubles));

        // Can't add to ? extends Number list
        // List<? extends Number> numbers = integers;
        // numbers.add(10); // Compile error!

        System.out.println();
    }

    public static double sum(List<? extends Number>list){
            double total = 0;
            for (Number item : list){
                total += item.doubleValue();
            }
            return total;
    }

    public static void printNumbers(List<? extends  Number>list){
        for (Number item : list){
            System.out.print(item + " ");
        }
        System.out.println();
    }

    public static <T extends Number & Comparable<T>> Optional<T> findMax(List<T> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            return Optional.empty();
        }

        T max = numbers.get(0);
        for (T number : numbers) {
            if (number.compareTo(max) > 0) {
                max = number;
            }
        }
        return Optional.of(max);
    }
}
