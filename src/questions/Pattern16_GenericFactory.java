package questions;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

class Pattern16_GenericFactory {

    public interface Factory<T> {
        T create();
        Class<T> getType();
    }

    public static class FactoryRegistry {
        private final Map<Class<?>, Factory<?>> factories = new ConcurrentHashMap<>();

        @SuppressWarnings("unchecked")
        public <T> void register(Class<T> type, Factory<T> factory) {
            factories.put(type, factory);
        }

        @SuppressWarnings("unchecked")
        public <T> T create(Class<T> type) {
            Factory<T> factory = (Factory<T>) factories.get(type);
            if (factory == null) {
                throw new IllegalArgumentException("No factory registered for " + type);
            }
            return factory.create();
        }

        @SuppressWarnings("unchecked")
        public <T> Factory<T> getFactory(Class<T> type) {
            return (Factory<T>) factories.get(type);
        }

        public Set<Class<?>> getRegisteredTypes() {
            return new HashSet<>(factories.keySet());
        }
    }

    // Example factories
    public static class StringFactory implements Factory<String> {
        private final String prefix;

        public StringFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public String create() {
            return prefix + "_" + System.currentTimeMillis();
        }

        @Override
        public Class<String> getType() {
            return String.class;
        }
    }

    public static class ListFactory<T> implements Factory<T> {
        @Override
        @SuppressWarnings("unchecked")
        public T create() {
            return (T) new ArrayList<>();
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<T> getType() {
            return (Class<T>) List.class;
        }
    }

    public static class ConfigurableFactory<T> implements Factory<T> {
        private final Supplier<T> supplier;
        private final Class<T> type;

        public ConfigurableFactory(Class<T> type, Supplier<T> supplier) {
            this.type = type;
            this.supplier = supplier;
        }

        @Override
        public T create() {
            return supplier.get();
        }

        @Override
        public Class<T> getType() {
            return type;
        }
    }

    public static void testPattern() {
        System.out.println("=== PATTERN 16: Generic Factory ===");

        FactoryRegistry registry = new FactoryRegistry();

        // Register different factories
        registry.register(String.class, new StringFactory("ID"));
        registry.register(List.class, new ListFactory<>());
        registry.register(Integer.class, new ConfigurableFactory<>(Integer.class, () -> (int)(Math.random() * 100)));
        registry.register(LocalDateTime.class, new ConfigurableFactory<>(LocalDateTime.class, LocalDateTime::now));

        // Create instances using factories
        String id1 = registry.create(String.class);
        String id2 = registry.create(String.class);
        System.out.println("Generated IDs: " + id1 + ", " + id2);

        List<?> list = registry.create(List.class);
        System.out.println("Created list: " + list + " (type: " + list.getClass().getSimpleName() + ")");

        Integer randomNumber = registry.create(Integer.class);
        System.out.println("Random number: " + randomNumber);

        LocalDateTime now = registry.create(LocalDateTime.class);
        System.out.println("Current time: " + now);

        // Show registered types
        System.out.println("Registered types: " + registry.getRegisteredTypes());

        // Get factory and create multiple instances
        Factory<String> stringFactory = registry.getFactory(String.class);
        System.out.println("Multiple IDs: " + stringFactory.create() + ", " + stringFactory.create());

        System.out.println();
    }
}