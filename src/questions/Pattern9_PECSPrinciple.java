package questions;

import java.util.*;

public class Pattern9_PECSPrinciple {
    public static void main(String[] args){
        System.out.println("Hello PECS");
        System.out.println("=== PATTERN 9: PECS Principle ===");

        // Producer extends example
        List<Integer> integers = Arrays.asList(1, 2, 3);
        List<Double> doubles = Arrays.asList(1.1, 2.2, 3.3);

        // Consumer super example
        List<Number> numbers = new ArrayList<>();
        List<Object> objects = new ArrayList<>();

        // Copy integers to numbers (Integer extends Number)
        copy(numbers, integers);
        System.out.println("Numbers after copying integers: " + numbers);

        // Copy doubles to numbers (Double extends Number)
        copy(numbers, doubles);
        System.out.println("Numbers after copying doubles: " + numbers);

        // Copy numbers to objects (Number extends Object)
        copy(objects, numbers);
        System.out.println("Objects after copying numbers: " + objects);

        // Real Collections.copy example
        List<Number> dest = new ArrayList<>(Collections.nCopies(5, 0));
        List<Integer> src = Arrays.asList(10, 20, 30, 40, 50);
        Collections.copy(dest, src);
        System.out.println("Collections.copy result: " + dest);

        System.out.println();
    }

    public static<T> void copy(List<? super T>dest, List<? extends T>src){
        for (T item : src){
            dest.add(item);
        }
    }

    public static<T> void addAll(Collection<? extends T > src, Collection<? super T>dest){
                dest.addAll(src);
    }
}
