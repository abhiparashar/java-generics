# Java Generics Mastery - Part 1: Foundations

## Why This Guide Exists

You want to contribute to Spring Boot and Spring AI. When you open those codebases, you'll see things like:

```java
public interface ChatClient<REQ extends Prompt, RES extends ChatResponse> { ... }
public class BeanFactory<T> { ... }
ResponseEntity<List<Map<String, Object>>> response = ...
```

If generics feel like hieroglyphics to you, this series will change that. By the end, you'll read Spring source code like it's prose.

---

## Chapter 1: The Problem Generics Solve

### Before Generics: The Dark Ages (Pre-Java 5)

Imagine you're building a simple list to hold strings:

```java
// Java 1.4 style - no generics
List names = new ArrayList();
names.add("Alice");
names.add("Bob");
names.add(42);  // Oops! No compiler error!

// Later in code...
for (int i = 0; i < names.size(); i++) {
    String name = (String) names.get(i);  // BOOM! ClassCastException at runtime for 42
}
```

**The Problems:**

1. **No Type Safety**: The compiler couldn't stop you from adding an `Integer` to a list meant for `String`s
2. **Runtime Failures**: Errors showed up when users ran the program, not when developers compiled it
3. **Casting Everywhere**: Every retrieval needed an explicit cast: `(String) names.get(i)`
4. **Lost Intent**: Reading code, you couldn't tell what type the list was supposed to hold

### The Real Cost

In enterprise software, these bugs were catastrophic:
- A `ClassCastException` in production could crash critical systems
- Debugging was hard because the error occurred far from where the wrong type was added
- Code reviews couldn't catch type mismatches by reading the code

---

## Chapter 2: What Generics Actually Are

### The Core Idea

Generics let you **parameterize types**. Just like methods have parameters for values:

```java
public int add(int a, int b) { return a + b; }
```

Classes and methods can have parameters for **types**:

```java
public class Box<T> {  // T is a type parameter
    private T item;
    
    public void set(T item) { this.item = item; }
    public T get() { return item; }
}
```

### The Mental Model

Think of `T` as a **placeholder** that gets filled in when you use the class:

```java
Box<String> stringBox = new Box<>();   // T becomes String
Box<Integer> intBox = new Box<>();     // T becomes Integer
Box<Employee> empBox = new Box<>();    // T becomes Employee
```

When you write `Box<String>`:
- Every `T` in the class **behaves as if** it were `String`
- `set(T item)` becomes `set(String item)`
- `get()` becomes `String get()`

### The Contract

Generics establish a **compile-time contract**:

```java
Box<String> box = new Box<>();
box.set("Hello");       // ✓ Allowed - String matches T
box.set(42);            // ✗ Compile error - Integer doesn't match T
String value = box.get(); // ✓ No cast needed - compiler knows it's String
```

---

## Chapter 3: Generic Class Anatomy

### Declaring a Generic Class

```java
public class Container<T> {
    //          ↑
    //   Type parameter declaration
    //   Goes after class name, inside angle brackets
    
    private T value;  // Using T as a field type
    
    public Container(T value) {  // Using T as parameter type
        this.value = value;
    }
    
    public T getValue() {  // Using T as return type
        return value;
    }
    
    public void setValue(T value) {  // Using T as parameter type
        this.value = value;
    }
}
```

### Naming Conventions (Important!)

These are conventions used across all Java projects including Spring:

| Parameter | Meaning | Example Usage |
|-----------|---------|---------------|
| `T` | Type | `Container<T>`, `List<T>` |
| `E` | Element | `Collection<E>`, `List<E>` |
| `K` | Key | `Map<K, V>` |
| `V` | Value | `Map<K, V>` |
| `N` | Number | `Calculator<N extends Number>` |
| `R` | Result/Return | `Function<T, R>` |
| `S, U, V` | 2nd, 3rd, 4th types | When you need multiple type parameters |

When you see Spring code like:

```java
public interface ResponseExtractor<T> {
    T extractData(ClientHttpResponse response);
}
```

You immediately know: "T is the type of data this extractor produces."

### Multiple Type Parameters

You can have as many type parameters as needed:

```java
public class Pair<K, V> {
    private K key;
    private V value;
    
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }
    
    public K getKey() { return key; }
    public V getValue() { return value; }
}

// Usage
Pair<String, Integer> score = new Pair<>("Alice", 95);
Pair<Employee, Department> assignment = new Pair<>(emp, dept);
```

This is exactly how `Map.Entry<K, V>` works in Java's collections.

---

## Chapter 4: Using Generic Classes

### Instantiation

```java
// Full syntax
Container<String> container1 = new Container<String>("Hello");

// Diamond operator (Java 7+) - compiler infers type from left side
Container<String> container2 = new Container<>("Hello");

// With var (Java 10+) - still need the type on the right
var container3 = new Container<String>("Hello");
```

### Why the Diamond Operator Exists

Before Java 7:
```java
Map<String, List<Employee>> departments = new HashMap<String, List<Employee>>();
// Repetitive and verbose!
```

After Java 7:
```java
Map<String, List<Employee>> departments = new HashMap<>();
// Compiler infers the type arguments from the left side
```

### Nested Generics

In Spring projects, you'll often see deeply nested generics:

```java
// A map where keys are strings, values are lists of maps
Map<String, List<Map<String, Object>>> complexData = new HashMap<>();

// Let's break this down:
// - Outer Map: String → List<...>
// - Each List contains: Map<String, Object>
// - Inner Map: String → Object

// Building it up step by step:
Map<String, Object> person1 = Map.of("name", "Alice", "age", 30);
Map<String, Object> person2 = Map.of("name", "Bob", "age", 25);

List<Map<String, Object>> people = List.of(person1, person2);

complexData.put("employees", people);
```

**Reading Nested Generics - The Inside-Out Technique:**

For `ResponseEntity<List<Map<String, Object>>>`:
1. Innermost: `Map<String, Object>` - a map with String keys and Object values
2. Next layer: `List<...>` - a list of those maps
3. Outermost: `ResponseEntity<...>` - an HTTP response containing that list

---

## Chapter 5: Raw Types and Why You Must Avoid Them

### What Are Raw Types?

A raw type is a generic class used without type parameters:

```java
List rawList = new ArrayList();  // Raw type - NO type parameter
List<String> typedList = new ArrayList<>();  // Parameterized type
```

### Why Raw Types Exist

Only for backward compatibility with pre-Java 5 code. **Never use them in new code.**

### The Dangers

```java
List rawList = new ArrayList();
rawList.add("Hello");
rawList.add(42);
rawList.add(new Employee());
// All compile! No type safety at all.

// Later...
String s = (String) rawList.get(1);  // ClassCastException!
```

### The Compiler Warning

When you use raw types, you'll see:

```
Note: SomeClass.java uses unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
```

**In Spring Boot projects, treat all compiler warnings as errors.** Configure your build:

```xml
<!-- Maven -->
<compilerArgs>
    <arg>-Xlint:all</arg>
    <arg>-Werror</arg>
</compilerArgs>
```

---

## Chapter 6: Your First Generic Class - A Real Example

Let's build something useful: a `Result` type that represents either success or failure.

### The Problem It Solves

Without a Result type:
```java
public User findUser(String id) throws UserNotFoundException {
    // What if we want to return an error without throwing?
    // What if we want to include error details?
}
```

### The Generic Solution

```java
public class Result<T> {
    private final T value;
    private final String error;
    private final boolean success;
    
    // Private constructor - we'll use factory methods
    private Result(T value, String error, boolean success) {
        this.value = value;
        this.error = error;
        this.success = success;
    }
    
    // Factory method for success
    public static <T> Result<T> success(T value) {
        return new Result<>(value, null, true);
    }
    
    // Factory method for failure
    public static <T> Result<T> failure(String error) {
        return new Result<>(null, error, false);
    }
    
    public boolean isSuccess() { return success; }
    public boolean isFailure() { return !success; }
    
    public T getValue() {
        if (!success) {
            throw new IllegalStateException("Cannot get value from failed result");
        }
        return value;
    }
    
    public String getError() {
        if (success) {
            throw new IllegalStateException("Cannot get error from successful result");
        }
        return error;
    }
}
```

### Usage

```java
public Result<User> findUser(String id) {
    User user = database.find(id);
    if (user != null) {
        return Result.success(user);
    }
    return Result.failure("User not found: " + id);
}

// Calling code
Result<User> result = findUser("123");
if (result.isSuccess()) {
    User user = result.getValue();  // No cast needed!
    System.out.println(user.getName());
} else {
    System.err.println(result.getError());
}
```

### Why This Pattern Matters for Spring

Spring uses this exact pattern! Look at `ResponseEntity<T>`:

```java
// Spring's ResponseEntity is generic
ResponseEntity<User> response = ResponseEntity.ok(user);
ResponseEntity<String> error = ResponseEntity.badRequest().body("Invalid ID");
```

---

## Chapter 7: Exercises

### Exercise 1: Build a `Cache<K, V>`

Create a simple cache that:
- Stores key-value pairs
- Has a maximum size
- Returns `Optional<V>` for gets (hint: use `Optional.ofNullable()`)

```java
public class Cache<K, V> {
    // Your implementation here
    
    public void put(K key, V value) { }
    public Optional<V> get(K key) { }
    public void remove(K key) { }
    public int size() { }
}
```

### Exercise 2: Identify the Types

For each declaration, identify what `T` represents:

```java
List<String> names;                    // T = ?
Map<Integer, Employee> employees;      // K = ?, V = ?
Optional<List<String>> maybeNames;     // T = ?
ResponseEntity<Map<String, Object>> response;  // T = ?
```

### Exercise 3: Fix the Raw Types

Convert this pre-generics code to use proper generics:

```java
List items = new ArrayList();
items.add("Hello");
items.add("World");

Iterator it = items.iterator();
while (it.hasNext()) {
    String s = (String) it.next();
    System.out.println(s.toUpperCase());
}
```

---

## Key Takeaways

1. **Generics provide compile-time type safety** - catch errors before runtime
2. **No more casting** - the compiler tracks types for you
3. **Self-documenting code** - `List<Employee>` is clearer than `List`
4. **Type parameters are placeholders** - filled in when the class is used
5. **Never use raw types** - always specify type parameters
6. **Follow naming conventions** - T for Type, E for Element, K for Key, V for Value

---

## What's Next

In Part 2, we'll dive into:
- **Generic Interfaces** - How Spring defines contracts like `Repository<T, ID>`
- **Implementing Generic Interfaces** - Creating concrete implementations
- **Generic Inheritance** - Extending generic classes and interfaces

This is where Spring's architecture really shines, and understanding it is crucial for contributing to Spring projects.