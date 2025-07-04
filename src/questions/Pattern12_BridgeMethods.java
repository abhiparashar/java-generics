package questions;

import java.util.Arrays;
import java.util.List;

public class Pattern12_BridgeMethods {
    public interface Processor<T>{
        void process(T item);
    }
    public static class StringProcessor implements Processor<String>{

        @Override
        public void process(String item) {
            System.out.println("Processing string: " + item.toUpperCase());
        }
    }
    public static class NumberProcessor implements Processor<Number>{

        @Override
        public void process(Number item) {
           System.out.println("Processing string: " + item.doubleValue());
        }
    }
    public static void main(String[] args){
        System.out.println("=== PATTERN 12: Bridge Methods ===");

        Processor<String> stringProcessor = new StringProcessor();
        Processor<Number> numberProcessor = new NumberProcessor();

        // Direct calls
        stringProcessor.process("hello");
        numberProcessor.process(42);

        // Polymorphic calls (uses bridge methods internally)
        List<Processor<?>> processors = Arrays.asList(stringProcessor, numberProcessor);

        // The bridge methods ensure this works correctly
        Object stringProc = stringProcessor;
        if (stringProc instanceof Processor) {
            // This call goes through bridge method
            System.out.println("Bridge method ensures polymorphism works");
        }

        System.out.println();
    }
}
