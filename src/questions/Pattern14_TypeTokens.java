package questions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pattern14_TypeTokens {

    public abstract static class TypeToken<T>{
        private final Class<T> type;

        @SuppressWarnings("unchecked")
        protected TypeToken(){
            this.type = (Class<T>)Object.class;
        }

        public Class<T>getType(){
            return type;
        }

        @Override
        public String toString() {
            return "TypeToken[" + type.getSimpleName() + "]";
        }
    }

    public static class GenericContainer<T>{
            private final T value;
            private final TypeToken<T> typeToken;

            public GenericContainer(T value, TypeToken<T>typeToken){
                this.value = value;
                this.typeToken = typeToken;
            }

            public T getValue(){
                return value;
            }

            public Class<T> getValueType(){
                return typeToken.getType();
            }

            @Override
            public String toString(){
                return String.format("Container[value=%s, type=%s]", value, typeToken);
            }
    }

    public static void main(String[] args){
        System.out.println("=== PATTERN 14: Type Tokens ===");

        // Create type tokens for complex types
        TypeToken<String> stringToken = new TypeToken<String>() {};
        TypeToken<List<String>> listToken = new TypeToken<List<String>>() {};
        TypeToken<Map<String, Integer>> mapToken = new TypeToken<Map<String, Integer>>() {};

        // Use type tokens to preserve type information
        GenericContainer<String> stringContainer = new GenericContainer<>("Hello", stringToken);
        System.out.println("String container: " + stringContainer);

        List<String> list = Arrays.asList("A", "B", "C");
        GenericContainer<List<String>> listContainer = new GenericContainer<>(list, listToken);
        System.out.println("List container: " + listContainer);

        Map<String, Integer> map = new HashMap<>();
        map.put("one", 1);
        map.put("two", 2);
        GenericContainer<Map<String, Integer>> mapContainer = new GenericContainer<>(map, mapToken);
        System.out.println("Map container: " + mapContainer);

        // Type tokens help with type-safe operations
        System.out.println("String type: " + stringContainer.getValueType());
        System.out.println("List type: " + listContainer.getValueType());

        System.out.println();
    }
}
