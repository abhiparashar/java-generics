package questions;

import java.util.*;

public class Pattern10_WildcardCapture {
    public static void main(String[]args){
        System.out.println("Pattern10_WildcardCapture");
        System.out.println("=== PATTERN 10: Wildcard Capture ===");

        List<String> strings = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
        System.out.println("Before swap: " + strings);
        swap(strings, 0, 3);
        System.out.println("After swap: " + strings);

        List<Integer> integers = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        System.out.println("Before reverse: " + integers);
//        reverse(integers);
        System.out.println("After reverse: " + integers);

        // Works with any List type
        List<?> wildcardList = Arrays.asList("X", "Y", "Z");
        System.out.println("Wildcard list: " + wildcardList);

        System.out.println();
    }

    public static void swap(List<?>list, int i, int j){
        swapHelper(list, i,j);
    }

    public static <T> void swapHelper(List<T>list, int i, int j){
        T temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }

    public static void reverse(List<?>list){
            reverseHelper(list);
    }

    public static <T> void reverseHelper(List<T>list){
        Collections.reverse(list);
    }
}
