package questions;

import java.util.ArrayList;
import java.util.List;

public class Pattern8_WildcardsLowerBound {
    public static void main(String[]args){
        System.out.println("=== PATTERN 8: Wildcards Lower Bound (? super) ===");

        List<Number> numbers = new ArrayList<>();
        List<Object> objects = new ArrayList<>();
        List<Integer> integers = new ArrayList<>();

        // All can accept Integer values
        addNumbers(numbers);
        System.out.println("Numbers after adding: " + numbers);

        addNumbers(objects);
        System.out.println("Objects after adding: " + objects);

        addNumbers(integers);
        System.out.println("Integers after adding: " + integers);

        // Fill with more integers
        fillWithIntegers(numbers, 5);
        System.out.println("Numbers after filling: " + numbers);

        // Can't use List<? super Number> with Integer list
        // List<? super Number> superNumbers = integers; // Compile error!

        System.out.println();
    }

    public static void addNumbers(List<? super Integer>list){
        list.add(1);
        list.add(2);
        list.add(3);

//        Integer a  = list.get(0);
        Object obj = list.get(0);
    }

    public static void fillWithIntegers(List<? super Integer>list, int count){
        for (int i = 1; i <= count; i++) {
            list.add(i);
        }
    }
}
