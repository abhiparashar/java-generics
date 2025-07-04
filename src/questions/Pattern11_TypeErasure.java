package questions;

public class Pattern11_TypeErasure {
        public static class GenericClass<T>{
            private final T value;
            public GenericClass(T val){
                this.value = val;
            }
            public T getValue(){
                return value;
            }

            public Class <?> getValueClass(){
                return value != null ? value.getClass() : null;
            }

            @Override
            public String toString() {
                return "GenericClass[" + value + "]";
            }
        }

        public static void main(String[] args){
            System.out.println("=== PATTERN 11: Type Erasure ===");

            GenericClass<String> stringGen = new GenericClass<>("Hello");
            GenericClass<Integer> intGen = new GenericClass<>(42);

            // At runtime, both have same class
            System.out.println("String generic class: " + stringGen.getClass());
            System.out.println("Integer generic class: " + intGen.getClass());
            System.out.println("Same class? " + (stringGen.getClass() == intGen.getClass()));

            // But values have different types
            System.out.println("String value class: " + stringGen.getValueClass());
            System.out.println("Integer value class: " + intGen.getValueClass());

            // Can't create generic arrays
            // GenericClass<String>[] array = new GenericClass<String>[10]; // Error!

            // But can create array of raw type
            @SuppressWarnings("unchecked")
            GenericClass<String>[] array = new GenericClass[10];
            array[0] = stringGen;
            System.out.println("Array element: " + array[0]);

            // instanceof with wildcards works
            System.out.println("Is GenericClass: " + (stringGen instanceof GenericClass<?>));

            System.out.println();
        }
}
