package practise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class Q05_MultipleTypeParameters {
    public static class Triple<A,B, C>{
        private final A first;
        private final B second;
        private final C third;
        //Basic Usage
        public Triple(A first, B second, C third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }

        public A getFirst(){
            return first;
        }

        public B getSecond() {
            return second;
        }

        public C getThird() {
            return third;
        }

        // Factory method
        public static<A,B,C> Triple<A,B,C> of(A first, B second, C third){
            return new Triple<>(first, second, third);
        }

        // Immutable update methods
        public Triple<A,B,C>withFirst(A newFirst){
            return new Triple<>(newFirst, second, third);
        }

        public Triple<A,B,C>withSecond(B newSecond){
            return new Triple<>(first,newSecond, third);
        }

        public Triple<A,B,C>withThird(C newThird){
            return new Triple<>(first,second, newThird);
        }

        // Transformation methods
        public <D> Triple<D,B, C>mapFirst(Function<A,D>mapper){
            return new Triple<>(mapper.apply(first),second, third);
        }

        public <D> Triple<A,D,C>mapSecond(Function<B,D>mapper){
            return new Triple<>(first, mapper.apply(second),third );
        }

        public <D> Triple<A,B,D>mapThird(Function<C,D>mapper){
            return new Triple<>(first, second,mapper.apply(third) );
        }

        // Utility methods
        public boolean hasNull(){
           return first==null|| second==null || third==null;
        }

        public boolean isComplete(){
            return first!= null && second != null && third != null;
        }

        public int countNonNull(){
            int count = 0;
            if (first != null) count++;
            if(second != null) count++;
            if(third != null) count++;
            return count;
        }

        //Cosnumer
        @FunctionalInterface
        public interface TriConsumer<A,B,C>{
            void accept(A first, B second, C third);
        }

        //Predicate
        public interface TriPredicate<A,B,C>{
            Boolean test(A first, B second, C third);
        }

        // Functional operations
        public void forEach(TriConsumer<A,B,C> action){
            action.accept(first,second,third);
        }

        public boolean test(TriPredicate<A,B,C>predicate){
           return predicate.test(first,second,third);
        }

        // Conversions
        public Object[] toArray(){
            return new Object[]{first, second, third};
        }

        public List<Object>toList(){
            return Arrays.asList(first, second, third);
        }

    }
    public static void main(String[] args){
        System.out.println("=== Q5: Multiple Type Parameters ===");

        // Basic usage
        Triple<String, Integer, Boolean> person = new Triple<>("Alice", 30, true);
        System.out.println("Person: " + person);
        System.out.println("Name: " + person.getFirst());
        System.out.println("Age: " + person.getSecond());
        System.out.println("Active: " + person.getThird());

        // Factory method
        Triple<String, String, String> colors = Triple.of("Red", "Green", "Blue");
        System.out.println("Colors: " + colors);

        // Immutable updates
        Triple<String, Integer, Boolean> updatedPerson = person.withSecond(31);
        System.out.println("Original: " + person);
        System.out.println("Updated age: " + updatedPerson);

        // Transformations
        System.out.println("\n--- Transformations ---");
        Triple<String, String, Boolean> personWithStringAge = person.mapSecond(age -> age + " years old");
        System.out.println("String age: " + personWithStringAge);

        Triple<String, Integer, String> personWithStatus = person.mapThird(active -> active ? "Active" : "Inactive");
        System.out.println("With status: " + personWithStatus);

        // Utility methods
        System.out.println("\n--- Utility Methods ---");
        System.out.println("Has null: " + person.hasNull());
        System.out.println("Is complete: " + person.isComplete());
        System.out.println("Non-null count: " + person.countNonNull());

        Triple<String, Integer, String> withNull = Triple.of("Test", null, "Value");
        System.out.println("With null - complete: " + withNull.isComplete());
        System.out.println("With null - count: " + withNull.countNonNull());

        // Functional operations
        System.out.println("\n--- Functional Operations ---");
        person.forEach((name, age, active) ->
                System.out.printf("Processing: %s is %d years old and %s%n",
                        name, age, active ? "active" : "inactive"));

        boolean isAdult = person.test((name, age, active) -> age >= 18);
        System.out.println("Is adult: " + isAdult);

        // Conversions
        System.out.println("\n--- Conversions ---");
        System.out.println("As array: " + Arrays.toString(person.toArray()));
        System.out.println("As list: " + person.toList());

        // Working with collections
        List<Triple<String, Integer, String>> employees = Arrays.asList(
                Triple.of("Alice", 85, "A"),
                Triple.of("Bob", 92, "A+"),
                Triple.of("Charlie", 78, "B+")
        );

        System.out.println("\n--- Employee Records ---");
        employees.forEach(emp ->
                System.out.printf("%s: %d (%s)%n",
                        emp.getFirst(), emp.getSecond(), emp.getThird()));

        System.out.println("✅ Q5 Completed!\n");
    }
}
