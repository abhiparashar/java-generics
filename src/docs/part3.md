# Java Generics Mastery - Part 3: Generic Methods & Type Inference

## Why Generic Methods Matter

Sometimes you need a single method to work with different types, but the class itself isn't generic. Spring is full of utility methods like:

```java
// From Spring's BeanUtils
public static <T> T instantiateClass(Class<T> clazz)

// From Collections
public static <T> List<T> emptyList()

// From Optional
public static <T> Optional<T> ofNullable(T value)
```

These are **generic methods** - they have their own type parameters independent of any class.

---

## Chapter 1: Generic Method Syntax

### The Anatomy

```java
public <T> T doSomething(T input) {
//     ↑  ↑      ↑
//     │  │      └── Parameter using T
//     │  └── Return type using T
//     └── Type parameter declaration (MUST come before return type)
    return input;
}
```

### Placement of Type Parameters

```java
// ✓ Correct - type parameter before return type
public <T> T process(T item) { ... }

// ✓ Correct - with modifiers, type parameter after modifiers
public static <T> T create(Class<T> type) { ... }

// ✗ Wrong - type parameter cannot come after return type
public T <T> process(T item) { ... }  // Compile error!
```

### Full Example

```java
public class Utilities {
    
    // Instance generic method
    public <T> T firstNonNull(T first, T second) {
        return first != null ? first : second;
    }
    
    // Static generic method
    public static <T> List<T> listOf(T... items) {
        return new ArrayList<>(Arrays.asList(items));
    }
    
    // Multiple type parameters
    public <K, V> Map<K, V> createMap(K key, V value) {
        Map<K, V> map = new HashMap<>();
        map.put(key, value);
        return map;
    }
}
```

---

## Chapter 2: Generic Methods in Non-Generic Classes

This is extremely common in utility classes:

```java
public class CollectionUtils {  // Not a generic class
    
    // Generic method to find first matching element
    public static <T> Optional<T> findFirst(Collection<T> collection, Predicate<T> predicate) {
        for (T item : collection) {
            if (predicate.test(item)) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }
    
    // Generic method to transform a collection
    public static <T, R> List<R> map(Collection<T> source, Function<T, R> mapper) {
        List<R> result = new ArrayList<>();
        for (T item : source) {
            result.add(mapper.apply(item));
        }
        return result;
    }
    
    // Generic method with multiple bounds (we'll cover bounds in Part 4)
    public static <T extends Comparable<T>> T max(Collection<T> collection) {
        if (collection.isEmpty()) {
            throw new IllegalArgumentException("Collection is empty");
        }
        T max = null;
        for (T item : collection) {
            if (max == null || item.compareTo(max) > 0) {
                max = item;
            }
        }
        return max;
    }
}
```

### Why Not Make the Class Generic?

You could write:

```java
public class CollectionUtils<T> {
    public Optional<T> findFirst(Collection<T> collection, Predicate<T> predicate) { ... }
}
```

But then you'd need: `new CollectionUtils<String>().findFirst(strings, s -> s.isEmpty())`

With a static generic method: `CollectionUtils.findFirst(strings, s -> s.isEmpty())`

**Generic methods are more flexible** - each call can have different types.

---

## Chapter 3: Type Inference - How Java Figures It Out

### The Compiler's Job

When you call a generic method, you rarely specify the type explicitly. The compiler **infers** it:

```java
List<String> names = Arrays.asList("Alice", "Bob");

// You write:
Optional<String> first = CollectionUtils.findFirst(names, s -> s.startsWith("A"));

// Compiler figures out:
// - Collection<T> receives List<String>, so T = String
// - Predicate<T> must be Predicate<String>
// - Return type Optional<T> becomes Optional<String>
```

### Inference Sources

The compiler uses multiple sources to infer types:

```java
public static <T> T identity(T input) {
    return input;
}

// Source 1: Method arguments
String s = identity("hello");  // T inferred from "hello" → String

// Source 2: Assignment target
Object obj = identity("hello");  // T could be String (from argument)

// Source 3: Explicit specification (when needed)
Number n = Utilities.<Number>identity(42);  // T explicitly set to Number
```

### When Inference Fails

Sometimes the compiler needs help:

```java
public static <T> List<T> emptyList() {
    return new ArrayList<>();
}

// ✗ Fails - no way to infer T
List<String> list = emptyList();  // Actually works in modern Java!

// In older Java or complex cases, you might need:
List<String> list = Collections.<String>emptyList();
//                             ↑
//                    Explicit type argument
```

### The Diamond Operator and Inference

```java
// These are equivalent:
List<String> list1 = new ArrayList<String>();
List<String> list2 = new ArrayList<>();  // Diamond infers String

// But diamond only works with 'new'
List<String> list3 = Collections.emptyList();  // No diamond - method inference
```

---

## Chapter 4: Generic Methods in Generic Classes

A class can have both class-level and method-level type parameters:

```java
public class Container<T> {
    private T item;
    
    // Method uses ONLY the class's type parameter
    public T getItem() {
        return item;
    }
    
    // Method has its OWN type parameter R, plus uses class's T
    public <R> R transform(Function<T, R> transformer) {
        return transformer.apply(item);
    }
    
    // Method's type parameter SHADOWS class's (don't do this!)
    public <T> T confusing(T other) {  // This T is different from the class's T!
        return other;
    }
}
```

### Spring Example: ResponseEntity Builder

```java
public class ResponseEntity<T> {
    
    // Class type parameter T is the body type
    private final T body;
    
    // Static method with its own type parameter
    public static <T> ResponseEntity<T> ok(T body) {
        return new ResponseEntity<>(body, HttpStatus.OK);
    }
    
    // Another static method - B is independent of class's T
    public static <B> ResponseEntity<B> status(HttpStatus status, B body) {
        return new ResponseEntity<>(body, status);
    }
}

// Usage:
ResponseEntity<User> response1 = ResponseEntity.ok(user);
ResponseEntity<String> response2 = ResponseEntity.ok("Hello");
// Each call has its own type!
```

---

## Chapter 5: Type Inference Deep Dive

### Target Type Inference (Java 8+)

Java can infer types from what you're assigning to:

```java
// Method:
public static <T> List<T> newList() {
    return new ArrayList<>();
}

// Target type inference - T inferred from left side
List<String> strings = newList();  // T = String
List<Integer> numbers = newList(); // T = Integer

// Works in method arguments too!
void process(List<String> items) { }

process(newList());  // T = String, inferred from parameter type
```

### Inference in Lambda Expressions

```java
List<String> names = Arrays.asList("Alice", "Bob", "Charlie");

// Compiler infers types through the chain:
List<Integer> lengths = names.stream()
    .map(s -> s.length())      // s inferred as String, lambda returns Integer
    .collect(Collectors.toList());  // Collector type inferred from stream

// Equivalent to:
List<Integer> lengths = names.stream()
    .map((String s) -> { return s.length(); })
    .collect(Collectors.<Integer>toList());
```

### When to Specify Types Explicitly

```java
// 1. Ambiguous situations
Object result = Collections.emptyList();  // T = Object (might not be what you want)
List<String> result = Collections.<String>emptyList();  // T = String explicitly

// 2. Complex nested generics
Map<String, List<Integer>> map = new HashMap<>();
// Sometimes clearer to be explicit

// 3. When the compiler gives up
public <T> void process(T item) { }
process(null);  // What's T? Compiler assumes Object
this.<String>process(null);  // T = String explicitly
```

---

## Chapter 6: The Class<T> Pattern

This is **everywhere** in Spring. It solves a fundamental problem: how do you create instances of T when you don't know what T is?

### The Problem

```java
public class Factory<T> {
    public T create() {
        return new T();  // ✗ Won't compile! Can't instantiate type parameter
    }
}
```

### The Solution: Pass Class<T>

```java
public class Factory<T> {
    private final Class<T> type;
    
    public Factory(Class<T> type) {
        this.type = type;
    }
    
    public T create() throws Exception {
        return type.getDeclaredConstructor().newInstance();
    }
}

// Usage:
Factory<User> userFactory = new Factory<>(User.class);
User user = userFactory.create();
```

### Spring's BeanUtils Pattern

```java
public class BeanUtils {
    
    // Create an instance of any class
    public static <T> T instantiateClass(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new BeanInstantiationException(clazz, "Failed to instantiate", e);
        }
    }
    
    // With constructor arguments
    public static <T> T instantiateClass(
            Class<T> clazz, Class<?>... parameterTypes) {
        // Implementation
    }
}

// Usage in Spring:
User user = BeanUtils.instantiateClass(User.class);
```

### Why Class<T> is Type-Safe

```java
Class<String> stringClass = String.class;
Class<Integer> intClass = Integer.class;

// The generic type prevents mistakes:
stringClass.cast("hello");  // ✓ Returns String
stringClass.cast(42);       // ✗ ClassCastException at runtime

// But you can't accidentally mix them:
Class<String> wrong = Integer.class;  // ✗ Compile error!
```

---

## Chapter 7: Common Patterns in Spring

### Pattern 1: Type-Safe Factory Methods

```java
public class ResponseFactory {
    
    public static <T> ResponseEntity<T> success(T data) {
        return ResponseEntity.ok(data);
    }
    
    public static <T> ResponseEntity<T> created(T data, String location) {
        return ResponseEntity
            .created(URI.create(location))
            .body(data);
    }
    
    public static <T> ResponseEntity<List<T>> list(List<T> items) {
        return ResponseEntity.ok(items);
    }
}

// Usage - type flows through:
ResponseEntity<User> response = ResponseFactory.success(user);
ResponseEntity<List<Product>> products = ResponseFactory.list(productList);
```

### Pattern 2: Generic Converter

```java
public interface Converter<S, T> {
    T convert(S source);
}

// Concrete implementation
public class StringToIntegerConverter implements Converter<String, Integer> {
    @Override
    public Integer convert(String source) {
        return Integer.valueOf(source);
    }
}

// Generic wrapper for null-safety
public class NullSafeConverter<S, T> implements Converter<S, T> {
    private final Converter<S, T> delegate;
    private final T defaultValue;
    
    public NullSafeConverter(Converter<S, T> delegate, T defaultValue) {
        this.delegate = delegate;
        this.defaultValue = defaultValue;
    }
    
    @Override
    public T convert(S source) {
        if (source == null) {
            return defaultValue;
        }
        return delegate.convert(source);
    }
}
```

### Pattern 3: Generic Event Handling

```java
public interface ApplicationEvent { }

public interface ApplicationListener<E extends ApplicationEvent> {
    void onApplicationEvent(E event);
}

// Concrete event
public class UserCreatedEvent implements ApplicationEvent {
    private final User user;
    
    public UserCreatedEvent(User user) {
        this.user = user;
    }
    
    public User getUser() { return user; }
}

// Concrete listener
@Component
public class UserCreatedListener implements ApplicationListener<UserCreatedEvent> {
    
    @Override
    public void onApplicationEvent(UserCreatedEvent event) {
        System.out.println("User created: " + event.getUser().getName());
    }
}
```

---

## Chapter 8: Method References and Generics

Method references work seamlessly with generic methods:

```java
// Generic method
public static <T> T identity(T item) {
    return item;
}

// Using with streams
List<String> names = Arrays.asList("Alice", "Bob");
List<String> copy = names.stream()
    .map(Utilities::identity)  // Method reference to generic method
    .collect(Collectors.toList());

// Constructor references
public static <T> T create(Supplier<T> supplier) {
    return supplier.get();
}

User user = create(User::new);  // User's no-arg constructor
ArrayList<String> list = create(ArrayList::new);
```

---

## Chapter 9: Exercises

### Exercise 1: Write a Generic swap Method

```java
public class ArrayUtils {
    // Swap two elements in an array
    public static <T> void swap(T[] array, int i, int j) {
        // Your implementation
    }
}
```

### Exercise 2: Create a Generic Cache with Factory

```java
public class Cache<K, V> {
    private final Map<K, V> store = new HashMap<>();
    private final Function<K, V> factory;
    
    public Cache(Function<K, V> factory) {
        this.factory = factory;
    }
    
    // Get value, creating if absent
    public V get(K key) {
        // Your implementation using computeIfAbsent
    }
    
    // Static factory method
    public static <K, V> Cache<K, V> create(Function<K, V> factory) {
        // Your implementation
    }
}
```

### Exercise 3: Trace the Type Inference

What types are inferred at each step?

```java
List<String> names = Arrays.asList("Alice", "Bob");
Map<String, Integer> lengths = names.stream()
    .collect(Collectors.toMap(
        Function.identity(),     // What's the type?
        String::length           // What's the type?
    ));
```

### Exercise 4: Fix the Compilation Error

```java
public class Broken {
    public static <T> List<T> merge(List<T> first, List<T> second) {
        List<T> result = new ArrayList<>(first);
        result.addAll(second);
        return result;
    }
}

// This fails to compile - why? How to fix?
List<Number> numbers = Broken.merge(
    Arrays.asList(1, 2, 3),
    Arrays.asList(4.0, 5.0)
);
```

---

## Key Takeaways

1. **Generic methods have their own type parameters** - independent of class type parameters
2. **Type parameter declaration comes before return type** - `public <T> T method()`
3. **Type inference usually works** - compiler figures out types from context
4. **Class<T> pattern** - essential for creating instances when you don't know T at compile time
5. **Static generic methods** - common in utility classes, each call can have different types
6. **Target type inference** - Java infers from assignment target and method parameters

---

## What's Next

In Part 4, we'll explore:
- **Bounded Type Parameters** - `<T extends Number>`, `<T extends Comparable<T>>`
- **Multiple Bounds** - `<T extends Serializable & Comparable<T>>`
- **When to Use Bounds** - Accessing methods on type parameters

This is crucial for understanding Spring's constraint mechanisms and validation.