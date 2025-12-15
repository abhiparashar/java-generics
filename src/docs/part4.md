# Java Generics Mastery - Part 4: Bounded Type Parameters

## Why Bounds Matter

Without bounds, you can only treat T as `Object`:

```java
public class Calculator<T> {
    public T add(T a, T b) {
        return a + b;  // ✗ Compile error! Can't use + on Object
    }
}
```

With bounds, you gain access to specific methods:

```java
public class Calculator<T extends Number> {
    public double add(T a, T b) {
        return a.doubleValue() + b.doubleValue();  // ✓ Works! T has Number's methods
    }
}
```

Spring uses bounds everywhere for type safety and capability guarantees.

---

## Chapter 1: Upper Bounds with `extends`

### Basic Syntax

```java
public class Box<T extends Number> {
//                ↑
//      T must be Number or a subclass of Number
    
    private T value;
    
    public double getDoubleValue() {
        return value.doubleValue();  // Can call Number methods on T
    }
}
```

### What Can T Be?

```java
Box<Integer> intBox = new Box<>();     // ✓ Integer extends Number
Box<Double> doubleBox = new Box<>();   // ✓ Double extends Number
Box<Long> longBox = new Box<>();       // ✓ Long extends Number
Box<String> stringBox = new Box<>();   // ✗ String doesn't extend Number
Box<Object> objectBox = new Box<>();   // ✗ Object doesn't extend Number
```

### The Mental Model

Think of `T extends Number` as a **contract**:
- "I promise T will always be a Number or subtype"
- "In exchange, you can treat T as a Number"

---

## Chapter 2: Why Bounds Unlock Capabilities

### Without Bounds - Limited to Object Methods

```java
public class Processor<T> {
    public void process(T item) {
        // What can you do with item?
        item.toString();    // ✓ Object method
        item.hashCode();    // ✓ Object method
        item.getClass();    // ✓ Object method
        item.compareTo(x);  // ✗ Not available - T might not be Comparable
        item.length();      // ✗ Not available - T might not be String/array
    }
}
```

### With Bounds - Access to Interface Methods

```java
public class Processor<T extends Comparable<T>> {
    public void process(T item1, T item2) {
        int result = item1.compareTo(item2);  // ✓ T guarantees Comparable
        // Now you can sort, find max/min, etc.
    }
}
```

### Real Example: Finding Maximum

```java
// Without bounds - impossible to implement correctly
public static <T> T max(T a, T b) {
    return a > b ? a : b;  // ✗ Can't compare T values
}

// With bounds - works perfectly
public static <T extends Comparable<T>> T max(T a, T b) {
    return a.compareTo(b) > 0 ? a : b;  // ✓ T can be compared
}

// Usage
String maxStr = max("apple", "banana");  // "banana"
Integer maxInt = max(10, 20);            // 20
```

---

## Chapter 3: Multiple Bounds

Sometimes you need T to satisfy multiple constraints:

```java
public class DataProcessor<T extends Serializable & Comparable<T>> {
//                                  ↑              ↑
//                            First bound    Second bound
//                            (can be class)  (must be interface)
    
    public void process(T data) {
        // Can serialize (Serializable)
        // Can compare (Comparable)
    }
}
```

### Rules for Multiple Bounds

1. **Syntax**: Use `&` to separate bounds
2. **Class first**: If a class is a bound, it must come first
3. **One class max**: Can only have one class bound (Java is single inheritance)
4. **Multiple interfaces**: Can have multiple interface bounds

```java
// ✓ Valid - class first, then interfaces
<T extends Number & Comparable<T> & Serializable>

// ✗ Invalid - interface before class
<T extends Comparable<T> & Number>

// ✗ Invalid - two classes
<T extends Number & String>
```

### Practical Example

```java
public class SortableCache<K extends Comparable<K> & Serializable, V extends Serializable> {
    private TreeMap<K, V> cache = new TreeMap<>();  // TreeMap needs Comparable keys
    
    public void put(K key, V value) {
        cache.put(key, value);  // Keys are sorted automatically
    }
    
    public V get(K key) {
        return cache.get(key);
    }
    
    public void saveToFile(File file) throws IOException {
        // Can serialize because both K and V extend Serializable
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(cache);
        }
    }
}
```

---

## Chapter 4: Recursive Bounds (Self-Referential Bounds)

This is a pattern you'll see constantly in Spring:

```java
public interface Comparable<T> {
    int compareTo(T other);
}

// Self-referential bound
public class MyClass implements Comparable<MyClass> {
    @Override
    public int compareTo(MyClass other) {
        // Compare this instance with another MyClass
    }
}
```

### The `<T extends Comparable<T>>` Pattern

```java
public static <T extends Comparable<T>> T max(T a, T b) {
//                           ↑
//              T can compare with other T's
    return a.compareTo(b) > 0 ? a : b;
}
```

This means: "T must be comparable to itself."

### Why It Matters

Without the self-reference:
```java
public static <T extends Comparable<?>> T max(T a, T b) {
    return a.compareTo(b) > 0 ? a : b;  // ✗ Compile error!
    // compareTo expects ?, not T
}
```

With the self-reference:
```java
public static <T extends Comparable<T>> T max(T a, T b) {
    return a.compareTo(b) > 0 ? a : b;  // ✓ compareTo expects T
}
```

---

## Chapter 5: Bounds in Spring Framework

### Spring Data Example

```java
public interface CrudRepository<T, ID> extends Repository<T, ID> {
    Optional<T> findById(ID id);
    <S extends T> S save(S entity);  // S is a subtype of T
}
```

The `<S extends T>` is crucial:
```java
// Entity hierarchy
public class User { }
public class AdminUser extends User { }

// Repository
public interface UserRepository extends CrudRepository<User, Long> { }

// What <S extends T> enables:
AdminUser admin = new AdminUser();
AdminUser saved = userRepository.save(admin);  // Returns AdminUser, not User!
```

Without `<S extends T>`:
```java
User saved = userRepository.save(admin);  // Would return User
AdminUser admin2 = (AdminUser) saved;     // Would need a cast!
```

### Spring's Assert Class

```java
public abstract class Assert {
    
    public static <T extends CharSequence> void hasText(T text, String message) {
        // T must be a CharSequence (String, StringBuilder, etc.)
        if (text == null || text.length() == 0 || !containsText(text)) {
            throw new IllegalArgumentException(message);
        }
    }
    
    public static <T extends Collection<?>> void notEmpty(T collection, String message) {
        // T must be a Collection
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }
}
```

---

## Chapter 6: The Enum Bound Pattern

Enums in Java have a special self-referential bound:

```java
// How Enum is actually declared (simplified)
public abstract class Enum<E extends Enum<E>> implements Comparable<E> {
    public final int compareTo(E other) { ... }
    public final String name() { ... }
}

// Your enum implicitly extends Enum<YourEnum>
public enum Color extends Enum<Color> {
    RED, GREEN, BLUE
}
```

### Using Enum Bounds

```java
public class EnumCache<E extends Enum<E>> {
    private final EnumMap<E, Object> cache;
    private final Class<E> enumType;
    
    public EnumCache(Class<E> enumType) {
        this.enumType = enumType;
        this.cache = new EnumMap<>(enumType);
    }
    
    public void put(E key, Object value) {
        cache.put(key, value);
    }
    
    public E[] getAllKeys() {
        return enumType.getEnumConstants();
    }
}

// Usage
EnumCache<Color> colorCache = new EnumCache<>(Color.class);
colorCache.put(Color.RED, "#FF0000");
```

---

## Chapter 7: Builder Pattern with Bounds

The famous "self-returning builder" pattern:

```java
public abstract class Builder<T, B extends Builder<T, B>> {
//                             ↑                    ↑
//                       What we build      The actual builder type
    
    protected abstract B self();
    
    public abstract T build();
}

public class UserBuilder extends Builder<User, UserBuilder> {
    private String name;
    private int age;
    
    @Override
    protected UserBuilder self() {
        return this;
    }
    
    public UserBuilder name(String name) {
        this.name = name;
        return self();  // Returns UserBuilder, not Builder
    }
    
    public UserBuilder age(int age) {
        this.age = age;
        return self();
    }
    
    @Override
    public User build() {
        return new User(name, age);
    }
}

// Fluent API works correctly:
User user = new UserBuilder()
    .name("Alice")  // Returns UserBuilder
    .age(30)        // Returns UserBuilder
    .build();       // Returns User
```

### Why This Works

```java
// Without the bound B extends Builder<T, B>:
public abstract class Builder<T, B> {
    public B name(String name) {
        // B could be anything! No guarantee it's actually a builder
    }
}

// With the bound:
public abstract class Builder<T, B extends Builder<T, B>> {
    // B is guaranteed to be a Builder subtype
    // So calling self() returns the actual builder type
}
```

---

## Chapter 8: Common Mistakes

### Mistake 1: Using Bounds When Not Needed

```java
// ✗ Unnecessary bound
public <T extends Object> void process(T item) { }

// ✓ Equivalent and simpler
public <T> void process(T item) { }
```

### Mistake 2: Wrong Bound Direction

```java
// You want to process any Number or subclass
public <T extends Number> void processNumbers(List<T> numbers) { }

// ✗ This is wrong - super is for wildcards only (see Part 5)
public <T super Integer> void wrongMethod() { }  // Doesn't compile!
```

### Mistake 3: Forgetting the Self-Reference

```java
// ✗ Problem: can compare with any Comparable
public static <T extends Comparable<?>> T max(T a, T b) {
    return a.compareTo(b) > 0 ? a : b;  // Won't compile!
}

// ✓ Correct: can only compare with same type
public static <T extends Comparable<T>> T max(T a, T b) {
    return a.compareTo(b) > 0 ? a : b;
}
```

### Mistake 4: Over-Constraining

```java
// ✗ Too many constraints - limits usability
public <T extends Number & Comparable<T> & Serializable & Cloneable> T process(T value) {
    // Most users can't provide a T that satisfies all of this
}

// ✓ Only constrain what you actually need
public <T extends Comparable<T>> T max(T a, T b) {
    // Only needs comparison capability
}
```

---

## Chapter 9: Real-World Spring Pattern

### The Generic DAO Pattern

```java
public interface GenericDao<T, ID extends Serializable> {
    T findById(ID id);
    List<T> findAll();
    T save(T entity);
    void delete(T entity);
}

public abstract class AbstractDao<T, ID extends Serializable> implements GenericDao<T, ID> {
    
    private final Class<T> entityClass;
    
    @SuppressWarnings("unchecked")
    protected AbstractDao() {
        // Get the actual type argument at runtime
        this.entityClass = (Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[0];
    }
    
    @Override
    public T findById(ID id) {
        return entityManager.find(entityClass, id);
    }
    
    @Override
    public List<T> findAll() {
        return entityManager.createQuery(
            "SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass)
            .getResultList();
    }
}

// Concrete implementation
public class UserDao extends AbstractDao<User, Long> {
    // Inherits all CRUD operations typed for User and Long
}
```

---

## Chapter 10: Exercises

### Exercise 1: Implement a Bounded Container

```java
public class NumberContainer<T extends Number> {
    private T value;
    
    public void setValue(T value) { this.value = value; }
    
    // Implement these using Number's methods
    public double asDouble() { /* Your code */ }
    public long asLong() { /* Your code */ }
    public boolean isPositive() { /* Your code */ }
}
```

### Exercise 2: Create a Comparable Sorter

```java
public class Sorter {
    // Implement bubble sort using Comparable
    public static <T extends Comparable<T>> void sort(List<T> list) {
        // Your implementation
    }
}
```

### Exercise 3: Multi-Bounded Method

Create a method that:
- Takes a list of items
- Each item must be Comparable (for sorting)
- Each item must be Serializable (for saving)
- Returns the sorted list

```java
public static <T extends ???> List<T> sortAndSave(List<T> items, String filename) {
    // Sort the list
    // Save to file
    // Return sorted list
}
```

### Exercise 4: Analyze Spring Code

What do the bounds mean in this Spring interface?

```java
public interface Converter<S, T> {
    T convert(S source);
}

public interface GenericConverter {
    Set<ConvertiblePair> getConvertibleTypes();
    Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType);
}

public interface ConditionalGenericConverter extends GenericConverter {
    boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType);
}
```

---

## Key Takeaways

1. **`extends` sets upper bounds** - T must be the bound type or a subtype
2. **Bounds unlock capabilities** - Access methods of the bound type
3. **Multiple bounds use `&`** - Class first (if any), then interfaces
4. **Self-referential bounds** - `<T extends Comparable<T>>` for comparing with same type
5. **Spring uses bounds extensively** - Repository save methods, builders, DAOs
6. **Only constrain when necessary** - Over-constraining reduces flexibility

---

## What's Next

In Part 5, we'll master:
- **Wildcards** - `?`, `? extends T`, `? super T`
- **PECS Principle** - Producer Extends, Consumer Super
- **When to Use Wildcards vs Type Parameters**

This is perhaps the most confusing part of generics, but also the most powerful. Understanding wildcards is essential for reading Spring's source code.