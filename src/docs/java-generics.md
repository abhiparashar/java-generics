# Java Generics: Complete Mastery Guide

## Table of Contents
1. [Introduction and Core Concepts](#introduction)
2. [Type Parameters and Naming Conventions](#type-parameters)
3. [Generic Classes and Interfaces](#generic-classes)
4. [Generic Methods](#generic-methods)
5. [Bounded Type Parameters](#bounded-types)
6. [Wildcards and PECS Principle](#wildcards)
7. [Type Erasure and Bridge Methods](#type-erasure)
8. [Advanced Patterns](#advanced-patterns)
9. [Real-World Use Cases](#real-world-cases)
10. [Best Practices and Common Pitfalls](#best-practices)
11. [Performance Considerations](#performance)
12. [Interview Questions and Scenarios](#interview-prep)

## 1. Introduction and Core Concepts {#introduction}

### What are Generics?
Generics allow you to write classes, interfaces, and methods that operate on objects of various types while providing compile-time type safety. They enable you to catch type errors at compile time rather than runtime.

### Before Generics (Pre-Java 5)
```java
// Raw types - prone to ClassCastException
List list = new ArrayList();
list.add("Hello");
list.add(42);
String str = (String) list.get(1); // Runtime exception!
```

### With Generics
```java
// Type-safe
List<String> list = new ArrayList<>();
list.add("Hello");
// list.add(42); // Compile-time error!
String str = list.get(0); // No casting needed
```

### Key Benefits
- **Type Safety**: Compile-time type checking
- **Elimination of Casts**: No need for explicit casting
- **Code Reusability**: Same code works with different types
- **Better Performance**: No boxing/unboxing overhead in many cases
- **Clearer APIs**: Self-documenting code

## 2. Type Parameters and Naming Conventions {#type-parameters}

### Standard Naming Conventions
- `T` - Type (most common)
- `E` - Element (used by collections)
- `K` - Key (used in maps)
- `V` - Value (used in maps)
- `N` - Number
- `S`, `U`, `V` - 2nd, 3rd, 4th types

### Multiple Type Parameters
```java
public class Pair<T, U> {
    private T first;
    private U second;
    
    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }
    
    public T getFirst() { return first; }
    public U getSecond() { return second; }
}

// Usage
Pair<String, Integer> nameAge = new Pair<>("John", 25);
```

## 3. Generic Classes and Interfaces {#generic-classes}

### Basic Generic Class
```java
public class Box<T> {
    private T content;
    
    public void set(T content) {
        this.content = content;
    }
    
    public T get() {
        return content;
    }
    
    public boolean isEmpty() {
        return content == null;
    }
}

// Usage
Box<String> stringBox = new Box<>();
Box<Integer> intBox = new Box<>();
```

### Generic Interface
```java
public interface Repository<T, ID> {
    void save(T entity);
    T findById(ID id);
    List<T> findAll();
    void delete(ID id);
}

// Implementation
public class UserRepository implements Repository<User, Long> {
    @Override
    public void save(User user) { /* implementation */ }
    
    @Override
    public User findById(Long id) { /* implementation */ }
    
    @Override
    public List<User> findAll() { /* implementation */ }
    
    @Override
    public void delete(Long id) { /* implementation */ }
}
```

### Inheritance with Generics
```java
// Generic superclass
public abstract class Animal<T> {
    protected T characteristic;
    
    public abstract void makeSound(T sound);
}

// Concrete subclass
public class Dog extends Animal<String> {
    @Override
    public void makeSound(String sound) {
        System.out.println("Dog says: " + sound);
    }
}

// Generic subclass
public class Cat<T> extends Animal<T> {
    @Override
    public void makeSound(T sound) {
        System.out.println("Cat says: " + sound);
    }
}
```

## 4. Generic Methods {#generic-methods}

### Static Generic Methods
```java
public class Utility {
    // Generic method in non-generic class
    public static <T> void swap(T[] array, int i, int j) {
        T temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
    
    // Multiple type parameters
    public static <T, U> Pair<T, U> makePair(T first, U second) {
        return new Pair<>(first, second);
    }
    
    // Return type depends on input
    public static <T> T getMiddle(T... elements) {
        return elements[elements.length / 2];
    }
}

// Usage
String[] names = {"Alice", "Bob", "Charlie"};
Utility.swap(names, 0, 2);
Pair<String, Integer> pair = Utility.makePair("Age", 25);
```

### Instance Generic Methods
```java
public class GenericClass<T> {
    private T field;
    
    // Method with different type parameter
    public <U> void process(U item) {
        System.out.println("Processing: " + item);
    }
    
    // Method using class type parameter
    public void setField(T field) {
        this.field = field;
    }
    
    // Method with bounded type parameter
    public <U extends Number> double processNumber(U number) {
        return number.doubleValue() * 2;
    }
}
```

### Generic Constructor
```java
public class GenericConstructor {
    private String data;
    
    public <T> GenericConstructor(T input) {
        this.data = input.toString();
    }
}
```

## 5. Bounded Type Parameters {#bounded-types}

### Upper Bounds (extends)
```java
// T must be Number or its subclass
public class NumberBox<T extends Number> {
    private T number;
    
    public NumberBox(T number) {
        this.number = number;
    }
    
    public double getDoubleValue() {
        return number.doubleValue(); // Can call Number methods
    }
    
    public boolean isPositive() {
        return number.doubleValue() > 0;
    }
}

// Usage
NumberBox<Integer> intBox = new NumberBox<>(42);
NumberBox<Double> doubleBox = new NumberBox<>(3.14);
// NumberBox<String> stringBox = new NumberBox<>("Hello"); // Compile error!
```

### Multiple Bounds
```java
// T must implement both Comparable and Serializable
public class SortableBox<T extends Comparable<T> & Serializable> {
    private List<T> items = new ArrayList<>();
    
    public void add(T item) {
        items.add(item);
    }
    
    public void sort() {
        Collections.sort(items); // Can sort because T implements Comparable
    }
    
    public void serialize() {
        // Can serialize because T implements Serializable
    }
}
```

### Recursive Type Bounds
```java
// Enum pattern
public abstract class Enum<E extends Enum<E>> implements Comparable<E> {
    // Implementation details
}

// Builder pattern
public abstract class Builder<T extends Builder<T>> {
    public abstract T self();
    
    public T setName(String name) {
        // Set name
        return self();
    }
    
    public T setAge(int age) {
        // Set age
        return self();
    }
}

public class PersonBuilder extends Builder<PersonBuilder> {
    @Override
    public PersonBuilder self() {
        return this;
    }
    
    public Person build() {
        return new Person();
    }
}
```

## 6. Wildcards and PECS Principle {#wildcards}

### Unbounded Wildcards (?)
```java
public void printList(List<?> list) {
    for (Object item : list) {
        System.out.println(item);
    }
}

// Works with any type
printList(Arrays.asList("A", "B", "C"));
printList(Arrays.asList(1, 2, 3));
```

### Upper Bounded Wildcards (? extends)
```java
// Producer - can only read, not write
public double sum(List<? extends Number> numbers) {
    double sum = 0.0;
    for (Number num : numbers) {
        sum += num.doubleValue();
    }
    return sum;
}

// Usage
List<Integer> ints = Arrays.asList(1, 2, 3);
List<Double> doubles = Arrays.asList(1.1, 2.2, 3.3);
double intSum = sum(ints);
double doubleSum = sum(doubles);
```

### Lower Bounded Wildcards (? super)
```java
// Consumer - can write, limited reading
public void addNumbers(List<? super Integer> list) {
    list.add(1);
    list.add(2);
    list.add(3);
    // Object obj = list.get(0); // Can only read as Object
}

// Usage
List<Number> numbers = new ArrayList<>();
List<Object> objects = new ArrayList<>();
addNumbers(numbers);
addNumbers(objects);
```

### PECS Principle (Producer Extends, Consumer Super)
```java
public class Collections {
    // Producer: source produces T, so use ? extends T
    public static <T> void copy(List<? super T> dest, List<? extends T> src) {
        for (T item : src) {
            dest.add(item);
        }
    }
}

// Real-world example: Collections.copy()
List<Number> numbers = new ArrayList<>();
List<Integer> integers = Arrays.asList(1, 2, 3);
Collections.copy(numbers, integers); // integers produces, numbers consumes
```

### Wildcard Capture
```java
public class WildcardCapture {
    // Helper method to capture wildcard
    private static <T> void swapHelper(List<T> list, int i, int j) {
        T temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }
    
    // Public method with wildcard
    public static void swap(List<?> list, int i, int j) {
        swapHelper(list, i, j); // Wildcard capture
    }
}
```

## 7. Type Erasure and Bridge Methods {#type-erasure}

### Understanding Type Erasure
```java
// Source code
public class Box<T> {
    private T content;
    public void set(T content) { this.content = content; }
    public T get() { return content; }
}

// After type erasure (conceptual)
public class Box {
    private Object content;
    public void set(Object content) { this.content = content; }
    public Object get() { return content; }
}
```

### Bridge Methods
```java
// Generic interface
interface Processor<T> {
    void process(T item);
}

// Implementation
class StringProcessor implements Processor<String> {
    @Override
    public void process(String item) {
        System.out.println("Processing: " + item);
    }
}

// Compiler generates bridge method:
// public void process(Object item) {
//     process((String) item);
// }
```

### Implications of Type Erasure
```java
public class TypeErasureExamples {
    // Cannot create instances of type parameters
    public <T> void cannotCreate() {
        // T obj = new T(); // Compile error
    }
    
    // Cannot create arrays of parameterized types
    public void arrayLimitations() {
        // List<String>[] arrays = new List<String>[10]; // Compile error
        List<String>[] arrays = new List[10]; // Warning, but works
    }
    
    // Cannot use instanceof with parameterized types
    public void instanceofLimitations(Object obj) {
        // if (obj instanceof List<String>) { } // Compile error
        if (obj instanceof List<?>) { } // This works
    }
    
    // Cannot catch parameterized exceptions
    public void exceptionLimitations() {
        try {
            // Some code
        } catch (Exception e) {
            // Cannot: catch (GenericException<String> e)
        }
    }
}
```

## 8. Advanced Patterns {#advanced-patterns}

### Type Tokens
```java
public class TypeToken<T> {
    private final Class<T> type;
    
    @SuppressWarnings("unchecked")
    protected TypeToken() {
        this.type = (Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[0];
    }
    
    public Class<T> getType() {
        return type;
    }
}

// Usage
TypeToken<List<String>> token = new TypeToken<List<String>>() {};
```

### Self-Bounded Generics
```java
public abstract class SelfBounded<T extends SelfBounded<T>> {
    public abstract T self();
    
    public T method1() {
        // Do something
        return self();
    }
    
    public T method2() {
        // Do something else
        return self();
    }
}

public class Concrete extends SelfBounded<Concrete> {
    @Override
    public Concrete self() {
        return this;
    }
}

// Usage - enables fluent interfaces
Concrete obj = new Concrete();
obj.method1().method2(); // Returns correct type
```

### Generic Factory Pattern
```java
public interface Factory<T> {
    T create();
}

public class FactoryRegistry {
    private Map<Class<?>, Factory<?>> factories = new HashMap<>();
    
    @SuppressWarnings("unchecked")
    public <T> void register(Class<T> type, Factory<T> factory) {
        factories.put(type, factory);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> type) {
        Factory<T> factory = (Factory<T>) factories.get(type);
        return factory != null ? factory.create() : null;
    }
}

// Usage
FactoryRegistry registry = new FactoryRegistry();
registry.register(String.class, () -> "Default String");
registry.register(Integer.class, () -> 42);

String str = registry.create(String.class);
Integer num = registry.create(Integer.class);
```

### Phantom Types
```java
// Phantom type parameter not used in class body
public class Distance<Unit> {
    private final double value;
    
    private Distance(double value) {
        this.value = value;
    }
    
    public static Distance<Meter> meters(double value) {
        return new Distance<>(value);
    }
    
    public static Distance<Foot> feet(double value) {
        return new Distance<>(value);
    }
    
    public double getValue() {
        return value;
    }
    
    // Type-safe conversion
    public Distance<Foot> toFeet() {
        return new Distance<>(value * 3.28084);
    }
}

// Phantom type markers
class Meter {}
class Foot {}

// Usage - prevents mixing units accidentally
Distance<Meter> meters = Distance.meters(100);
Distance<Foot> feet = meters.toFeet();
```

## 9. Real-World Use Cases {#real-world-cases}

### 1. Repository Pattern with JPA
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByLastName(String lastName);
    Optional<User> findByEmail(String email);
}

@Service
public class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User createUser(User user) {
        return userRepository.save(user);
    }
    
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
```

### 2. Builder Pattern with Generics
```java
public class HttpRequest {
    private final String url;
    private final String method;
    private final Map<String, String> headers;
    private final String body;
    
    private HttpRequest(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.headers = builder.headers;
        this.body = builder.body;
    }
    
    public static class Builder {
        private String url;
        private String method = "GET";
        private Map<String, String> headers = new HashMap<>();
        private String body;
        
        public Builder url(String url) {
            this.url = url;
            return this;
        }
        
        public Builder method(String method) {
            this.method = method;
            return this;
        }
        
        public Builder header(String key, String value) {
            headers.put(key, value);
            return this;
        }
        
        public Builder body(String body) {
            this.body = body;
            return this;
        }
        
        public HttpRequest build() {
            return new HttpRequest(this);
        }
    }
}

// Usage
HttpRequest request = new HttpRequest.Builder()
    .url("https://api.example.com/users")
    .method("POST")
    .header("Content-Type", "application/json")
    .body("{\"name\":\"John\"}")
    .build();
```

### 3. Event System with Generics
```java
public interface EventListener<T extends Event> {
    void onEvent(T event);
}

public abstract class Event {
    private final LocalDateTime timestamp;
    
    protected Event() {
        this.timestamp = LocalDateTime.now();
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

public class UserCreatedEvent extends Event {
    private final User user;
    
    public UserCreatedEvent(User user) {
        this.user = user;
    }
    
    public User getUser() {
        return user;
    }
}

@Component
public class EventBus {
    private final Map<Class<? extends Event>, List<EventListener<? extends Event>>> listeners = new HashMap<>();
    
    @SuppressWarnings("unchecked")
    public <T extends Event> void subscribe(Class<T> eventType, EventListener<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Event> void publish(T event) {
        List<EventListener<? extends Event>> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (EventListener<? extends Event> listener : eventListeners) {
                ((EventListener<T>) listener).onEvent(event);
            }
        }
    }
}

// Usage
@Component
public class UserEventHandler implements EventListener<UserCreatedEvent> {
    @Override
    public void onEvent(UserCreatedEvent event) {
        System.out.println("User created: " + event.getUser().getName());
    }
}
```

### 4. Generic DAO Pattern
```java
public abstract class GenericDAO<T, ID> {
    private final Class<T> entityClass;
    
    @SuppressWarnings("unchecked")
    public GenericDAO() {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[0];
    }
    
    public void save(T entity) {
        // Implementation using entityClass
    }
    
    public T findById(ID id) {
        // Implementation using entityClass
        return null;
    }
    
    public List<T> findAll() {
        // Implementation using entityClass
        return new ArrayList<>();
    }
    
    public void delete(ID id) {
        // Implementation using entityClass
    }
    
    protected Class<T> getEntityClass() {
        return entityClass;
    }
}

public class UserDAO extends GenericDAO<User, Long> {
    public List<User> findByLastName(String lastName) {
        // Specific query for User
        return new ArrayList<>();
    }
}
```

### 5. Functional Programming with Generics
```java
@FunctionalInterface
public interface Mapper<T, R> {
    R map(T input);
}

@FunctionalInterface
public interface Predicate<T> {
    boolean test(T input);
}

public class Stream<T> {
    private final List<T> items;
    
    public Stream(List<T> items) {
        this.items = new ArrayList<>(items);
    }
    
    public <R> Stream<R> map(Mapper<T, R> mapper) {
        List<R> mapped = new ArrayList<>();
        for (T item : items) {
            mapped.add(mapper.map(item));
        }
        return new Stream<>(mapped);
    }
    
    public Stream<T> filter(Predicate<T> predicate) {
        List<T> filtered = new ArrayList<>();
        for (T item : items) {
            if (predicate.test(item)) {
                filtered.add(item);
            }
        }
        return new Stream<>(filtered);
    }
    
    public List<T> collect() {
        return new ArrayList<>(items);
    }
    
    public static <T> Stream<T> of(T... items) {
        return new Stream<>(Arrays.asList(items));
    }
}

// Usage
List<String> names = Stream.of("Alice", "Bob", "Charlie")
    .filter(name -> name.length() > 3)
    .map(String::toUpperCase)
    .collect();
```

### 6. Generic Cache Implementation
```java
public class Cache<K, V> {
    private final Map<K, CacheEntry<V>> cache = new ConcurrentHashMap<>();
    private final long ttlMillis;
    
    public Cache(long ttlMillis) {
        this.ttlMillis = ttlMillis;
    }
    
    public void put(K key, V value) {
        cache.put(key, new CacheEntry<>(value, System.currentTimeMillis() + ttlMillis));
    }
    
    public Optional<V> get(K key) {
        CacheEntry<V> entry = cache.get(key);
        if (entry != null && entry.isValid()) {
            return Optional.of(entry.getValue());
        } else {
            cache.remove(key);
            return Optional.empty();
        }
    }
    
    public void remove(K key) {
        cache.remove(key);
    }
    
    public void clear() {
        cache.clear();
    }
    
    private static class CacheEntry<V> {
        private final V value;
        private final long expiry;
        
        public CacheEntry(V value, long expiry) {
            this.value = value;
            this.expiry = expiry;
        }
        
        public V getValue() {
            return value;
        }
        
        public boolean isValid() {
            return System.currentTimeMillis() < expiry;
        }
    }
}

// Usage
Cache<String, User> userCache = new Cache<>(TimeUnit.MINUTES.toMillis(10));
userCache.put("user123", new User("John", "john@example.com"));
Optional<User> user = userCache.get("user123");
```

## 10. Best Practices and Common Pitfalls {#best-practices}

### Best Practices

#### 1. Use Bounded Wildcards for API Flexibility
```java
// Good: Flexible API
public void processItems(List<? extends Item> items) {
    for (Item item : items) {
        process(item);
    }
}

// Less flexible
public void processItems(List<Item> items) {
    // Won't accept List<SubItem>
}
```

#### 2. Use Generic Methods When Possible
```java
// Good: Generic method
public static <T> void swap(T[] array, int i, int j) {
    T temp = array[i];
    array[i] = array[j];
    array[j] = temp;
}

// Not as good: Multiple overloaded methods
public static void swap(String[] array, int i, int j) { }
public static void swap(Integer[] array, int i, int j) { }
// ... many more overloads
```

#### 3. Prefer Lists to Arrays with Generics
```java
// Good: Type-safe
List<String> strings = new ArrayList<>();

// Problematic: Generic array creation issues
// String[] strings = new String[10]; // This is fine
// List<String>[] stringLists = new List<String>[10]; // Compile error
```

#### 4. Use Descriptive Type Parameter Names
```java
// Good: Clear intent
public class Repository<Entity, PrimaryKey> {
    // ...
}

// Less clear
public class Repository<T, U> {
    // ...
}
```

### Common Pitfalls

#### 1. Raw Types
```java
// Bad: Raw type
List list = new ArrayList();
list.add("string");
list.add(42);

// Good: Parameterized type
List<String> list = new ArrayList<>();
```

#### 2. Unnecessary Wildcards
```java
// Bad: Unnecessary wildcard
public void method(List<?> list) {
    // Can only read as Object
}

// Good: Use specific type when possible
public <T> void method(List<T> list) {
    // Can work with T
}
```

#### 3. Confusing ? extends and ? super
```java
// Remember PECS: Producer Extends, Consumer Super

// Producer: generates/provides data
List<? extends Number> numbers = getNumbers(); // Can read as Number
// numbers.add(1); // Compile error - can't add

// Consumer: accepts/consumes data
List<? super Integer> ints = new ArrayList<Number>();
ints.add(1); // Can add Integer
// Integer i = ints.get(0); // Compile error - can only read as Object
```

#### 4. Type Erasure Confusion
```java
// Bad: Won't work as expected
public class GenericArray<T> {
    private T[] array;
    
    public GenericArray(int size) {
        // array = new T[size]; // Compile error
        array = (T[]) new Object[size]; // Warning, potential ClassCastException
    }
}

// Better: Use List instead
public class GenericContainer<T> {
    private List<T> items = new ArrayList<>();
    
    public void add(T item) {
        items.add(item);
    }
    
    public T get(int index) {
        return items.get(index);
    }
}
```

## 11. Performance Considerations {#performance}

### Boxing and Unboxing
```java
// Potential performance issue
List<Integer> numbers = new ArrayList<>();
for (int i = 0; i < 1000000; i++) {
    numbers.add(i); // Autoboxing: int -> Integer
}

// Better for primitive-heavy operations
int[] numbers = new int[1000000];
for (int i = 0; i < 1000000; i++) {
    numbers[i] = i; // No boxing
}

// Or use specialized collections
TIntList numbers = new TIntArrayList(); // From GNU Trove
```

### Generic Method Overhead
```java
// Slight overhead due to type checking and casting
public static <T extends Comparable<T>> T max(T a, T b) {
    return a.compareTo(b) > 0 ? a : b;
}

// Direct method might be faster for specific types
public static int max(int a, int b) {
    return a > b ? a : b;
}
```

### Memory Considerations
```java
// Generic types don't increase memory per instance
// but may have slight overhead due to type information

// Same memory footprint
List<String> strings1 = new ArrayList<>();
List<Integer> integers1 = new ArrayList<>();

// Type information stored at class level, not instance level
```

## 12. Interview Questions and Scenarios {#interview-prep}

### Common Interview Questions

#### Q1: What is type erasure and why does it exist?
**Answer:** Type erasure is the process by which generic type information is removed during compilation. It exists for backward compatibility with pre-generic Java code. At runtime, `List<String>` and `List<Integer>` are just `List`.

#### Q2: Explain PECS principle
**Answer:** PECS stands for "Producer Extends, Consumer Super":
- Use `? extends T` when you're reading from a structure (producer of T)
- Use `? super T` when you're writing to a structure (consumer of T)

#### Q3: Why can't you create a generic array?
**Answer:** Due to type erasure and array covariance, generic arrays would be unsafe:
```java
// This would be unsafe if allowed
List<String>[] stringLists = new List<String>[10]; // Compile error
Object[] objects = stringLists; // Arrays are covariant
objects[0] = new ArrayList<Integer>(); // Would be allowed at runtime
List<String> strings = stringLists[0]; // ClassCastException!
```

#### Q4: What are bridge methods?
**Answer:** Bridge methods are synthetic methods generated by the compiler to maintain polymorphism after type erasure. They ensure that method overriding works correctly.

### Advanced Scenarios

#### Scenario 1: Generic Method Resolution
```java
public class MethodResolution {
    public static <T> void method(T t) {
        System.out.println("Generic method: " + t);
    }
    
    public static void method(String s) {
        System.out.println("Specific method: " + s);
    }
    
    public static void main(String[] args) {
        method("Hello"); // Calls specific method
        MethodResolution.<String>method("Hello"); // Calls generic method
    }
}
```

#### Scenario 2: Wildcard Capture
```java
public class WildcardCapture {
    // This won't compile
    public static void badSwap(List<?> list, int i, int j) {
        // Object temp = list.get(i);
        // list.set(i, list.get(j)); // Compile error
        // list.set(j, temp);
    }
    
    // This works - wildcard capture
    public static void swap(List<?> list, int i, int j) {
        swapHelper(list, i, j);
    }
    
    private static <T> void swapHelper(List<T> list, int i, int j) {
        T temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }
}
```

#### Scenario 3: Generic Singleton Pattern
```java
public class GenericSingleton<T> {
    private static final GenericSingleton<?> INSTANCE = new GenericSingleton<>();
    
    private GenericSingleton() {}
    
    @SuppressWarnings("unchecked")
    public static <T> GenericSingleton<T> getInstance() {
        return (GenericSingleton<T>) INSTANCE;
    }
}
```

#### Scenario 4: Complex Bounded Types
```java
public class ComplexBounds {
    // Method that accepts types that are both Comparable and Serializable
    public static <T extends Comparable<T> & Serializable> void processComparableSerializable(List<T> items) {
        Collections.sort(items); // Can sort because of Comparable
        // Can serialize because of Serializable
    }
    
    // Recursive bounds with interfaces
    public interface Builder<T extends Builder<T>> {
        T name(String name);
        T age(int age);
    }
    
    public static class PersonBuilder implements Builder<PersonBuilder> {
        private String name;
        private int age;
        
        @Override
        public PersonBuilder name(String name) {
            this.name = name;
            return this;
        }
        
        @Override
        public PersonBuilder age(int age) {
            this.age = age;
            return this;
        }
        
        public Person build() {
            return new Person(name, age);
        }
    }
}
```

## 13. Advanced Generic Patterns in Modern Java

### Sealed Classes with Generics (Java 17+)
```java
public sealed interface Result<T> permits Success, Error {
    // Common methods
}

public final class Success<T> implements Result<T> {
    private final T value;
    
    public Success(T value) {
        this.value = value;
    }
    
    public T getValue() {
        return value;
    }
}

public final class Error<T> implements Result<T> {
    private final String message;
    
    public Error(String message) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
}

// Usage with pattern matching
public static <T> void handleResult(Result<T> result) {
    switch (result) {
        case Success<T> success -> System.out.println("Success: " + success.getValue());
        case Error<T> error -> System.out.println("Error: " + error.getMessage());
    }
}
```

### Record Classes with Generics (Java 14+)
```java
public record Pair<T, U>(T first, U second) {
    // Compact constructor
    public Pair {
        Objects.requireNonNull(first);
        Objects.requireNonNull(second);
    }
    
    // Additional methods
    public <V> Pair<V, U> mapFirst(Function<T, V> mapper) {
        return new Pair<>(mapper.apply(first), second);
    }
    
    public <V> Pair<T, V> mapSecond(Function<U, V> mapper) {
        return new Pair<>(first, mapper.apply(second));
    }
}

// Usage
Pair<String, Integer> nameAge = new Pair<>("John", 25);
Pair<String, String> nameAgeStr = nameAge.mapSecond(String::valueOf);
```

### Generic Enums
```java
public enum Operation {
    PLUS("+") {
        @Override
        public <T extends Number> double apply(T x, T y) {
            return x.doubleValue() + y.doubleValue();
        }
    },
    MINUS("-") {
        @Override
        public <T extends Number> double apply(T x, T y) {
            return x.doubleValue() - y.doubleValue();
        }
    },
    MULTIPLY("*") {
        @Override
        public <T extends Number> double apply(T x, T y) {
            return x.doubleValue() * y.doubleValue();
        }
    },
    DIVIDE("/") {
        @Override
        public <T extends Number> double apply(T x, T y) {
            return x.doubleValue() / y.doubleValue();
        }
    };
    
    private final String symbol;
    
    Operation(String symbol) {
        this.symbol = symbol;
    }
    
    public abstract <T extends Number> double apply(T x, T y);
    
    @Override
    public String toString() {
        return symbol;
    }
}

// Usage
double result = Operation.PLUS.apply(10, 5.5);
```

## 14. Integration with Modern Java Features

### Generics with Streams (Java 8+)
```java
public class StreamWithGenerics {
    public static <T> Stream<T> flatten(Stream<? extends Collection<T>> stream) {
        return stream.flatMap(Collection::stream);
    }
    
    public static <T, R> Stream<R> mapNotNull(Stream<T> stream, Function<T, R> mapper) {
        return stream.map(mapper)
                    .filter(Objects::nonNull);
    }
    
    public static <T> Collector<T, ?, List<T>> toImmutableList() {
        return Collector.of(
            ArrayList::new,
            List::add,
            (list1, list2) -> { list1.addAll(list2); return list1; },
            Collections::unmodifiableList
        );
    }
}

// Usage
List<List<String>> nestedList = Arrays.asList(
    Arrays.asList("a", "b"),
    Arrays.asList("c", "d")
);

List<String> flattened = StreamWithGenerics.flatten(nestedList.stream())
    .collect(StreamWithGenerics.toImmutableList());
```

### Generics with Optional (Java 8+)
```java
public class OptionalWithGenerics {
    public static <T> Optional<T> fromNullable(T value) {
        return Optional.ofNullable(value);
    }
    
    public static <T, R> Optional<R> mapIfPresent(Optional<T> optional, Function<T, R> mapper) {
        return optional.map(mapper);
    }
    
    public static <T> T orElseThrow(Optional<T> optional, Supplier<? extends RuntimeException> exceptionSupplier) {
        return optional.orElseThrow(exceptionSupplier);
    }
    
    // Chaining optionals
    public static <T, R> Optional<R> flatMapChain(Optional<T> optional, Function<T, Optional<R>> mapper) {
        return optional.flatMap(mapper);
    }
}

// Usage example
Optional<String> optionalString = Optional.of("Hello");
Optional<Integer> length = OptionalWithGenerics.mapIfPresent(optionalString, String::length);
```

### Generics with CompletableFuture
```java
public class AsyncWithGenerics {
    public static <T> CompletableFuture<T> fromCallable(Callable<T> callable) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    public static <T, R> CompletableFuture<R> mapAsync(CompletableFuture<T> future, Function<T, R> mapper) {
        return future.thenApply(mapper);
    }
    
    public static <T, R> CompletableFuture<R> flatMapAsync(CompletableFuture<T> future, Function<T, CompletableFuture<R>> mapper) {
        return future.thenCompose(mapper);
    }
    
    // Combine multiple futures
    public static <T, U, R> CompletableFuture<R> combine(
            CompletableFuture<T> future1,
            CompletableFuture<U> future2,
            BiFunction<T, U, R> combiner) {
        return future1.thenCombine(future2, combiner);
    }
}
```

## 15. Real-World Enterprise Patterns

### Generic Repository with Spring Data
```java
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {
    @Query("SELECT e FROM #{#entityName} e WHERE e.active = true")
    List<T> findAllActive();
    
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.active = false WHERE e.id = :id")
    void softDelete(@Param("id") ID id);
    
    default Optional<T> findActiveById(ID id) {
        return findById(id).filter(entity -> {
            try {
                Method isActiveMethod = entity.getClass().getMethod("isActive");
                return (Boolean) isActiveMethod.invoke(entity);
            } catch (Exception e) {
                return true; // Assume active if no isActive method
            }
        });
    }
}

@Repository
public interface UserRepository extends BaseRepository<User, Long> {
    List<User> findByEmailContaining(String email);
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.active = true")
    List<User> findActiveUsersByRole(@Param("role") UserRole role);
}
```

### Generic Service Layer
```java
@Service
@Transactional
public abstract class GenericService<T, ID, R extends JpaRepository<T, ID>> {
    protected final R repository;
    
    public GenericService(R repository) {
        this.repository = repository;
    }
    
    public T save(T entity) {
        return repository.save(entity);
    }
    
    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }
    
    public List<T> findAll() {
        return repository.findAll();
    }
    
    public void deleteById(ID id) {
        repository.deleteById(id);
    }
    
    public Page<T> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }
    
    // Template method pattern
    protected abstract void validateEntity(T entity);
    
    public T saveWithValidation(T entity) {
        validateEntity(entity);
        return save(entity);
    }
}

@Service
public class UserService extends GenericService<User, Long, UserRepository> {
    
    public UserService(UserRepository userRepository) {
        super(userRepository);
    }
    
    @Override
    protected void validateEntity(User user) {
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        // Additional validation logic
    }
    
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }
}
```

### Generic REST Controller
```java
@RestController
public abstract class GenericController<T, ID, S extends GenericService<T, ID, ?>> {
    protected final S service;
    
    public GenericController(S service) {
        this.service = service;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<T> findById(@PathVariable ID id) {
        return service.findById(id)
            .map(entity -> ResponseEntity.ok(entity))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<Page<T>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<T> result = service.findAll(pageable);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping
    public ResponseEntity<T> create(@Valid @RequestBody T entity) {
        T saved = service.saveWithValidation(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<T> update(@PathVariable ID id, @Valid @RequestBody T entity) {
        return service.findById(id)
            .map(existing -> {
                // Copy properties or use a mapping framework
                T updated = service.save(entity);
                return ResponseEntity.ok(updated);
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable ID id) {
        if (service.findById(id).isPresent()) {
            service.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

@RestController
@RequestMapping("/api/users")
public class UserController extends GenericController<User, Long, UserService> {
    
    public UserController(UserService userService) {
        super(userService);
    }
    
    @GetMapping("/by-email")
    public ResponseEntity<User> findByEmail(@RequestParam String email) {
        return service.findByEmail(email)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
```

## 16. Testing with Generics

### Generic Test Utilities
```java
public abstract class GenericRepositoryTest<T, ID, R extends JpaRepository<T, ID>> {
    
    @Autowired
    protected R repository;
    
    protected abstract T createValidEntity();
    protected abstract ID getEntityId(T entity);
    protected abstract void modifyEntity(T entity);
    
    @Test
    void testSaveAndFindById() {
        // Given
        T entity = createValidEntity();
        
        // When
        T saved = repository.save(entity);
        Optional<T> found = repository.findById(getEntityId(saved));
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(saved);
    }
    
    @Test
    void testUpdate() {
        // Given
        T entity = createValidEntity();
        T saved = repository.save(entity);
        
        // When
        modifyEntity(saved);
        T updated = repository.save(saved);
        
        // Then
        Optional<T> found = repository.findById(getEntityId(updated));
        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(updated);
    }
    
    @Test
    void testDelete() {
        // Given
        T entity = createValidEntity();
        T saved = repository.save(entity);
        ID id = getEntityId(saved);
        
        // When
        repository.deleteById(id);
        
        // Then
        Optional<T> found = repository.findById(id);
        assertThat(found).isEmpty();
    }
}

@DataJpaTest
class UserRepositoryTest extends GenericRepositoryTest<User, Long, UserRepository> {
    
    @Override
    protected User createValidEntity() {
        return User.builder()
            .name("John Doe")
            .email("john@example.com")
            .active(true)
            .build();
    }
    
    @Override
    protected Long getEntityId(User entity) {
        return entity.getId();
    }
    
    @Override
    protected void modifyEntity(User entity) {
        entity.setName("Jane Doe");
    }
    
    @Test
    void testFindByEmail() {
        // Given
        User user = createValidEntity();
        repository.save(user);
        
        // When
        Optional<User> found = repository.findByEmail("john@example.com");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John Doe");
    }
}
```

### Generic Test Data Builders
```java
public abstract class GenericBuilder<T, B extends GenericBuilder<T, B>> {
    protected abstract B self();
    public abstract T build();
    
    // Common builder methods can be added here
    protected <V> B with(Consumer<T> setter, V value) {
        // This would need to be implemented in concrete builders
        return self();
    }
}

public class UserBuilder extends GenericBuilder<User, UserBuilder> {
    private String name = "Default Name";
    private String email = "default@example.com";
    private boolean active = true;
    private UserRole role = UserRole.USER;
    
    @Override
    protected UserBuilder self() {
        return this;
    }
    
    public UserBuilder name(String name) {
        this.name = name;
        return this;
    }
    
    public UserBuilder email(String email) {
        this.email = email;
        return this;
    }
    
    public UserBuilder active(boolean active) {
        this.active = active;
        return this;
    }
    
    public UserBuilder role(UserRole role) {
        this.role = role;
        return this;
    }
    
    @Override
    public User build() {
        return User.builder()
            .name(name)
            .email(email)
            .active(active)
            .role(role)
            .build();
    }
    
    public static UserBuilder aUser() {
        return new UserBuilder();
    }
}

// Usage in tests
User user = UserBuilder.aUser()
    .name("John Doe")
    .email("john@example.com")
    .role(UserRole.ADMIN)
    .build();
```

## 17. Performance Optimization with Generics

### Avoiding Excessive Object Creation
```java
// Inefficient - creates new objects in loop
public class InefficientGenericProcessor<T> {
    public List<String> processItems(List<T> items) {
        List<String> results = new ArrayList<>();
        for (T item : items) {
            results.add(item.toString()); // Potential string concatenation issues
        }
        return results;
    }
}

// Efficient - pre-sized collections and StringBuilder
public class EfficientGenericProcessor<T> {
    public List<String> processItems(List<T> items) {
        List<String> results = new ArrayList<>(items.size()); // Pre-sized
        StringBuilder sb = new StringBuilder(); // Reuse StringBuilder
        
        for (T item : items) {
            sb.setLength(0); // Reset instead of creating new
            sb.append("Processed: ").append(item.toString());
            results.add(sb.toString());
        }
        return results;
    }
}
```

### Generic Object Pooling
```java
public class GenericObjectPool<T> {
    private final Queue<T> pool = new ConcurrentLinkedQueue<>();
    private final Supplier<T> factory;
    private final Consumer<T> resetFunction;
    private final int maxSize;
    
    public GenericObjectPool(Supplier<T> factory, Consumer<T> resetFunction, int maxSize) {
        this.factory = factory;
        this.resetFunction = resetFunction;
        this.maxSize = maxSize;
    }
    
    public T acquire() {
        T object = pool.poll();
        return object != null ? object : factory.get();
    }
    
    public void release(T object) {
        if (pool.size() < maxSize) {
            resetFunction.accept(object);
            pool.offer(object);
        }
    }
    
    public int size() {
        return pool.size();
    }
}

// Usage
GenericObjectPool<StringBuilder> stringBuilderPool = new GenericObjectPool<>(
    StringBuilder::new,
    sb -> sb.setLength(0), // Reset function
    10 // Max pool size
);

// Use in performance-critical code
StringBuilder sb = stringBuilderPool.acquire();
try {
    sb.append("Some text");
    String result = sb.toString();
    // Use result
} finally {
    stringBuilderPool.release(sb);
}
```

## Conclusion

Mastering Java Generics requires understanding not just the syntax, but the underlying principles of type safety, variance, and type erasure. The key to becoming top 1% is:

1. **Understand Type Erasure**: Know how generics work at runtime and why certain limitations exist
2. **Master Wildcards**: Use PECS principle effectively for flexible APIs
3. **Apply Advanced Patterns**: Use self-bounded generics, phantom types, and generic factories
4. **Practice with Real Projects**: Implement generic repositories, builders, and utility classes
5. **Optimize Performance**: Understand boxing/unboxing and object creation implications
6. **Test Thoroughly**: Write comprehensive tests for your generic code

The real mastery comes from applying these concepts in production code, understanding the trade-offs, and knowing when to use (and when not to use) generics for maximum benefit.

Remember: Generics are not just about avoiding ClassCastException - they're about creating expressive, type-safe APIs that make incorrect code impossible to compile. When you can design APIs that guide developers toward correct usage through the type system, you've truly mastered Java Generics.