# Java Generics Mastery - Part 6: Type Erasure

## The Shocking Truth

Here's something that surprises many Java developers:

```java
List<String> strings = new ArrayList<>();
List<Integer> integers = new ArrayList<>();

System.out.println(strings.getClass() == integers.getClass());  // true!
```

At runtime, both are just `ArrayList`. The generic type information is **erased**. This chapter explains why, how, and what it means for your code.

---

## Chapter 1: What Is Type Erasure?

### The Core Concept

When Java compiles generic code, it:
1. Replaces type parameters with their bounds (or Object if unbounded)
2. Inserts casts where necessary
3. Generates bridge methods for polymorphism

### Before and After Compilation

**Your code:**
```java
public class Box<T> {
    private T item;
    
    public void set(T item) {
        this.item = item;
    }
    
    public T get() {
        return item;
    }
}
```

**After erasure (what the JVM sees):**
```java
public class Box {
    private Object item;  // T becomes Object
    
    public void set(Object item) {
        this.item = item;
    }
    
    public Object get() {
        return item;
    }
}
```

### Erasure with Bounds

**Your code:**
```java
public class NumberBox<T extends Number> {
    private T value;
    
    public double getDouble() {
        return value.doubleValue();
    }
}
```

**After erasure:**
```java
public class NumberBox {
    private Number value;  // T becomes Number (the bound)
    
    public double getDouble() {
        return value.doubleValue();
    }
}
```

---

## Chapter 2: How the Compiler Maintains Type Safety

If types are erased, how does Java prevent you from adding a String to a `List<Integer>`?

### Compile-Time Checks + Inserted Casts

**Your code:**
```java
List<String> names = new ArrayList<>();
names.add("Alice");
String name = names.get(0);
```

**After erasure (conceptually):**
```java
List names = new ArrayList();     // Raw List
names.add("Alice");               // No change needed
String name = (String) names.get(0);  // Compiler inserts cast!
```

The compiler:
1. **Prevents bad additions** at compile time
2. **Inserts casts** for retrievals
3. Guarantees that runtime casts won't fail (if you didn't use raw types or unchecked operations)

### The Guarantee

If your code compiles without unchecked warnings, the inserted casts will NEVER throw `ClassCastException`. This is called **heap pollution** prevention.

---

## Chapter 3: Consequences of Type Erasure

### Consequence 1: No Runtime Type Information

```java
// ✗ None of these work:
if (list instanceof List<String>) { }  // Compile error!
List<String>.class                      // Syntax error!
new T()                                 // Can't instantiate type parameter

// ✓ This is all you can check:
if (list instanceof List<?>) { }        // Just checks it's a List
if (list instanceof ArrayList) { }      // Checks runtime class
```

### Consequence 2: Same Compiled Class

```java
// These both compile to the same class:
public class StringList extends ArrayList<String> { }
public class IntegerList extends ArrayList<Integer> { }

// You can't overload based on generic type:
public void process(List<String> strings) { }
public void process(List<Integer> integers) { }  // ✗ Compile error - same erasure!
```

### Consequence 3: Can't Create Generic Arrays

```java
// ✗ Won't compile:
T[] array = new T[10];
List<String>[] stringLists = new List<String>[10];

// ✓ This works (but with warnings):
@SuppressWarnings("unchecked")
T[] array = (T[]) new Object[10];

// ✓ Better - use ArrayList instead:
List<T> list = new ArrayList<>();
```

---

## Chapter 4: Why Type Erasure Exists

### Historical Reason: Backward Compatibility

When generics were added in Java 5, billions of lines of code existed. Erasure allowed:

```java
// Old pre-generics code:
List list = new ArrayList();
list.add("hello");

// New generic code:
List<String> genericList = new ArrayList<>();
genericList.add("hello");

// They can interoperate!
List<String> newList = list;  // Warning, but compiles
```

Without erasure, the JVM would need changes, and old code wouldn't run. Erasure was a pragmatic choice.

### Trade-offs

**Advantages:**
- Perfect backward compatibility
- No JVM changes needed
- Existing libraries worked unchanged

**Disadvantages:**
- No runtime type information
- Can't create generic arrays
- Some patterns are impossible
- Bridge methods add complexity

---

## Chapter 5: Bridge Methods

When you override a generic method with a more specific type, the compiler generates a "bridge method":

### The Problem

```java
public class Node<T> {
    public T data;
    
    public void setData(T data) {
        this.data = data;
    }
}

public class StringNode extends Node<String> {
    @Override
    public void setData(String data) {  // More specific type
        System.out.println("Setting: " + data);
        super.setData(data);
    }
}
```

After erasure:
- `Node.setData(Object data)` - erased signature
- `StringNode.setData(String data)` - specific signature

These don't match! How can polymorphism work?

### The Solution: Bridge Methods

The compiler generates:

```java
public class StringNode extends Node {
    // Your method
    public void setData(String data) {
        System.out.println("Setting: " + data);
        super.setData(data);
    }
    
    // Bridge method (compiler-generated)
    public void setData(Object data) {
        setData((String) data);  // Calls your method
    }
}
```

Now `Node.setData(Object)` calls `StringNode.setData(Object)` which calls `StringNode.setData(String)`.

### You Can See Bridge Methods

```java
for (Method m : StringNode.class.getDeclaredMethods()) {
    System.out.println(m.getName() + " - bridge: " + m.isBridge());
}
// Output:
// setData - bridge: false
// setData - bridge: true   <- The bridge method!
```

---

## Chapter 6: Working Around Erasure

### Pattern 1: Pass Class<T> Token

```java
public class Factory<T> {
    private final Class<T> type;
    
    public Factory(Class<T> type) {
        this.type = type;
    }
    
    public T create() throws Exception {
        return type.getDeclaredConstructor().newInstance();
    }
    
    public boolean isInstance(Object obj) {
        return type.isInstance(obj);
    }
}

// Usage
Factory<User> userFactory = new Factory<>(User.class);
User user = userFactory.create();
```

### Pattern 2: Super Type Token (Jackson, Spring)

For complex types like `List<String>`, a simple Class token doesn't work. Use an anonymous class trick:

```java
public abstract class TypeReference<T> {
    private final Type type;
    
    protected TypeReference() {
        Type superclass = getClass().getGenericSuperclass();
        this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }
    
    public Type getType() {
        return type;
    }
}

// Usage - anonymous class captures type info!
TypeReference<List<String>> ref = new TypeReference<List<String>>() {};
System.out.println(ref.getType());  // java.util.List<java.lang.String>
```

### How This Works

When you create `new TypeReference<List<String>>() {}`:
1. You create an anonymous subclass of TypeReference
2. The anonymous class has `TypeReference<List<String>>` as its generic superclass
3. This is stored in the `.class` file and accessible via reflection!

### Pattern 3: Capture at Assignment

```java
public abstract class GenericDao<T> {
    private final Class<T> entityClass;
    
    @SuppressWarnings("unchecked")
    protected GenericDao() {
        // Get the actual type argument from the subclass
        ParameterizedType genericSuperclass = 
            (ParameterizedType) getClass().getGenericSuperclass();
        this.entityClass = (Class<T>) genericSuperclass.getActualTypeArguments()[0];
    }
    
    public T findById(Long id) {
        return entityManager.find(entityClass, id);
    }
}

// Concrete subclass "captures" the type
public class UserDao extends GenericDao<User> {
    // entityClass is automatically User.class!
}
```

This pattern is used extensively in Spring Data.

---

## Chapter 7: Heap Pollution

Heap pollution occurs when a variable of a parameterized type refers to an object of an incompatible type.

### How It Happens

```java
// Don't do this!
@SuppressWarnings("unchecked")
public static void main(String[] args) {
    List<String> strings = new ArrayList<>();
    strings.add("hello");
    
    List rawList = strings;  // Warning: unchecked
    rawList.add(42);         // No compile error (raw type)!
    
    // Later...
    String s = strings.get(1);  // ClassCastException!
}
```

### The Warning Signs

When you see:
```
Note: SomeClass.java uses unchecked or unsafe operations.
```

Pay attention! You might be introducing heap pollution.

### Safe Varargs

Varargs with generics can cause heap pollution:

```java
// Potential heap pollution!
public static <T> List<T> asList(T... elements) {
    // elements is actually Object[]!
    // If someone passes different types, bad things happen
}

// Use @SafeVarargs when you know it's safe:
@SafeVarargs
public static <T> List<T> asList(T... elements) {
    return Arrays.asList(elements);
}
```

---

## Chapter 8: What Information IS Preserved?

Not everything is erased. These survive:

### 1. Class-Level Type Arguments

```java
public class StringList extends ArrayList<String> { }

// This information is preserved!
Type superclass = StringList.class.getGenericSuperclass();
ParameterizedType pt = (ParameterizedType) superclass;
System.out.println(pt.getActualTypeArguments()[0]);  // class java.lang.String
```

### 2. Field Type Arguments

```java
public class MyClass {
    private List<String> names;
}

Field field = MyClass.class.getDeclaredField("names");
ParameterizedType type = (ParameterizedType) field.getGenericType();
System.out.println(type.getActualTypeArguments()[0]);  // class java.lang.String
```

### 3. Method Parameter and Return Types

```java
public class MyClass {
    public Map<String, Integer> getMap() { return null; }
}

Method method = MyClass.class.getMethod("getMap");
ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();
// Can inspect String and Integer from this!
```

### What's NOT Preserved

- Local variable type arguments
- Instance-level type arguments (what T is for a specific instance)

---

## Chapter 9: Practical Implications for Spring

### Why Spring Needs TypeReference

```java
// Jackson uses TypeReference for complex types
ObjectMapper mapper = new ObjectMapper();

// Simple type - Class token works:
User user = mapper.readValue(json, User.class);

// Complex type - need TypeReference:
List<User> users = mapper.readValue(json, new TypeReference<List<User>>() {});
```

### Spring's ResolvableType

Spring provides `ResolvableType` for working with generic type information:

```java
// Get type information from a field
ResolvableType type = ResolvableType.forField(
    MyClass.class.getDeclaredField("users"));

// Inspect nested generics
ResolvableType listType = type;  // List<User>
ResolvableType elementType = listType.getGeneric(0);  // User
Class<?> rawType = elementType.resolve();  // User.class
```

### Why @Autowired Works with Generics

```java
@Service
public class UserService {
    @Autowired
    private Repository<User> userRepo;  // How does Spring know to inject UserRepository?
}
```

Spring uses reflection on the field to get the generic type information (since field types ARE preserved).

---

## Chapter 10: Common Gotchas

### Gotcha 1: Array Creation

```java
// ✗ Won't compile
public <T> T[] toArray(List<T> list) {
    return new T[list.size()];  // Error!
}

// ✓ Workaround with Class token
public <T> T[] toArray(List<T> list, Class<T> type) {
    @SuppressWarnings("unchecked")
    T[] array = (T[]) Array.newInstance(type, list.size());
    return list.toArray(array);
}
```

### Gotcha 2: Static Context

```java
public class Box<T> {
    // ✗ Can't use T in static context
    private static T defaultValue;  // Error!
    
    public static T getDefault() {  // Error!
        return defaultValue;
    }
    
    // Why? Because T is instance-specific, but static is class-wide.
    // Box<String> and Box<Integer> share the same static members!
}
```

### Gotcha 3: instanceof with Generics

```java
public <T> void process(Object obj) {
    // ✗ Can't check specific generic type
    if (obj instanceof List<String>) { }  // Error!
    
    // ✓ Can check raw type
    if (obj instanceof List<?>) { }
    
    // ✓ Then cast (with warning)
    @SuppressWarnings("unchecked")
    List<String> list = (List<String>) obj;  // Unchecked warning
}
```

---

## Chapter 11: Exercises

### Exercise 1: Predict the Output

```java
List<String> strings = new ArrayList<>();
List<Integer> integers = new ArrayList<>();

System.out.println(strings.getClass().getName());
System.out.println(integers.getClass().getName());
System.out.println(strings.getClass() == integers.getClass());
```

### Exercise 2: Fix the Factory

```java
// This won't compile - fix it
public class Factory<T> {
    public T create() {
        return new T();  // Error!
    }
}
```

### Exercise 3: Explain the Error

Why does this fail at runtime, not compile time?

```java
List<String> strings = new ArrayList<>();
List rawList = strings;
rawList.add(42);  // Compiles!
String s = strings.get(0);  // What happens here?
```

### Exercise 4: Implement TypeToken

Create a simple type token class that preserves generic type information:

```java
public abstract class TypeToken<T> {
    private final Type type;
    
    protected TypeToken() {
        // Your implementation to extract type from anonymous subclass
    }
    
    public Type getType() {
        return type;
    }
}

// Should work like this:
TypeToken<Map<String, List<Integer>>> token = 
    new TypeToken<Map<String, List<Integer>>>() {};
System.out.println(token.getType());
// Should print: java.util.Map<java.lang.String, java.util.List<java.lang.Integer>>
```

---

## Quick Reference

### What's Erased
| Your Code | After Erasure |
|-----------|---------------|
| `T` | `Object` |
| `T extends Number` | `Number` |
| `List<String>` | `List` |
| `Box<T>` | `Box` |

### What's Preserved (via Reflection)
- Generic superclass of a class
- Generic interfaces of a class
- Generic type of fields
- Generic parameter types of methods
- Generic return types of methods

### Cannot Do at Runtime
- `new T()`
- `new T[]`
- `instanceof List<String>`
- `List<String>.class`
- Overload by generic type

---

## Key Takeaways

1. **Type erasure removes generic type info at runtime** - For backward compatibility
2. **The compiler inserts casts** - Maintains type safety
3. **Bridge methods enable polymorphism** - Work around erasure limitations
4. **Class<T> token pattern** - Pass type info explicitly when needed
5. **TypeReference pattern** - Capture complex generic types via anonymous class
6. **Some info is preserved** - Field, method, and superclass type arguments survive
7. **Avoid heap pollution** - Don't mix raw types with generics

---

## What's Next

In Part 7, we'll put everything together with:
- **Advanced Patterns Used in Spring**
- **Reading Spring Boot Source Code**
- **Contributing to Spring AI**
- **Real-World Generic Design Decisions**

You now have all the foundational knowledge. The final part shows how it all comes together in professional frameworks.