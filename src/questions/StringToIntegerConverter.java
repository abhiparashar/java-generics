package questions;

import java.util.Arrays;
import java.util.List;

interface Converter<T, R>{
    R convert(T input);

    // Default method for error handling
    default R convertSafely(T input, R defaultValue){
        try{
            return convert(input);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}

public class StringToIntegerConverter implements Converter<String,Integer>{

    @Override
    public Integer convert(String input) {
        return 0;
    }
};

class Q04_GenericInterface {
    public static void main(String[] args) {
        System.out.println("=== Generic Converter Interface ===");

        // String to Integer conversion
        Converter<String, Integer> stringToInt = new StringToIntegerConverter();

        System.out.println("String '123' to Integer: " + stringToInt.convert("123"));
//        System.out.println("String '456' safely: " + stringToInt.convertSafely("456", -1));
//        System.out.println("Invalid string safely: " + stringToInt.convertSafely("abc", -1));

        // Integer to String conversion
//        Converter<Integer, String> intToString = new IntegerToStringConverter();
//        System.out.println("Integer 789 to String: '" + intToString.convert(789) + "'");

        // Chaining converters: String -> Integer -> String
//        Converter<String, String> roundTrip = stringToInt.andThen(intToString);
//        System.out.println("Round trip '999': '" + roundTrip.convert("999") + "'");

        // Convert list of strings to integers
        List<String> stringNumbers = Arrays.asList("1", "2", "3", "4", "5");
//        List<Integer> integers = ConverterUtils.convertList(stringNumbers, stringToInt);
//        System.out.println("Converted list: " + integers);

        // String to Double
//        Converter<String, Double> stringToDouble = new StringToDoubleConverter();
        List<String> stringDecimals = Arrays.asList("1.1", "2.2", "3.3");
//        List<Double> doubles = ConverterUtils.convertList(stringDecimals, stringToDouble);
//        System.out.println("Converted doubles: " + doubles);

        System.out.println("✅ Q4 Completed!");
    }
}