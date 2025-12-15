# Java Generics Mastery - Part 5: Wildcards

## The Big Picture

Wildcards (`?`) are about **flexibility in method parameters**. They answer: "How can I write a method that works with `List<Dog>`, `List<Cat>`, AND `List<Animal>`?"

This is harder than it sounds because of a fundamental truth:

> **`List<Dog>` is NOT a subtype of `List<Animal>`**

Wait, what? Let's understand why, then learn how wildcards solve this.

---

## Chapter 1: The Invariance Problem

### Why `List<Dog>` Isn't a Subtype of `List<Animal>`

It seems intuitive that since `Dog extends Animal`, `List<Dog>` should be usable wherever `List<Animal>` is expected. But watch what would happen:

```java
// Assume this worked (it doesn't!)
List<Dog> dogs = new ArrayList<>();
dogs.add(new Dog("Buddy"));

List<Animal> animals = dogs;  // Imagine this compiled...

animals.add(new Cat("Whiskers"));  // This would be legal for List<Animal>

Dog dog = dogs.get(1);  // BOOM! Actually a Cat!
```

If `List<Dog>` were a subtype of `List<Animal>`, you could add a Cat to a list of Dogs! Java prevents this at compile time.

### Arrays Have This Bug!

Actually, Java arrays ARE covariant, and it causes runtime errors:

```java
Dog[] dogs = new Dog[2];
Animal[] animals = dogs;  // This compiles! Arrays are covariant.

animals[0] = new Cat();  // Compiles! But...
// ArrayStoreException at runtime!
```

Generics were designed to avoid this mistake.

---

## Chapter 2: The Three Types of Wildcards

### 1. Unbounded Wildcard: `?`

"I don't care what type it is."

```java
public void printList(List<?> list) {
    for (Object item : list) {
        System.out.println(item);
    }
}

// Works with any List:
printList(Arrays.asList("A", "B", "C"));
printList(Arrays.asList(1, 2, 3));
printList(Arrays.asList(new Dog(), new Cat()));
```

### 2. Upper Bounded Wildcard: `? extends T`

"Any type that is T or a subtype of T."

```java
public double sumList(List<? extends Number> numbers) {
    double sum = 0;
    for (Number n : numbers) {
        sum += n.doubleValue();
    }
    return sum;
}

// Works with List<Number>, List<Integer>, List<Double>, etc.
sumList(Arrays.asList(1, 2, 3));           // List<Integer>
sumList(Arrays.asList(1.5, 2.5));          // List<Double>
sumList(Arrays.asList(1, 2.5, 3L));        // List<Number>
```

### 3. Lower Bounded Wildcard: `? super T`

"Any type that is T or a supertype of T."

```java
public void addDogs(List<? super Dog> list) {
    list.add(new Dog("Buddy"));
    list.add(new Dog("Max"));
}

// Works with List<Dog>, List<Animal>, List<Object>
List<Dog> dogs = new ArrayList<>();
List<Animal> animals = new ArrayList<>();
List<Object> objects = new ArrayList<>();

addDogs(dogs);      // ✓
addDogs(animals);   // ✓
addDogs(objects);   // ✓
```

---

## Chapter 3: The PECS Principle

**PECS: Producer Extends, Consumer Super**

This is the golden rule for choosing wildcards.

### Producer = You READ from it → Use `extends`

```java
// This collection PRODUCES items for us to read
public void processAnimals(List<? extends Animal> producer) {
    for (Animal animal : producer) {  // Reading - OK!
        animal.makeSound();
    }
    
    producer.add(new Dog());  // ✗ Compile error! Can't add.
}
```

Why can't you add? Because `producer` might be a `List<Cat>`, and you can't add a Dog to that!

### Consumer = You WRITE to it → Use `super`

```java
// This collection CONSUMES items we give it
public void fillWithDogs(List<? super Dog> consumer) {
    consumer.add(new Dog("Buddy"));   // Writing - OK!
    consumer.add(new Dog("Max"));     // Writing - OK!
    
    Dog dog = consumer.get(0);  // ✗ Compile error! Return type is Object.
}
```

Why can't you read as Dog? Because `consumer` might be a `List<Object>`, which could contain anything!

### Both = Use Exact Type or Both Bounds

```java
// Both reading and writing - need exact type
public void swap(List<Dog> list, int i, int j) {
    Dog temp = list.get(i);   // Reading
    list.set(i, list.get(j)); // Reading and writing
    list.set(j, temp);        // Writing
}
```

---

## Chapter 4: Wildcards in Real Code

### Example 1: Copy Method

```java
// From Collections class (simplified)
public static <T> void copy(List<? super T> dest, List<? extends T> src) {
//                               ↑ Consumer          ↑ Producer
    for (int i = 0; i < src.size(); i++) {
        T item = src.get(i);      // Read from producer
        dest.set(i, item);        // Write to consumer
    }
}

// Usage:
List<Integer> integers = Arrays.asList(1, 2, 3);
List<Number> numbers = new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0));

Collections.copy(numbers, integers);  // Copy integers into numbers list
```

### Example 2: addAll Method

```java
public interface Collection<E> {
    boolean addAll(Collection<? extends E> c);
    //                         ↑ Producer
}

// Why ? extends E?
List<Number> numbers = new ArrayList<>();
List<Integer> integers = Arrays.asList(1, 2, 3);
List<Double> doubles = Arrays.asList(1.5, 2.5);

numbers.addAll(integers);  // ✓ Integer extends Number
numbers.addAll(doubles);   // ✓ Double extends Number
```

### Example 3: Stream's collect

```java
// Simplified Collector interface
public interface Collector<T, A, R> {
    Supplier<A> supplier();
    BiConsumer<A, ? super T> accumulator();  // Consumes T
    //            ↑ Consumer
}

// The accumulator CONSUMES elements, so it uses ? super T
// This means it can accept T or any supertype
```

---

## Chapter 5: When to Use Each Wildcard

### Use `?` (Unbounded) When:

1. You only use `Object` methods
2. You don't care about the type at all
3. The method doesn't depend on the type parameter

```java
public int getSize(List<?> list) {
    return list.size();  // size() doesn't depend on element type
}

public void clear(List<?> list) {
    list.clear();  // clear() doesn't depend on element type
}
```

### Use `? extends T` When:

1. You only READ from the structure
2. You want to accept subtypes
3. The structure is a PRODUCER

```java
public double sum(Collection<? extends Number> numbers) {
    // Only reading, never adding
}

public void displayAll(List<? extends Shape> shapes) {
    // Only drawing, never adding shapes
}
```

### Use `? super T` When:

1. You only WRITE to the structure
2. You want to accept supertypes
3. The structure is a CONSUMER

```java
public void populateWithStrings(List<? super String> list) {
    list.add("Hello");
    list.add("World");
}

public void addDefaults(Collection<? super Integer> numbers) {
    numbers.add(0);
    numbers.add(-1);
}
```

---

## Chapter 6: Wildcards vs Type Parameters

A common confusion: when to use `<?>` vs `<T>`?

### Use Type Parameters When:

1. You need to relate multiple uses of the type
2. You need to use the type as a return type
3. You need type safety across a method

```java
// Need T in multiple places - use type parameter
public <T> T firstElement(List<T> list) {
    return list.get(0);  // Return type is T
}

// Need to ensure same type in two places
public <T> void copy(List<T> dest, List<T> src) {
    dest.addAll(src);  // Same T in both
}
```

### Use Wildcards When:

1. The type is used in only one place
2. You don't need to reference the type elsewhere
3. You want maximum flexibility

```java
// Type used only once - wildcard is fine
public void printAll(List<?> list) {
    for (Object o : list) {
        System.out.println(o);
    }
}

// Only reading - extends wildcard
public void processNumbers(List<? extends Number> numbers) {
    for (Number n : numbers) {
        System.out.println(n.doubleValue());
    }
}
```

### The Conversion Rule

Often you can convert between them:

```java
// These are equivalent for reading:
public <T extends Number> void process1(List<T> numbers) { }
public void process2(List<? extends Number> numbers) { }

// But type parameter needed if you use T elsewhere:
public <T extends Number> T getFirst(List<T> numbers) {
    return numbers.get(0);  // Returns T, not just Number
}
```

---

## Chapter 7: Wildcard Capture

Sometimes the compiler needs to "capture" a wildcard to work with it:

### The Helper Method Pattern

```java
public void swap(List<?> list, int i, int j) {
    // Problem: can't express "get and set same type"
    // list.set(i, list.get(j));  // ✗ Compile error!
    
    // Solution: delegate to a helper with type parameter
    swapHelper(list, i, j);
}

// Helper method "captures" the wildcard as T
private <T> void swapHelper(List<T> list, int i, int j) {
    T temp = list.get(i);
    list.set(i, list.get(j));
    list.set(j, temp);
}
```

### Why This Works

The compiler knows that `List<?>` has SOME specific type. The helper method captures that type as T. Inside the helper:
- `list.get(i)` returns T
- `list.set(i, T)` accepts T
- Everything type-checks!

---

## Chapter 8: Spring's Use of Wildcards

### ResponseEntity Methods

```java
public class ResponseEntity<T> {
    
    // Using wildcards for flexible factory methods
    public static ResponseEntity<?> notFound() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    
    // Compare with type parameter version
    public static <T> ResponseEntity<T> ok(T body) {
        return new ResponseEntity<>(body, HttpStatus.OK);
    }
}
```

### BeanFactory

```java
public interface BeanFactory {
    
    // Wildcard - we don't know the type
    boolean containsBean(String name);
    
    // Type parameter - return the actual type
    <T> T getBean(Class<T> requiredType);
    
    // Wildcard in parameter
    boolean isTypeMatch(String name, Class<?> typeToMatch);
}
```

### ApplicationContext

```java
public interface ApplicationContext {
    
    // Returns map with any value type
    <T> Map<String, T> getBeansOfType(Class<T> type);
    
    // Uses wildcard for the bean type
    String[] getBeanNamesForType(Class<?> type);
    
    // Combines both
    <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType);
}
```

---

## Chapter 9: Advanced Patterns

### Pattern 1: The getClass() Trick

```java
public class Copier {
    // Copy any object using its own class
    @SuppressWarnings("unchecked")
    public static <T> T copy(T original) {
        // getClass() returns Class<?>, need to cast
        Class<T> clazz = (Class<T>) original.getClass();
        return BeanUtils.instantiateClass(clazz);
    }
}
```

### Pattern 2: Self-Type with Wildcard

```java
public interface Comparable<T> {
    int compareTo(T other);
}

// Sometimes you see this pattern in APIs:
public static <T extends Comparable<? super T>> void sort(List<T> list) {
//                                 ↑
//              T's compareTo might accept a supertype!
}

// Why? Consider:
class Animal implements Comparable<Animal> { ... }
class Dog extends Animal { }  // Dog inherits Comparable<Animal>, not Comparable<Dog>

// With ? super T, this works:
List<Dog> dogs = ...;
sort(dogs);  // ✓ Dog extends Comparable<Animal>, Animal is super of Dog
```

### Pattern 3: Multiple Wildcards

```java
public static <K, V> void copyMap(
    Map<? extends K, ? extends V> source,  // Read from any subtype
    Map<? super K, ? super V> dest         // Write to any supertype
) {
    dest.putAll(source);
}

// Maximum flexibility:
Map<String, Integer> specific = new HashMap<>();
specific.put("one", 1);

Map<CharSequence, Number> general = new HashMap<>();
copyMap(specific, general);  // ✓ String extends CharSequence, Integer extends Number
```

---

## Chapter 10: Common Mistakes

### Mistake 1: Adding to `? extends` Collection

```java
List<? extends Number> numbers = new ArrayList<Integer>();
numbers.add(42);  // ✗ Compile error!

// You CAN add null (the only exception):
numbers.add(null);  // ✓ (but probably not useful)
```

### Mistake 2: Expecting Specific Type from `? super`

```java
List<? super Integer> list = new ArrayList<Number>();
Integer i = list.get(0);  // ✗ Compile error! Returns Object, not Integer
Object o = list.get(0);   // ✓ You only know it's at least Object
```

### Mistake 3: Using Wildcards for Type Relationships

```java
// ✗ Wrong - wildcards can't express relationships
public void process(List<?> input, List<?> output) {
    output.add(input.get(0));  // ✗ Error! The two ?'s are independent
}

// ✓ Right - use type parameter for relationships
public <T> void process(List<T> input, List<T> output) {
    output.add(input.get(0));  // ✓ Same T in both
}
```

### Mistake 4: Nesting Wildcards Incorrectly

```java
// This doesn't mean what you might think:
List<List<?>> lists;
// This is a list of "lists of unknown type"
// Each inner list could be a different type!

// If you want "list of lists of same type":
<T> List<List<T>> sameLists();
```

---

## Chapter 11: Exercises

### Exercise 1: Fix the Code

```java
public void addNumbers(List<? extends Number> numbers) {
    numbers.add(Integer.valueOf(42));  // Why doesn't this compile? Fix it.
}
```

### Exercise 2: Choose the Right Wildcard

For each method, choose the correct wildcard:

```java
// Read animals and process them
public void processAnimals(List<___Animal> animals) {
    for (Animal a : animals) {
        a.makeSound();
    }
}

// Add new animals to a collection
public void addAnimals(Collection<___Animal> destination) {
    destination.add(new Dog());
    destination.add(new Cat());
}

// Copy animals from source to destination
public void copyAnimals(
    List<___Animal> source,
    List<___Animal> destination) {
    destination.addAll(source);
}
```

### Exercise 3: Implement a Generic Comparator

```java
public class MaxFinder {
    // Find maximum element using provided comparator
    // Should work with List<Dog> and Comparator<Animal>
    public static <T> T findMax(
        List<? extends T> list,
        Comparator<___> comparator) {
        // Your implementation
    }
}
```

### Exercise 4: Analyze Spring Code

Explain why Spring uses these wildcards:

```java
// From GenericApplicationContext
public final <T> void registerBean(
    Class<T> beanClass,
    BeanDefinitionCustomizer... customizers) { }

public final <T> void registerBean(
    String beanName,
    Class<T> beanClass,
    Supplier<T> supplier,
    BeanDefinitionCustomizer... customizers) { }

// Why Class<T> and not Class<? extends T>?
// Why Supplier<T> and not Supplier<? extends T>?
```

---

## Quick Reference Card

| Wildcard | Meaning | Can Read As | Can Write |
|----------|---------|-------------|-----------|
| `<?>` | Unknown type | Object | null only |
| `<? extends T>` | T or subtype | T | null only |
| `<? super T>` | T or supertype | Object | T |
| `<T>` | Exact type T | T | T |

### PECS Summary
- **P**roducer **E**xtends → Use `? extends T` when you GET values
- **C**onsumer **S**uper → Use `? super T` when you PUT values

---

## Key Takeaways

1. **Generics are invariant** - `List<Dog>` is NOT a `List<Animal>`
2. **Wildcards provide flexibility** - Accept related types in methods
3. **PECS is the golden rule** - Producer Extends, Consumer Super
4. **`? extends T`** - Read-only, accepts subtypes
5. **`? super T`** - Write-friendly, accepts supertypes
6. **Use type parameters** - When you need to relate types or use as return type
7. **Wildcard capture** - Helper methods can "capture" `?` as a type parameter

---

## What's Next

In Part 6, we'll explore:
- **Type Erasure** - What really happens at runtime
- **Reification** - Why you can't do `new T()`
- **Working Around Erasure** - Patterns for runtime type information

Understanding erasure explains why generics work the way they do and helps you avoid runtime surprises.