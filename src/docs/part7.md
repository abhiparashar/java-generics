# Java Generics Mastery - Part 7: Advanced Patterns in Spring

## Putting It All Together

You've learned:
- Generic classes and interfaces
- Generic methods and type inference
- Bounded type parameters
- Wildcards and PECS
- Type erasure and workarounds

Now let's see how Spring uses ALL of these together. This is what you'll encounter when contributing to Spring Boot and Spring AI.

---

## Chapter 1: The Complete Repository Pattern

Let's trace through Spring Data's entire generic architecture:

### Level 1: The Foundation

```java
// The marker interface - no methods, just declares what it is
@Indexed
public interface Repository<T, ID> {
    // T = Entity type (User, Product, Order)
    // ID = Primary key type (Long, String, UUID)
}
```

### Level 2: Basic CRUD

```java
@NoRepositoryBean
public interface CrudRepository<T, ID> extends Repository<T, ID> {
    
    // Notice: <S extends T> - returns the SAME subtype you pass in
    <S extends T> S save(S entity);
    
    // Batch version - same pattern
    <S extends T> Iterable<S> saveAll(Iterable<S> entities);
    
    // Standard operations
    Optional<T> findById(ID id);
    boolean existsById(ID id);
    Iterable<T> findAll();
    Iterable<T> findAllById(Iterable<ID> ids);
    long count();
    void deleteById(ID id);
    void delete(T entity);
    void deleteAllById(Iterable<? extends ID> ids);  // Note the wildcard!
    void deleteAll(Iterable<? extends T> entities);
    void deleteAll();
}
```

**Why `<S extends T> S save(S entity)`?**

```java
// Without this pattern:
public class UserRepository {
    User save(User user);  // Always returns User
}

AdminUser admin = new AdminUser();
User saved = userRepo.save(admin);  // Returns User, not AdminUser!
AdminUser backToAdmin = (AdminUser) saved;  // Need to cast!

// With <S extends T> S save(S entity):
AdminUser saved = userRepo.save(admin);  // Returns AdminUser directly!
```

### Level 3: Pagination and Sorting

```java
@NoRepositoryBean
public interface PagingAndSortingRepository<T, ID> extends CrudRepository<T, ID> {
    
    Iterable<T> findAll(Sort sort);
    Page<T> findAll(Pageable pageable);
}

// Page<T> is itself generic:
public interface Page<T> extends Slice<T> {
    int getTotalPages();
    long getTotalElements();
    <U> Page<U> map(Function<? super T, ? extends U> converter);
    //               ↑ PECS! Consumes T, produces U
}
```

### Level 4: JPA Specifics

```java
@NoRepositoryBean
public interface JpaRepository<T, ID> extends 
        PagingAndSortingRepository<T, ID>,
        QueryByExampleExecutor<T> {
    
    @Override
    List<T> findAll();  // Returns List instead of Iterable
    
    @Override
    List<T> findAll(Sort sort);
    
    @Override
    <S extends T> List<S> saveAll(Iterable<S> entities);
    
    void flush();
    
    <S extends T> S saveAndFlush(S entity);
    
    <S extends T> List<S> saveAllAndFlush(Iterable<S> entities);
    
    // Example-based querying
    @Override
    <S extends T> List<S> findAll(Example<S> example);
}
```

### Level 5: Your Repository

```java
// When you write this:
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
}

// Spring generates an implementation where:
// T = User
// ID = Long
// All methods are now typed for User and Long
```

---

## Chapter 2: The ResponseEntity Deep Dive

### The Class Structure

```java
public class ResponseEntity<T> extends HttpEntity<T> {
    private final HttpStatusCode status;
    
    // Full constructor
    public ResponseEntity(@Nullable T body, 
                         @Nullable HttpHeaders headers, 
                         HttpStatusCode status) {
        super(body, headers);
        this.status = status;
    }
    
    // Static factories with their own type parameters
    public static <T> ResponseEntity<T> ok(@Nullable T body) {
        return ok().body(body);
    }
    
    public static <T> ResponseEntity<T> of(Optional<T> body) {
        return body.map(ResponseEntity::ok)
                   .orElseGet(() -> notFound().build());
    }
    
    // Builder pattern
    public static BodyBuilder ok() {
        return status(HttpStatus.OK);
    }
    
    public static BodyBuilder status(HttpStatusCode status) {
        return new DefaultBuilder(status);
    }
}
```

### The Builder Interface

```java
public interface BodyBuilder extends HeadersBuilder<BodyBuilder> {
    
    // Notice: method-level type parameter
    <T> ResponseEntity<T> body(@Nullable T body);
}

// HeadersBuilder uses self-referential generic
public interface HeadersBuilder<B extends HeadersBuilder<B>> {
    B header(String headerName, String... headerValues);
    B headers(@Nullable HttpHeaders headers);
    <T> ResponseEntity<T> build();
}
```

### Usage Patterns

```java
// Pattern 1: Direct body
@GetMapping("/user/{id}")
public ResponseEntity<User> getUser(@PathVariable Long id) {
    return userService.findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
}

// Pattern 2: Builder with headers
@PostMapping("/users")
public ResponseEntity<User> createUser(@RequestBody User user) {
    User saved = userService.save(user);
    return ResponseEntity
        .created(URI.create("/users/" + saved.getId()))
        .header("X-Custom", "value")
        .body(saved);
}

// Pattern 3: Generic list
@GetMapping("/users")
public ResponseEntity<List<User>> getAllUsers() {
    return ResponseEntity.ok(userService.findAll());
}

// Pattern 4: Complex nested types
@GetMapping("/stats")
public ResponseEntity<Map<String, List<StatEntry>>> getStats() {
    return ResponseEntity.ok(statsService.getGroupedStats());
}
```

---

## Chapter 3: Spring AI Patterns

Spring AI uses generics extensively. Let's analyze real patterns:

### The Chat Client Interface

```java
public interface ChatClient {
    
    // Simple generation
    ChatResponse call(Prompt prompt);
    
    // Streaming with reactive types
    Flux<ChatResponse> stream(Prompt prompt);
}

// Generic model interface
public interface Model<TReq, TRes> {
    TRes call(TReq request);
}

// Chat model extends with specific types
public interface ChatModel extends Model<Prompt, ChatResponse> {
    
    default String call(String message) {
        return call(new Prompt(message))
            .getResult()
            .getOutput()
            .getContent();
    }
}
```

### Embedding Models

```java
public interface EmbeddingModel extends Model<EmbeddingRequest, EmbeddingResponse> {
    
    // Convenience method
    default List<Double> embed(String text) {
        return embed(List.of(text)).get(0);
    }
    
    default List<List<Double>> embed(List<String> texts) {
        return call(new EmbeddingRequest(texts))
            .getResults()
            .stream()
            .map(Embedding::getOutput)
            .toList();
    }
}
```

### Vector Store Pattern

```java
public interface VectorStore {
    
    void add(List<Document> documents);
    
    Optional<Boolean> delete(List<String> idList);
    
    // Search returns parameterized results
    List<Document> similaritySearch(SearchRequest request);
    
    default List<Document> similaritySearch(String query) {
        return similaritySearch(SearchRequest.query(query));
    }
}

// Generic similarity search with type safety
public interface SimilaritySearcher<T> {
    List<T> search(String query, int topK);
    List<T> search(String query, int topK, Filter filter);
}
```

### The Advisor Pattern (AOP-like)

```java
public interface CallAroundAdvisor {
    
    // Generic response type
    <T extends ChatResponse> T aroundCall(
        ChatClientRequest request,
        CallAroundAdvisorChain chain
    );
}

public interface CallAroundAdvisorChain {
    <T extends ChatResponse> T nextAroundCall(ChatClientRequest request);
}
```

---

## Chapter 4: Event System Architecture

Spring's event system shows elegant generic design:

### Event Interfaces

```java
// Base event
public abstract class ApplicationEvent extends EventObject {
    private final long timestamp;
    
    public ApplicationEvent(Object source) {
        super(source);
        this.timestamp = System.currentTimeMillis();
    }
}

// Generic listener
@FunctionalInterface
public interface ApplicationListener<E extends ApplicationEvent> 
        extends EventListener {
    
    void onApplicationEvent(E event);
    
    // Default method with bounded wildcard
    default boolean supportsAsyncExecution() {
        return true;
    }
}
```

### Publisher Pattern

```java
@FunctionalInterface
public interface ApplicationEventPublisher {
    
    // Wildcard allows any event subtype
    void publishEvent(ApplicationEvent event);
    
    // Generic overload for non-ApplicationEvent objects
    default void publishEvent(Object event) {
        publishEvent(new PayloadApplicationEvent<>(this, event));
    }
}

// Payload wrapper - makes any object an event
public class PayloadApplicationEvent<T> extends ApplicationEvent {
    private final T payload;
    
    public PayloadApplicationEvent(Object source, T payload) {
        super(source);
        this.payload = payload;
    }
    
    public T getPayload() {
        return payload;
    }
}
```

### Usage in Your Code

```java
// Custom event
public class UserCreatedEvent extends ApplicationEvent {
    private final User user;
    
    public UserCreatedEvent(Object source, User user) {
        super(source);
        this.user = user;
    }
    
    public User getUser() { return user; }
}

// Type-safe listener
@Component
public class UserCreatedListener implements ApplicationListener<UserCreatedEvent> {
    
    @Override
    public void onApplicationEvent(UserCreatedEvent event) {
        System.out.println("User created: " + event.getUser().getName());
    }
}

// Or with @EventListener (uses reflection for type matching)
@Component
public class UserEventHandler {
    
    @EventListener
    public void handleUserCreated(UserCreatedEvent event) {
        // Spring matches by parameter type!
    }
}
```

---

## Chapter 5: Converter Framework

Spring's conversion system is a masterclass in generics:

### Core Interfaces

```java
// Simple converter
@FunctionalInterface
public interface Converter<S, T> {
    @Nullable
    T convert(S source);
}

// Conditional converter
public interface ConditionalConverter {
    boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType);
}

// Combined
public interface ConditionalGenericConverter 
        extends GenericConverter, ConditionalConverter {
}

// Generic converter for complex cases
public interface GenericConverter {
    
    @Nullable
    Set<ConvertiblePair> getConvertibleTypes();
    
    @Nullable
    Object convert(@Nullable Object source, 
                   TypeDescriptor sourceType, 
                   TypeDescriptor targetType);
}
```

### Converter Registry

```java
public interface ConverterRegistry {
    
    void addConverter(Converter<?, ?> converter);
    
    <S, T> void addConverter(Class<S> sourceType, 
                             Class<T> targetType, 
                             Converter<? super S, ? extends T> converter);
    //                                 ↑ Consumer      ↑ Producer
    //                                 PECS in action!
    
    void addConverter(GenericConverter converter);
    
    void addConverterFactory(ConverterFactory<?, ?> factory);
    
    void removeConvertible(Class<?> sourceType, Class<?> targetType);
}
```

### Converter Factory Pattern

```java
// Factory for a family of converters
public interface ConverterFactory<S, R> {
    <T extends R> Converter<S, T> getConverter(Class<T> targetType);
}

// Example: String to any Enum
public class StringToEnumConverterFactory 
        implements ConverterFactory<String, Enum> {
    
    @Override
    public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnum<>(targetType);
    }
    
    private static class StringToEnum<T extends Enum> implements Converter<String, T> {
        private final Class<T> enumType;
        
        StringToEnum(Class<T> enumType) {
            this.enumType = enumType;
        }
        
        @Override
        public T convert(String source) {
            return Enum.valueOf(enumType, source.trim().toUpperCase());
        }
    }
}
```

---

## Chapter 6: Bean Factory Generics

### The Heart of Spring

```java
public interface BeanFactory {
    
    // Type-safe bean retrieval
    <T> T getBean(Class<T> requiredType) throws BeansException;
    
    <T> T getBean(String name, Class<T> requiredType) throws BeansException;
    
    // Object provider for lazy/optional beans
    <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType);
    
    <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType);
}

// ObjectProvider uses generics beautifully
public interface ObjectProvider<T> extends ObjectFactory<T>, Iterable<T> {
    
    T getObject() throws BeansException;
    
    T getObject(Object... args) throws BeansException;
    
    @Nullable
    T getIfAvailable() throws BeansException;
    
    default T getIfAvailable(Supplier<T> defaultSupplier) {
        T dependency = getIfAvailable();
        return dependency != null ? dependency : defaultSupplier.get();
    }
    
    @Nullable
    T getIfUnique() throws BeansException;
    
    default void ifAvailable(Consumer<T> dependencyConsumer) {
        T dependency = getIfAvailable();
        if (dependency != null) {
            dependencyConsumer.accept(dependency);
        }
    }
    
    default void ifUnique(Consumer<T> dependencyConsumer) {
        T dependency = getIfUnique();
        if (dependency != null) {
            dependencyConsumer.accept(dependency);
        }
    }
    
    Stream<T> stream();
    
    Stream<T> orderedStream();
}
```

### Usage in Your Code

```java
@Service
public class MyService {
    
    // ObjectProvider for optional dependency
    private final ObjectProvider<CacheManager> cacheManagerProvider;
    
    public MyService(ObjectProvider<CacheManager> cacheManagerProvider) {
        this.cacheManagerProvider = cacheManagerProvider;
    }
    
    public void doWork() {
        // Safe access - might be null
        CacheManager cache = cacheManagerProvider.getIfAvailable();
        
        // With default
        CacheManager cacheOrDefault = cacheManagerProvider
            .getIfAvailable(NoOpCacheManager::new);
        
        // Process all beans of this type
        cacheManagerProvider.stream()
            .forEach(manager -> manager.getCache("default").clear());
    }
}
```

---

## Chapter 7: Creating Your Own Generic Framework Code

### Example: Generic REST Client

```java
public class RestClient {
    
    private final RestTemplate restTemplate;
    
    public RestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    // Simple GET
    public <T> T get(String url, Class<T> responseType) {
        return restTemplate.getForObject(url, responseType);
    }
    
    // GET with path variables
    public <T> T get(String url, Class<T> responseType, Object... uriVariables) {
        return restTemplate.getForObject(url, responseType, uriVariables);
    }
    
    // For complex types (List<User>, Map<String, Object>, etc.)
    public <T> T get(String url, ParameterizedTypeReference<T> responseType) {
        return restTemplate.exchange(url, HttpMethod.GET, null, responseType).getBody();
    }
    
    // POST with request body
    public <T, R> R post(String url, T body, Class<R> responseType) {
        return restTemplate.postForObject(url, body, responseType);
    }
    
    // DELETE returning nothing
    public void delete(String url, Object... uriVariables) {
        restTemplate.delete(url, uriVariables);
    }
}

// Usage
RestClient client = new RestClient(new RestTemplate());

// Simple type
User user = client.get("/users/1", User.class);

// Complex type needs ParameterizedTypeReference
List<User> users = client.get("/users", new ParameterizedTypeReference<List<User>>() {});

Map<String, Object> data = client.get("/data", 
    new ParameterizedTypeReference<Map<String, Object>>() {});
```

### Example: Generic Result Monad

```java
public sealed interface Result<T> permits Result.Success, Result.Failure {
    
    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }
    
    static <T> Result<T> failure(Throwable error) {
        return new Failure<>(error);
    }
    
    static <T> Result<T> of(Supplier<T> supplier) {
        try {
            return success(supplier.get());
        } catch (Exception e) {
            return failure(e);
        }
    }
    
    boolean isSuccess();
    
    T getOrThrow();
    
    T getOrElse(T defaultValue);
    
    <U> Result<U> map(Function<? super T, ? extends U> mapper);
    
    <U> Result<U> flatMap(Function<? super T, Result<U>> mapper);
    
    record Success<T>(T value) implements Result<T> {
        @Override
        public boolean isSuccess() { return true; }
        
        @Override
        public T getOrThrow() { return value; }
        
        @Override
        public T getOrElse(T defaultValue) { return value; }
        
        @Override
        public <U> Result<U> map(Function<? super T, ? extends U> mapper) {
            return success(mapper.apply(value));
        }
        
        @Override
        public <U> Result<U> flatMap(Function<? super T, Result<U>> mapper) {
            return mapper.apply(value);
        }
    }
    
    record Failure<T>(Throwable error) implements Result<T> {
        @Override
        public boolean isSuccess() { return false; }
        
        @Override
        public T getOrThrow() {
            throw new RuntimeException(error);
        }
        
        @Override
        public T getOrElse(T defaultValue) { return defaultValue; }
        
        @Override
        @SuppressWarnings("unchecked")
        public <U> Result<U> map(Function<? super T, ? extends U> mapper) {
            return (Result<U>) this;  // Failure carries through
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public <U> Result<U> flatMap(Function<? super T, Result<U>> mapper) {
            return (Result<U>) this;
        }
    }
}

// Usage
Result<User> result = Result.of(() -> userService.findById(id))
    .map(user -> enrichUser(user))
    .flatMap(user -> validateUser(user));

String name = result
    .map(User::getName)
    .getOrElse("Unknown");
```

---

## Chapter 8: Reading Spring Source Code

### Tips for Understanding Generic Code

1. **Start from concrete usage**, trace to interface
2. **Identify type parameter conventions**: T=Type, E=Element, K=Key, V=Value
3. **Look for PECS patterns** in method signatures
4. **Check for bridge methods** when override behavior seems odd
5. **Use your IDE** to resolve type parameters at call sites

### Practice Exercise: Analyze This Code

```java
public interface ReactiveRepository<T, ID> extends Repository<T, ID> {
    
    <S extends T> Mono<S> save(S entity);
    
    <S extends T> Flux<S> saveAll(Iterable<S> entities);
    
    <S extends T> Flux<S> saveAll(Publisher<S> entityStream);
    
    Mono<T> findById(ID id);
    
    Mono<T> findById(Publisher<ID> id);
    
    Flux<T> findAllById(Iterable<ID> ids);
    
    Flux<T> findAllById(Publisher<ID> idStream);
    
    Mono<Boolean> existsById(ID id);
    
    Mono<Boolean> existsById(Publisher<ID> id);
    
    Flux<T> findAll();
    
    Mono<Long> count();
    
    Mono<Void> deleteById(ID id);
    
    Mono<Void> delete(T entity);
    
    Mono<Void> deleteAll(Iterable<? extends T> entities);
    
    Mono<Void> deleteAll(Publisher<? extends T> entityStream);
    
    Mono<Void> deleteAll();
}
```

**Questions to answer:**
1. Why `<S extends T>` for save but just `T` for find?
2. Why `Publisher<? extends T>` for delete but `Publisher<S>` for saveAll?
3. What's the purpose of `Publisher<ID>` overloads?

---

## Chapter 9: Contributing to Spring

### Checklist Before Contributing

- [ ] Understand the existing generic patterns
- [ ] Follow naming conventions (T, E, K, V, S, U)
- [ ] Use appropriate wildcards (PECS)
- [ ] Consider type inference at call sites
- [ ] Test with various type arguments
- [ ] Don't break backward compatibility
- [ ] Add proper @SuppressWarnings with comments

### Common Review Comments

```java
// ✗ Might get this feedback
public void addAll(List<Object> items) { }

// ✓ More flexible with wildcard
public void addAll(List<?> items) { }

// ✓ Even better if you need element type
public void addAll(List<? extends T> items) { }
```

```java
// ✗ Unnecessarily specific
public <T extends Serializable & Comparable<T>> void process(T item) { }

// ✓ Only bound what you need
public <T extends Comparable<T>> void process(T item) { }
```

---

## Final Exercises

### Exercise 1: Implement a Generic Cache with TTL

```java
public interface Cache<K, V> {
    void put(K key, V value, Duration ttl);
    Optional<V> get(K key);
    void evict(K key);
    void clear();
    
    // Implement with proper generics
    <T extends V> void putAll(Map<? extends K, T> entries, Duration ttl);
    Map<K, V> getAll(Set<? extends K> keys);
}
```

### Exercise 2: Create a Generic Pipeline

```java
public interface Pipeline<I, O> {
    O process(I input);
    
    default <R> Pipeline<I, R> andThen(Pipeline<? super O, R> next) {
        // Implement
    }
    
    static <T> Pipeline<T, T> identity() {
        // Implement
    }
}
```

### Exercise 3: Analyze and Improve

Take any generic code you've written and ask:
1. Could wildcards make it more flexible?
2. Are bounds too restrictive?
3. Could type inference be cleaner for callers?

---

## Congratulations!

You've completed the Java Generics Mastery series. You now understand:

1. **Why generics exist** - Type safety and elimination of casts
2. **Generic classes and interfaces** - Parameterized types
3. **Generic methods** - Method-level type parameters
4. **Bounded type parameters** - Constraints on type arguments
5. **Wildcards** - Flexibility in method parameters
6. **Type erasure** - Runtime behavior and workarounds
7. **Advanced patterns** - Real-world Spring usage

You're ready to read and contribute to Spring Boot, Spring AI, and any other Java framework. The patterns you've learned here are universal in professional Java development.

**Next Steps:**
1. Clone Spring Boot repository
2. Pick a small issue labeled "good first issue"
3. Read the related code using your new generics knowledge
4. Make your contribution!

Good luck! 🚀