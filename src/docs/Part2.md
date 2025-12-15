# Java Generics Mastery - Part 2: Generic Interfaces & Inheritance

## Why This Part Matters for Spring

Spring's entire architecture is built on generic interfaces. When you see:

```java
public interface CrudRepository<T, ID> extends Repository<T, ID> {
    Optional<T> findById(ID id);
    <S extends T> S save(S entity);
    Iterable<T> findAll();
}
```

You need to understand:
1. How generic interfaces work
2. How to implement them
3. How generic inheritance chains work

Let's master each concept.

---

## Chapter 1: Generic Interfaces

### Basic Declaration

A generic interface looks just like a generic class:

```java
public interface Transformer<I, O> {
    O transform(I input);
}
```

- `I` = Input type
- `O` = Output type
- Any implementation must specify what `I` and `O` are

### Implementing Generic Interfaces

There are **three ways** to implement a generic interface:

#### Way 1: Specify Concrete Types

```java
// The interface
public interface Transformer<I, O> {
    O transform(I input);
}

// Implementation with concrete types
public class StringToIntegerTransformer implements Transformer<String, Integer> {
    @Override
    public Integer transform(String input) {
        return Integer.parseInt(input);
    }
}
```

When you implement `Transformer<String, Integer>`:
- `I` becomes `String`
- `O` becomes `Integer`
- The method signature becomes `Integer transform(String input)`

#### Way 2: Preserve the Generic Parameters

```java
// Still generic - passes the type parameters through
public class LoggingTransformer<I, O> implements Transformer<I, O> {
    private final Transformer<I, O> delegate;
    
    public LoggingTransformer(Transformer<I, O> delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public O transform(I input) {
        System.out.println("Input: " + input);
        O output = delegate.transform(input);
        System.out.println("Output: " + output);
        return output;
    }
}
```

This is called a **decorator** - it wraps another transformer and adds logging.

#### Way 3: Partially Specify Types

```java
// Fix one type, keep the other generic
public class JsonParser<T> implements Transformer<String, T> {
    private final Class<T> targetType;
    
    public JsonParser(Class<T> targetType) {
        this.targetType = targetType;
    }
    
    @Override
    public T transform(String json) {
        // Parse JSON to type T
        return objectMapper.readValue(json, targetType);
    }
}

// Usage
JsonParser<User> userParser = new JsonParser<>(User.class);
User user = userParser.transform("{\"name\": \"Alice\"}");
```

---

## Chapter 2: Spring's Repository Pattern - A Deep Dive

Let's trace through Spring Data's actual interface hierarchy:

### Level 1: The Marker Interface

```java
// The root - just marks something as a repository
@Indexed
public interface Repository<T, ID> {
    // Empty! Just a marker interface.
    // T = Entity type (e.g., User, Product)
    // ID = Primary key type (e.g., Long, String, UUID)
}
```

### Level 2: CRUD Operations

```java
@NoRepositoryBean
public interface CrudRepository<T, ID> extends Repository<T, ID> {
    
    <S extends T> S save(S entity);
    
    Optional<T> findById(ID id);
    
    boolean existsById(ID id);
    
    Iterable<T> findAll();
    
    long count();
    
    void deleteById(ID id);
    
    void delete(T entity);
}
```

**Notice something interesting:** `<S extends T> S save(S entity)`

This means: "Accept any type `S` that is `T` or a subclass of `T`, and return that same type `S`."

Why? Consider:
```java
// Without <S extends T>
User save(User entity);  // Always returns User

// With <S extends T>
AdminUser admin = new AdminUser();  // AdminUser extends User
AdminUser saved = repository.save(admin);  // Returns AdminUser, not just User!
```

This preserves the specific subtype through the save operation.

### Level 3: Your Implementation

```java
// Your entity
@Entity
public class Product {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private BigDecimal price;
    // getters, setters
}

// Your repository - just an interface!
public interface ProductRepository extends CrudRepository<Product, Long> {
    // Spring Data implements this automatically!
    
    // You can add custom query methods:
    List<Product> findByPriceLessThan(BigDecimal maxPrice);
    Optional<Product> findByName(String name);
}
```

When you extend `CrudRepository<Product, Long>`:
- `T` becomes `Product`
- `ID` becomes `Long`
- All methods are automatically typed correctly

---

## Chapter 3: Generic Inheritance Rules

### Rule 1: Subclass Must Satisfy Superclass Type Parameters

```java
public interface Container<T> {
    void add(T item);
    T get(int index);
}

// ✓ Valid - specifies concrete types
public class StringContainer implements Container<String> { ... }

// ✓ Valid - passes through type parameters
public class FlexibleContainer<E> implements Container<E> { ... }

// ✗ Invalid - missing type argument
public class BrokenContainer implements Container { ... }  // Raw type warning!
```

### Rule 2: Extending Generic Classes

```java
public class Box<T> {
    protected T item;
    
    public T get() { return item; }
    public void set(T item) { this.item = item; }
}

// Option 1: Fix the type
public class StringBox extends Box<String> {
    // Inherits: String get() and void set(String item)
    
    public void append(String suffix) {
        set(get() + suffix);  // Works because item is String
    }
}

// Option 2: Keep it generic with different name
public class LabeledBox<E> extends Box<E> {
    private String label;
    
    public LabeledBox(String label) {
        this.label = label;
    }
    
    public String getLabel() { return label; }
}

// Option 3: Add more type parameters
public class PairBox<K, V> extends Box<V> {
    private K key;
    
    public K getKey() { return key; }
    public void setKey(K key) { this.key = key; }
    // Inherits V-typed methods from Box
}
```

### Rule 3: Multiple Interface Implementation

A class can implement multiple generic interfaces:

```java
public interface Readable<T> {
    T read();
}

public interface Writable<T> {
    void write(T data);
}

// Implementing both with the SAME type parameter
public class FileHandler<T> implements Readable<T>, Writable<T> {
    @Override
    public T read() { /* ... */ }
    
    @Override
    public void write(T data) { /* ... */ }
}

// Implementing both with DIFFERENT type parameters
public class Converter<I, O> implements Readable<I>, Writable<O> {
    @Override
    public I read() { /* ... */ }
    
    @Override
    public void write(O data) { /* ... */ }
}
```

---

## Chapter 4: The Covariant Return Type Trick

When overriding methods, you can return a more specific type:

```java
public interface Factory<T> {
    T create();
}

public class UserFactory implements Factory<User> {
    @Override
    public User create() {  // Could also return AdminUser (a User subclass)
        return new User();
    }
}

public class AdminUserFactory implements Factory<User> {
    @Override
    public AdminUser create() {  // More specific return type - VALID!
        return new AdminUser();
    }
}
```

This is called a **covariant return type** and it works because `AdminUser IS-A User`.

---

## Chapter 5: Real-World Pattern - The Builder Pattern with Generics

Spring Boot uses this pattern extensively for configuration builders:

```java
public abstract class AbstractBuilder<T, B extends AbstractBuilder<T, B>> {
    // B is the actual builder type (for method chaining)
    // T is what we're building
    
    @SuppressWarnings("unchecked")
    protected B self() {
        return (B) this;
    }
    
    public abstract T build();
}

public class UserBuilder extends AbstractBuilder<User, UserBuilder> {
    private String name;
    private int age;
    
    public UserBuilder name(String name) {
        this.name = name;
        return self();  // Returns UserBuilder, not AbstractBuilder
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

// Usage - fluent API with proper types
User user = new UserBuilder()
    .name("Alice")   // Returns UserBuilder
    .age(30)         // Returns UserBuilder
    .build();        // Returns User
```

### Why `B extends AbstractBuilder<T, B>`?

This is the **Curiously Recurring Template Pattern (CRTP)**. Let's decode it:

```java
AbstractBuilder<T, B extends AbstractBuilder<T, B>>
//             ↑  ↑
//             │  └── B must be a subclass of AbstractBuilder
//             └───── T is what we build

// When UserBuilder extends AbstractBuilder<User, UserBuilder>:
// T = User
// B = UserBuilder
// B extends AbstractBuilder<User, UserBuilder> ✓
```

The benefit: `self()` returns the **actual builder type**, enabling proper method chaining.

---

## Chapter 6: Spring's ResponseEntity Deep Dive

`ResponseEntity` is generic and shows many patterns:

```java
public class ResponseEntity<T> extends HttpEntity<T> {
    private final HttpStatusCode status;
    
    // Constructor
    public ResponseEntity(T body, HttpHeaders headers, HttpStatusCode status) {
        super(body, headers);
        this.status = status;
    }
    
    // Static factory methods with generics
    public static <T> ResponseEntity<T> ok(T body) {
        return new ResponseEntity<>(body, HttpStatus.OK);
    }
    
    public static <T> ResponseEntity<T> of(Optional<T> body) {
        return body.map(ResponseEntity::ok)
                   .orElseGet(() -> notFound().build());
    }
    
    // Builder pattern
    public static BodyBuilder status(HttpStatusCode status) {
        return new DefaultBuilder(status);
    }
    
    public static BodyBuilder ok() {
        return status(HttpStatus.OK);
    }
    
    // The builder interface
    public interface BodyBuilder {
        <T> ResponseEntity<T> body(T body);
        ResponseEntity<Void> build();
    }
}
```

### Usage Patterns You'll See

```java
// Simple response with body
@GetMapping("/user/{id}")
public ResponseEntity<User> getUser(@PathVariable Long id) {
    return userService.findById(id)
        .map(ResponseEntity::ok)                    // User found → 200 OK
        .orElse(ResponseEntity.notFound().build()); // Not found → 404
}

// Complex response
@PostMapping("/users")
public ResponseEntity<User> createUser(@RequestBody User user) {
    User saved = userService.save(user);
    URI location = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(saved.getId())
        .toUri();
    return ResponseEntity.created(location).body(saved);
}

// List response
@GetMapping("/users")
public ResponseEntity<List<User>> getAllUsers() {
    List<User> users = userService.findAll();
    return ResponseEntity.ok(users);
    // ResponseEntity<List<User>> - nested generics!
}
```

---

## Chapter 7: Common Mistakes and How to Avoid Them

### Mistake 1: Implementing Same Interface Twice with Different Types

```java
// ✗ Won't compile!
public class Broken implements Comparable<String>, Comparable<Integer> {
    // Type erasure makes both Comparable<String> and Comparable<Integer>
    // become just Comparable at runtime - collision!
}
```

### Mistake 2: Forgetting Type Arguments in Inheritance

```java
public interface Service<T> {
    T process(T input);
}

// ✗ Bad - raw type warning
public class MyService implements Service {
    public Object process(Object input) { ... }
}

// ✓ Good - specify the type
public class MyService implements Service<String> {
    public String process(String input) { ... }
}
```

### Mistake 3: Mixing Up Type Parameters

```java
public class Box<T> {
    private T item;
    
    // ✗ Wrong - E is not declared anywhere in this scope
    public void set(E item) {
        this.item = item;  // Compile error
    }
    
    // ✓ Right - use the class's type parameter
    public void set(T item) {
        this.item = item;
    }
}
```

---

## Chapter 8: Exercises

### Exercise 1: Implement a Generic Event System

Create an event system like Spring's:

```java
public interface Event { }

public interface EventListener<E extends Event> {
    void onEvent(E event);
}

public class EventBus {
    // Store listeners and dispatch events
    public <E extends Event> void register(Class<E> eventType, EventListener<E> listener) { }
    public <E extends Event> void publish(E event) { }
}
```

### Exercise 2: Create a Response Wrapper

Build a generic API response wrapper:

```java
public class ApiResponse<T> {
    private T data;
    private String message;
    private int statusCode;
    private LocalDateTime timestamp;
    
    // Implement factory methods:
    public static <T> ApiResponse<T> success(T data) { }
    public static <T> ApiResponse<T> error(String message, int code) { }
}
```

### Exercise 3: Trace the Types

Given:
```java
public interface Repository<T, ID> { }
public interface CrudRepository<T, ID> extends Repository<T, ID> { }
public interface JpaRepository<T, ID> extends CrudRepository<T, ID> { }
public interface UserRepository extends JpaRepository<User, Long> { }
```

What are `T` and `ID` at each level when `UserRepository` is used?

---

## Key Takeaways

1. **Generic interfaces define contracts** - implementations must provide concrete types
2. **Three ways to implement**: concrete types, preserve generics, or partial specification
3. **Generic inheritance chains** - types flow down through the hierarchy
4. **CRTP pattern** - `B extends Builder<T, B>` enables fluent APIs with proper types
5. **Covariant return types** - override methods can return more specific types
6. **Spring uses these patterns everywhere** - repositories, ResponseEntity, builders

---

## What's Next

In Part 3, we'll master:
- **Generic Methods** - Methods with their own type parameters
- **Type Inference** - How the compiler figures out types
- **Static Generic Methods** - Factory methods and utilities

This is where you'll learn to write utility classes like Spring's `BeanUtils` and `CollectionUtils`.