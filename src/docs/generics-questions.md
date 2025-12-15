# Java Generics Practice Questions: Easy to Hard

## Section 1: Introduction and Core Concepts

### Easy Questions
1. **Basic Generic Class**: Create a generic `Box<T>` class that can store any type of object. Include methods to set and get the value.

2. **Type Safety**: Explain why this code won't compile and fix it:
   ```java
   List<String> strings = new ArrayList<>();
   strings.add("Hello");
   strings.add(42); // Error here
   ```

3. **Generic Method**: Write a generic method that swaps two elements in an array.

### Medium Questions
4. **Generic Interface**: Create a generic `Converter<T, R>` interface with a method `R convert(T input)`. Implement it for String to Integer conversion.

5. **Multiple Type Parameters**: Design a generic `Triple<A, B, C>` class that can hold three different types of objects with appropriate getters and setters.

6. **Generic Collections**: Create a method that takes a `List<? extends Number>` and returns the sum of all elements as a double.

### Hard Questions
7. **Complex Generic Hierarchy**: Design a generic repository pattern with the following requirements:
    - Base interface `Repository<T, ID>`
    - `CrudRepository<T, ID>` extending Repository with basic CRUD operations
    - `PagingRepository<T, ID>` extending CrudRepository with pagination support
    - Include at least 5 methods in each interface

8. **Type Inference Challenge**: Explain why this code works and what types are inferred:
   ```java
   Map<String, List<Integer>> map = new HashMap<>();
   map.computeIfAbsent("key", k -> new ArrayList<>()).add(42);
   ```

9. **Generic Factory Pattern**: Implement a type-safe factory that can create instances of different types based on a Class parameter, with proper error handling.

---

## Section 2: Type Parameters and Naming Conventions

### Easy Questions
10. **Naming Convention**: Identify the issues with these type parameter names and suggest improvements:
    ```java
    public class MyClass<Type1, Type2, Type3> { }
    public interface MyMap<Key123, Value456> { }
    ```

11. **Single Type Parameter**: Create a generic `Stack<T>` class with push, pop, peek, and isEmpty methods.

12. **Type Parameter Bounds**: Write a method signature that accepts only classes that extend Number.

### Medium Questions
13. **Multiple Bounded Parameters**: Create a method that takes two type parameters where the first extends Comparable and the second extends Serializable.

14. **Recursive Type Parameters**: Implement a generic builder pattern where the builder method returns the correct subtype.

15. **Convention Application**: Design interfaces for a data processing pipeline using proper generic naming conventions for at least 4 different type parameters.

### Hard Questions
16. **Complex Type Parameter Relationships**: Create a generic event system where:
    - Events have a type parameter for the payload
    - Handlers have type parameters for event type and result type
    - EventBus coordinates between them with proper type safety

17. **Self-Referential Type Parameters**: Implement a generic tree structure where nodes can reference their parent and children with full type safety.

18. **Advanced Naming Scenario**: Design a generic ORM framework with proper type parameter naming for Entity, ID, Repository, Query, and Result types.

---

## Section 3: Generic Classes and Interfaces

### Easy Questions
19. **Basic Generic Class**: Implement a generic `Pair<T, U>` with equals and hashCode methods.

20. **Generic Interface Implementation**: Create a generic `Predicate<T>` interface and implement it for checking if a number is positive.

21. **Simple Inheritance**: Create a generic `Animal<T>` class and a concrete `Dog` class that extends it.

### Medium Questions
22. **Generic Interface Composition**: Design interfaces for `Readable<T>`, `Writable<T>`, and `ReadWrite<T>` where ReadWrite extends both.

23. **Parameterized Inheritance**: Create a hierarchy where `Vehicle<T>` is the base, `Car<T>` extends it, and `SportsCar` is a concrete implementation.

24. **Generic Inner Classes**: Implement a generic `Tree<T>` class with a generic inner `Node<T>` class.

### Hard Questions
25. **Complex Generic Hierarchy**: Design a complete generic collection framework with:
    - `Collection<T>` interface
    - `List<T>`, `Set<T>`, `Queue<T>` interfaces
    - At least two concrete implementations for each
    - Proper inheritance relationships

26. **Generic Interface Segregation**: Design a media player system with generic interfaces for different media types, ensuring single responsibility and type safety.

27. **Advanced Generic Composition**: Create a generic workflow engine where steps can transform data from one type to another, with proper composition and type checking.

---

## Section 4: Generic Methods

### Easy Questions
28. **Basic Generic Method**: Write a method that finds the maximum element in an array of Comparable objects.

29. **Static Generic Method**: Create a static method that creates and returns a List containing a single element.

30. **Generic Method vs Class**: Explain when to use generic methods vs generic classes and provide examples.

### Medium Questions
31. **Multiple Type Parameters in Method**: Write a method that takes two different types and returns a Pair containing both.

32. **Generic Method Overloading**: Create overloaded generic methods that handle different scenarios (null safety, empty collections).

33. **Type Inference**: Write methods where the return type is inferred from the parameters, demonstrating different inference scenarios.

### Hard Questions
34. **Generic Method Resolution**: Explain and demonstrate how the compiler resolves generic method calls in complex inheritance hierarchies.

35. **Recursive Generic Methods**: Implement a generic method that can flatten nested collections of arbitrary depth.

36. **Advanced Generic Algorithms**: Implement a generic merge sort algorithm that works with any Comparable type, including custom comparison logic.

---

## Section 5: Bounded Type Parameters

### Easy Questions
37. **Upper Bound**: Create a method that accepts only Number subtypes and returns their sum.

38. **Interface Bound**: Write a generic method that works only with Serializable objects.

39. **Simple Multiple Bounds**: Create a method signature with a type that extends both Comparable and Cloneable.

### Medium Questions
40. **Complex Bounds**: Implement a sorting utility that works with objects that are both Comparable and have a specific interface you define.

41. **Nested Bounds**: Create a generic data structure where elements must extend a base class AND implement specific interfaces.

42. **Bound Inheritance**: Design a class hierarchy where generic bounds are inherited and refined in subclasses.

### Hard Questions
43. **Recursive Bounds with Enums**: Implement a type-safe enum pattern using recursive bounds (like `Enum<E extends Enum<E>>`).

44. **Multiple Interface Bounds**: Create a generic validation framework where validators must implement multiple interfaces and extend a base class.

45. **Advanced Bound Scenarios**: Design a generic expression evaluator where expressions have complex bounds involving multiple inheritance levels.

---

## Section 6: Wildcards and PECS Principle

### Easy Questions
46. **Basic Wildcards**: Explain the difference between `List<?>`, `List<? extends Number>`, and `List<? super Integer>`.

47. **Reading with Extends**: Write a method that calculates the sum of a list that could contain any type of Number.

48. **Writing with Super**: Write a method that adds integers to a list that can accept Integer or its supertypes.

### Medium Questions
49. **PECS Application**: Implement a `copy` method that copies elements from one list to another using appropriate wildcards.

50. **Wildcard Capture**: Write a helper method to swap elements in a `List<?>` using wildcard capture.

51. **Producer Consumer**: Design a data processing pipeline where producers generate data and consumers process it, using appropriate wildcards.

### Hard Questions
52. **Complex PECS Scenario**: Implement a generic event system where events can be published to handlers using proper producer/consumer patterns.

53. **Wildcard Inheritance**: Create a method that works with collections of objects in a complex inheritance hierarchy using wildcards appropriately.

54. **Advanced Wildcard Patterns**: Design a generic visitor pattern using wildcards where visitors can process objects of different but related types.

---

## Section 7: Type Erasure and Bridge Methods

### Easy Questions
55. **Type Erasure Basics**: Explain what happens to generic type information at runtime and why.

56. **Runtime Type Checking**: Show why `instanceof` doesn't work with parameterized types and provide alternatives.

57. **Array Creation**: Explain why you can't create arrays of parameterized types and show workarounds.

### Medium Questions
58. **Bridge Method Understanding**: Create an example where bridge methods are generated and explain their purpose.

59. **Erasure Implications**: Demonstrate scenarios where type erasure causes unexpected behavior and how to handle them.

60. **Generic Array Alternatives**: Implement a type-safe alternative to generic arrays using collections or other approaches.

### Hard Questions
61. **Bridge Method Analysis**: Analyze the bytecode or use reflection to examine bridge methods in a complex generic inheritance hierarchy.

62. **Type Erasure Workarounds**: Implement a generic class that preserves type information at runtime using techniques like type tokens.

63. **Erasure in Frameworks**: Explain how frameworks like Spring handle type erasure when doing dependency injection with generics.

---

## Section 8: Advanced Patterns

### Easy Questions
64. **Type Token**: Implement a basic type token class that can capture generic type information.

65. **Self-Bounded Simple**: Create a simple fluent interface using self-bounded generics.

66. **Generic Factory**: Implement a basic generic factory pattern.

### Medium Questions
67. **Phantom Types**: Implement a units-of-measure system using phantom types to prevent mixing incompatible units.

68. **Advanced Builder**: Create a complex builder pattern with multiple configuration steps using self-bounded generics.

69. **Generic Strategy**: Implement a strategy pattern where strategies are type-safe and can be composed.

### Hard Questions
70. **Complex Type Tokens**: Implement a full type token system that can handle nested generics and provides runtime type information.

71. **Advanced Phantom Types**: Create a state machine using phantom types to ensure compile-time state validation.

72. **Generic Monad Pattern**: Implement a generic monad pattern (like Optional or Either) with proper flatMap, map, and filter operations.

---

## Section 9: Real-World Use Cases

### Easy Questions
73. **DAO Pattern**: Implement a basic generic DAO with CRUD operations.

74. **Event Listener**: Create a type-safe event listener system for a simple application.

75. **Configuration Builder**: Build a configuration system using generic builders.

### Medium Questions
76. **Repository with Specifications**: Implement a repository pattern that supports dynamic query building with specifications.

77. **Generic Cache**: Create a generic caching system with TTL support and different eviction policies.

78. **Message Queue**: Implement a type-safe message queue system with different message types.

### Hard Questions
79. **Complete ORM Framework**: Design and implement a mini ORM framework with generic entity mapping, query building, and relationship handling.

80. **Generic Workflow Engine**: Create a workflow engine where steps can transform data between different types with validation and error handling.

81. **Event Sourcing System**: Implement an event sourcing system with generic events, aggregates, and projections.

---

## Section 10: Best Practices and Common Pitfalls

### Easy Questions
82. **Raw Types**: Identify and fix raw type usage in legacy code.

83. **Unnecessary Wildcards**: Simplify overly complex wildcard usage in method signatures.

84. **Generic Naming**: Improve poorly named generic type parameters in existing code.

### Medium Questions
85. **Wildcard Guidelines**: Refactor a class that incorrectly uses wildcards to follow PECS principles.

86. **Performance Issues**: Identify and fix performance issues caused by excessive boxing/unboxing in generic code.

87. **API Design**: Design a public API using generics that is both flexible and easy to use.

### Hard Questions
88. **Legacy Integration**: Design a strategy for introducing generics into a large legacy codebase without breaking existing functionality.

89. **Generic API Evolution**: Show how to evolve a generic API while maintaining backward compatibility.

90. **Complex Refactoring**: Refactor a complex non-generic system to use generics while maintaining all existing functionality.

---

## Section 11: Performance Considerations

### Easy Questions
91. **Boxing Overhead**: Identify where autoboxing occurs in generic collections and suggest optimizations.

92. **Memory Usage**: Compare memory usage between generic and non-generic collections.

93. **Primitive Collections**: When would you use specialized primitive collections instead of generic ones?

### Medium Questions
94. **Generic vs Specialized**: Benchmark and compare performance between generic collections and specialized alternatives.

95. **Cache-Friendly Generics**: Design generic data structures that are cache-friendly and explain the considerations.

96. **JIT Optimization**: Explain how the JIT compiler optimizes generic code and what patterns help or hinder optimization.

### Hard Questions
97. **Performance Profiling**: Profile a complex generic system and identify performance bottlenecks specific to generic usage.

98. **Memory-Optimized Generics**: Design a memory-optimized generic collection for scenarios with millions of objects.

99. **High-Performance Generic Framework**: Create a high-performance generic framework for real-time systems with minimal garbage collection.

---

## Section 12: Interview Questions and Scenarios

### Easy Questions
100. **Basic Interview Questions**:
    - What are Java generics and why were they introduced?
    - What is type erasure?
    - What does PECS stand for?

101. **Code Review**: Review and critique a piece of generic code for common issues.

102. **Quick Fixes**: Fix compilation errors in generic code snippets.

### Medium Questions
103. **Design Questions**: Design a generic solution for a given problem during a mock interview.

104. **Debugging Scenarios**: Debug complex generic code with compilation or runtime issues.

105. **Architecture Discussion**: Discuss the pros and cons of using generics in different architectural patterns.

### Hard Questions
106. **System Design with Generics**: Design a large-scale system using generics appropriately throughout the architecture.

107. **Advanced Debugging**: Debug and fix issues in a complex generic framework with multiple inheritance levels.

108. **Performance Interview**: Discuss and solve performance problems in generic code during a technical interview.

---

## Sections 13-17: Advanced Modern Java Features

### Easy Questions (Sections 13-15)
109. **Records with Generics**: Create generic record classes for common data structures.

110. **Sealed Classes**: Implement a generic sealed class hierarchy for representing different result types.

111. **Pattern Matching**: Use pattern matching with generic sealed classes.

### Medium Questions (Sections 13-15)
112. **Modern Collection Operations**: Implement generic stream operations for complex data transformations.

113. **Generic Collectors**: Create custom generic collectors for specialized data processing.

114. **Reactive Generics**: Implement generic reactive streams with proper type safety.

### Hard Questions (Sections 13-17)
115. **Complete Modern Framework**: Design a modern framework using all latest Java features with generics.

116. **Migration Strategy**: Plan and execute migration of legacy generic code to modern Java features.

117. **Advanced Integration**: Integrate generics with virtual threads, pattern matching, and other modern features.

---

## Practical Coding Challenges

### Challenge 1: Generic Data Processing Pipeline (Medium-Hard)
118. **Requirements**:
     - Create a pipeline that can transform data through multiple stages
     - Each stage can change the data type
     - Include error handling and validation
     - Support parallel processing
     - Provide monitoring and metrics

### Challenge 2: Type-Safe Configuration System (Hard)
119. **Requirements**:
     - Support nested configuration objects
     - Type-safe property access
     - Default values and validation
     - Environment-specific overrides
     - Change notification system

### Challenge 3: Generic Game Engine Components (Hard)
120. **Requirements**:
     - Entity-Component-System architecture using generics
     - Type-safe component queries
     - Generic event system
     - Resource management with generics
     - Serialization support

### Challenge 4: Distributed Computing Framework (Expert)
121. **Requirements**:
     - Generic task distribution system
     - Type-safe remote method calls
     - Generic data serialization
     - Fault tolerance and recovery
     - Load balancing with type constraints

### Challenge 5: AI/ML Framework with Generics (Expert)
122. **Requirements**:
     - Generic tensor operations
     - Type-safe neural network layers
     - Generic optimization algorithms
     - Model serialization and loading
     - Training pipeline with generics

---

## Debugging and Problem-Solving Scenarios

### Scenario 1: Generic Collection Issues
123. **Problem**: A team is experiencing ClassCastException in production despite using generics everywhere. Investigate and fix.

### Scenario 2: Performance Degradation
124. **Problem**: A generic-based system is 3x slower than expected. Profile and optimize.

### Scenario 3: API Design Conflict
125. **Problem**: Two teams need incompatible generic APIs to interoperate. Design a solution.

### Scenario 4: Legacy Integration
126. **Problem**: Integrate a modern generic framework with a legacy system that uses raw types.

### Scenario 5: Memory Leak Investigation
127. **Problem**: A generic caching system is causing memory leaks. Find and fix the root cause.

---

## Bonus: Real-World Project Ideas

### Project 1: Generic Web Framework
128. Build a complete web framework using generics for:
     - Request/response handling
     - Dependency injection
     - ORM integration
     - Validation framework
     - Testing utilities

### Project 2: Generic Microservices Platform
129. Create a microservices platform with:
     - Service discovery with type information
     - Generic message passing
     - Type-safe configuration management
     - Generic monitoring and metrics
     - Deployment pipeline

### Project 3: Generic Data Analytics Engine
130. Develop an analytics engine featuring:
     - Generic data ingestion
     - Type-safe transformations
     - Generic aggregation functions
     - Pluggable storage backends
     - Query optimization

---

## Solution Guidelines

### For Beginners (Questions 1-30):
- Focus on understanding basic syntax and concepts
- Practice with simple examples
- Don't worry about complex scenarios initially
- Use IDE help and compiler errors as learning tools

### For Intermediate (Questions 31-90):
- Challenge yourself with real-world scenarios
- Focus on understanding the "why" behind patterns
- Practice API design principles
- Study framework source code

### For Advanced (Questions 91-130):
- Work on complete systems and frameworks
- Focus on performance and scalability
- Contribute to open source projects
- Mentor others and explain complex concepts

### Study Approach:
1. **Pick 5-10 questions from your current level**
2. **Implement complete, working solutions**
3. **Test your solutions thoroughly**
4. **Optimize for performance where applicable**
5. **Review and refactor your code**
6. **Move to the next difficulty level**

### Success Metrics:
- **Easy**: Can solve without external help
- **Medium**: Can solve with minimal documentation lookup
- **Hard**: Can solve with research and planning
- **Expert**: Can solve and explain to others

Remember: The goal is not just to solve these problems, but to deeply understand the principles and be able to apply them in real-world scenarios. Focus on quality over quantity!