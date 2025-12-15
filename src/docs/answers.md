// ==========================================
// 1. GENERIC REPOSITORY SYSTEM FOR REAL PROJECT
// ==========================================

// Base Entity Interface
public interface BaseEntity<ID> {
ID getId();
void setId(ID id);
LocalDateTime getCreatedAt();
LocalDateTime getUpdatedAt();
void setCreatedAt(LocalDateTime createdAt);
void setUpdatedAt(LocalDateTime updatedAt);
}

// Abstract Base Entity
@MappedSuperclass
public abstract class AbstractEntity<ID> implements BaseEntity<ID> {
@Column(name = "created_at")
private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    @Override
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    @Override
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    @Override
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    @Override
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

// Generic Repository Interface with Advanced Features
public interface GenericRepository<T extends BaseEntity<ID>, ID extends Serializable> {

    // Basic CRUD operations
    T save(T entity);
    List<T> saveAll(Iterable<T> entities);
    Optional<T> findById(ID id);
    List<T> findAllById(Iterable<ID> ids);
    List<T> findAll();
    Page<T> findAll(Pageable pageable);
    long count();
    boolean existsById(ID id);
    void deleteById(ID id);
    void delete(T entity);
    void deleteAll(Iterable<T> entities);
    void deleteAll();
    
    // Advanced query operations
    List<T> findByExample(Example<T> example);
    Page<T> findByExample(Example<T> example, Pageable pageable);
    Optional<T> findOne(Specification<T> spec);
    List<T> findAll(Specification<T> spec);
    Page<T> findAll(Specification<T> spec, Pageable pageable);
    List<T> findAll(Specification<T> spec, Sort sort);
    long count(Specification<T> spec);
    
    // Custom query support
    <R> List<R> findAllProjected(Class<R> projectionClass);
    <R> Page<R> findAllProjected(Class<R> projectionClass, Pageable pageable);
    
    // Batch operations
    void flush();
    <S extends T> S saveAndFlush(S entity);
    void deleteInBatch(Iterable<T> entities);
    void deleteAllInBatch();
    
    // Soft delete support
    void softDelete(ID id);
    void softDelete(T entity);
    List<T> findAllActive();
    Page<T> findAllActive(Pageable pageable);
    Optional<T> findActiveById(ID id);
}

// Generic Repository Implementation
@Repository
@Transactional
public class GenericRepositoryImpl<T extends BaseEntity<ID>, ID extends Serializable>
implements GenericRepository<T, ID> {

    @PersistenceContext
    protected EntityManager entityManager;
    
    private final Class<T> entityClass;
    private final Class<ID> idClass;
    
    @SuppressWarnings("unchecked")
    public GenericRepositoryImpl() {
        Type[] actualTypeArguments = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
        this.entityClass = (Class<T>) actualTypeArguments[0];
        this.idClass = (Class<ID>) actualTypeArguments[1];
    }
    
    public GenericRepositoryImpl(Class<T> entityClass, Class<ID> idClass) {
        this.entityClass = entityClass;
        this.idClass = idClass;
    }
    
    @Override
    public T save(T entity) {
        if (entity.getId() == null) {
            entityManager.persist(entity);
            return entity;
        } else {
            return entityManager.merge(entity);
        }
    }
    
    @Override
    public List<T> saveAll(Iterable<T> entities) {
        List<T> result = new ArrayList<>();
        for (T entity : entities) {
            result.add(save(entity));
        }
        return result;
    }
    
    @Override
    public Optional<T> findById(ID id) {
        T entity = entityManager.find(entityClass, id);
        return Optional.ofNullable(entity);
    }
    
    @Override
    public List<T> findAllById(Iterable<ID> ids) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        
        List<ID> idList = new ArrayList<>();
        ids.forEach(idList::add);
        
        query.where(root.get("id").in(idList));
        return entityManager.createQuery(query).getResultList();
    }
    
    @Override
    public List<T> findAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        query.select(root);
        return entityManager.createQuery(query).getResultList();
    }
    
    @Override
    public Page<T> findAll(Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        
        // Apply sorting
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            for (Sort.Order order : pageable.getSort()) {
                if (order.isAscending()) {
                    orders.add(cb.asc(root.get(order.getProperty())));
                } else {
                    orders.add(cb.desc(root.get(order.getProperty())));
                }
            }
            query.orderBy(orders);
        }
        
        TypedQuery<T> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        
        List<T> content = typedQuery.getResultList();
        long total = count();
        
        return new PageImpl<>(content, pageable, total);
    }
    
    @Override
    public long count() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(entityClass);
        query.select(cb.count(root));
        return entityManager.createQuery(query).getSingleResult();
    }
    
    @Override
    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }
    
    @Override
    public void deleteById(ID id) {
        findById(id).ifPresent(this::delete);
    }
    
    @Override
    public void delete(T entity) {
        if (entityManager.contains(entity)) {
            entityManager.remove(entity);
        } else {
            entityManager.remove(entityManager.merge(entity));
        }
    }
    
    @Override
    public void deleteAll(Iterable<T> entities) {
        entities.forEach(this::delete);
    }
    
    @Override
    public void deleteAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaDelete<T> delete = cb.createCriteriaDelete(entityClass);
        delete.from(entityClass);
        entityManager.createQuery(delete).executeUpdate();
    }
    
    @Override
    public List<T> findByExample(Example<T> example) {
        // Implementation would use Example API
        throw new UnsupportedOperationException("Example queries not implemented in this demo");
    }
    
    @Override
    public Page<T> findByExample(Example<T> example, Pageable pageable) {
        throw new UnsupportedOperationException("Example queries not implemented in this demo");
    }
    
    @Override
    public Optional<T> findOne(Specification<T> spec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        
        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, query, cb);
            if (predicate != null) {
                query.where(predicate);
            }
        }
        
        try {
            return Optional.of(entityManager.createQuery(query).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<T> findAll(Specification<T> spec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        
        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, query, cb);
            if (predicate != null) {
                query.where(predicate);
            }
        }
        
        return entityManager.createQuery(query).getResultList();
    }
    
    @Override
    public Page<T> findAll(Specification<T> spec, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        
        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, query, cb);
            if (predicate != null) {
                query.where(predicate);
            }
        }
        
        // Apply sorting
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            for (Sort.Order order : pageable.getSort()) {
                if (order.isAscending()) {
                    orders.add(cb.asc(root.get(order.getProperty())));
                } else {
                    orders.add(cb.desc(root.get(order.getProperty())));
                }
            }
            query.orderBy(orders);
        }
        
        TypedQuery<T> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        
        List<T> content = typedQuery.getResultList();
        long total = count(spec);
        
        return new PageImpl<>(content, pageable, total);
    }
    
    @Override
    public List<T> findAll(Specification<T> spec, Sort sort) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        
        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, query, cb);
            if (predicate != null) {
                query.where(predicate);
            }
        }
        
        if (sort.isSorted()) {
            List<Order> orders = new ArrayList<>();
            for (Sort.Order order : sort) {
                if (order.isAscending()) {
                    orders.add(cb.asc(root.get(order.getProperty())));
                } else {
                    orders.add(cb.desc(root.get(order.getProperty())));
                }
            }
            query.orderBy(orders);
        }
        
        return entityManager.createQuery(query).getResultList();
    }
    
    @Override
    public long count(Specification<T> spec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(entityClass);
        query.select(cb.count(root));
        
        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, query, cb);
            if (predicate != null) {
                query.where(predicate);
            }
        }
        
        return entityManager.createQuery(query).getSingleResult();
    }
    
    @Override
    public <R> List<R> findAllProjected(Class<R> projectionClass) {
        // Implementation would use constructor expressions or projections
        throw new UnsupportedOperationException("Projections not implemented in this demo");
    }
    
    @Override
    public <R> Page<R> findAllProjected(Class<R> projectionClass, Pageable pageable) {
        throw new UnsupportedOperationException("Projections not implemented in this demo");
    }
    
    @Override
    public void flush() {
        entityManager.flush();
    }
    
    @Override
    public <S extends T> S saveAndFlush(S entity) {
        S saved = (S) save(entity);
        flush();
        return saved;
    }
    
    @Override
    public void deleteInBatch(Iterable<T> entities) {
        List<ID> ids = new ArrayList<>();
        entities.forEach(entity -> ids.add(entity.getId()));
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaDelete<T> delete = cb.createCriteriaDelete(entityClass);
        Root<T> root = delete.from(entityClass);
        delete.where(root.get("id").in(ids));
        entityManager.createQuery(delete).executeUpdate();
    }
    
    @Override
    public void deleteAllInBatch() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaDelete<T> delete = cb.createCriteriaDelete(entityClass);
        delete.from(entityClass);
        entityManager.createQuery(delete).executeUpdate();
    }
    
    @Override
    public void softDelete(ID id) {
        // Assumes entity has an 'active' field
        findById(id).ifPresent(this::softDelete);
    }
    
    @Override
    public void softDelete(T entity) {
        try {
            Method setActiveMethod = entity.getClass().getMethod("setActive", boolean.class);
            setActiveMethod.invoke(entity, false);
            save(entity);
        } catch (Exception e) {
            // If no setActive method, perform hard delete
            delete(entity);
        }
    }
    
    @Override
    public List<T> findAllActive() {
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<T> query = cb.createQuery(entityClass);
            Root<T> root = query.from(entityClass);
            query.where(cb.equal(root.get("active"), true));
            return entityManager.createQuery(query).getResultList();
        } catch (Exception e) {
            // If no active field, return all
            return findAll();
        }
    }
    
    @Override
    public Page<T> findAllActive(Pageable pageable) {
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<T> query = cb.createQuery(entityClass);
            Root<T> root = query.from(entityClass);
            query.where(cb.equal(root.get("active"), true));
            
            if (pageable.getSort().isSorted()) {
                List<Order> orders = new ArrayList<>();
                for (Sort.Order order : pageable.getSort()) {
                    if (order.isAscending()) {
                        orders.add(cb.asc(root.get(order.getProperty())));
                    } else {
                        orders.add(cb.desc(root.get(order.getProperty())));
                    }
                }
                query.orderBy(orders);
            }
            
            TypedQuery<T> typedQuery = entityManager.createQuery(query);
            typedQuery.setFirstResult((int) pageable.getOffset());
            typedQuery.setMaxResults(pageable.getPageSize());
            
            List<T> content = typedQuery.getResultList();
            
            // Count active entities
            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            Root<T> countRoot = countQuery.from(entityClass);
            countQuery.select(cb.count(countRoot));
            countQuery.where(cb.equal(countRoot.get("active"), true));
            long total = entityManager.createQuery(countQuery).getSingleResult();
            
            return new PageImpl<>(content, pageable, total);
        } catch (Exception e) {
            return findAll(pageable);
        }
    }
    
    @Override
    public Optional<T> findActiveById(ID id) {
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<T> query = cb.createQuery(entityClass);
            Root<T> root = query.from(entityClass);
            query.where(
                cb.and(
                    cb.equal(root.get("id"), id),
                    cb.equal(root.get("active"), true)
                )
            );
            
            T result = entityManager.createQuery(query).getSingleResult();
            return Optional.of(result);
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (Exception e) {
            return findById(id);
        }
    }
    
    protected Class<T> getEntityClass() {
        return entityClass;
    }
    
    protected Class<ID> getIdClass() {
        return idClass;
    }
    
    protected EntityManager getEntityManager() {
        return entityManager;
    }
}

// Concrete Entity Example
@Entity
@Table(name = "users")
public class User extends AbstractEntity<Long> {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private boolean active = true;
    
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;
    
    // Constructors
    public User() {}
    
    public User(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
    
    // Getters and setters
    @Override
    public Long getId() { return id; }
    @Override
    public void setId(Long id) { this.id = id; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(email, user.email);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", active=" + active +
                ", role=" + role +
                '}';
    }
}

// User Repository Interface
public interface UserRepository extends GenericRepository<User, Long> {
Optional<User> findByEmail(String email);
List<User> findByRole(UserRole role);
List<User> findByFirstNameContainingIgnoreCase(String firstName);
List<User> findByLastNameContainingIgnoreCase(String lastName);

    @Query("SELECT u FROM User u WHERE u.firstName LIKE %:name% OR u.lastName LIKE %:name%")
    List<User> findByNameContaining(@Param("name") String name);
}

// User Repository Implementation
@Repository
public class UserRepositoryImpl extends GenericRepositoryImpl<User, Long> implements UserRepository {

    public UserRepositoryImpl() {
        super(User.class, Long.class);
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        query.where(cb.equal(root.get("email"), email));
        
        try {
            User user = getEntityManager().createQuery(query).getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<User> findByRole(UserRole role) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        query.where(cb.equal(root.get("role"), role));
        return getEntityManager().createQuery(query).getResultList();
    }
    
    @Override
    public List<User> findByFirstNameContainingIgnoreCase(String firstName) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        query.where(cb.like(cb.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%"));
        return getEntityManager().createQuery(query).getResultList();
    }
    
    @Override
    public List<User> findByLastNameContainingIgnoreCase(String lastName) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        query.where(cb.like(cb.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%"));
        return getEntityManager().createQuery(query).getResultList();
    }
    
    @Override
    public List<User> findByNameContaining(String name) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        
        Predicate firstNamePredicate = cb.like(cb.lower(root.get("firstName")), "%" + name.toLowerCase() + "%");
        Predicate lastNamePredicate = cb.like(cb.lower(root.get("lastName")), "%" + name.toLowerCase() + "%");
        
        query.where(cb.or(firstNamePredicate, lastNamePredicate));
        return getEntityManager().createQuery(query).getResultList();
    }
}

// User Service using Generic Repository
@Service
@Transactional
public class UserService {
private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User createUser(String firstName, String lastName, String email, UserRole role) {
        validateEmail(email);
        
        User user = new User(firstName, lastName, email);
        user.setRole(role);
        return userRepository.save(user);
    }
    
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public List<User> findUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }
    
    public Page<User> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
    
    public List<User> searchUsersByName(String name) {
        return userRepository.findByNameContaining(name);
    }
    
    public User updateUser(Long id, String firstName, String lastName, String email) {
        return userRepository.findById(id)
            .map(user -> {
                if (email != null && !email.equals(user.getEmail())) {
                    validateEmail(email);
                    user.setEmail(email);
                }
                if (firstName != null) user.setFirstName(firstName);
                if (lastName != null) user.setLastName(lastName);
                return userRepository.save(user);
            })
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }
    
    public void deactivateUser(Long id) {
        userRepository.softDelete(id);
    }
    
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    public List<User> findActiveUsers() {
        return userRepository.findAllActive();
    }
    
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        
        userRepository.findByEmail(email).ifPresent(user -> {
            throw new IllegalArgumentException("Email already exists: " + email);
        });
    }
}

// Supporting Enums and Classes
public enum UserRole {
USER, ADMIN, MODERATOR
}

public class EntityNotFoundException extends RuntimeException {
public EntityNotFoundException(String message) {
super(message);
}
}

// ==========================================
// 2. TYPE-SAFE EVENT BUS SYSTEM
// ==========================================

// Base Event Class
public abstract class Event {
private final String eventId;
private final LocalDateTime timestamp;
private final String source;

    protected Event(String source) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.source = source;
    }
    
    public String getEventId() { return eventId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getSource() { return source; }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "eventId='" + eventId + '\'' +
                ", timestamp=" + timestamp +
                ", source='" + source + '\'' +
                '}';
    }
}

// Event Listener Interface
@FunctionalInterface
public interface EventListener<T extends Event> {
void onEvent(T event);

    // Default method for error handling
    default void onError(T event, Exception exception) {
        System.err.println("Error processing event " + event + ": " + exception.getMessage());
    }
    
    // Default method for filtering
    default boolean canHandle(T event) {
        return true;
    }
}

// Async Event Listener
@FunctionalInterface
public interface AsyncEventListener<T extends Event> extends EventListener<T> {
CompletableFuture<Void> onEventAsync(T event);

    @Override
    default void onEvent(T event) {
        onEventAsync(event).exceptionally(throwable -> {
            onError(event, (Exception) throwable);
            return null;
        });
    }
}

// Event Handler Registration
public class EventHandlerRegistration<T extends Event> {
private final Class<T> eventType;
private final EventListener<T> listener;
private final int priority;
private final boolean async;
private final String handlerId;

    public EventHandlerRegistration(Class<T> eventType, EventListener<T> listener, int priority, boolean async) {
        this.eventType = eventType;
        this.listener = listener;
        this.priority = priority;
        this.async = async;
        this.handlerId = UUID.randomUUID().toString();
    }
    
    public Class<T> getEventType() { return eventType; }
    public EventListener<T> getListener() { return listener; }
    public int getPriority() { return priority; }
    public boolean isAsync() { return async; }
    public String getHandlerId() { return handlerId; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventHandlerRegistration<?> that = (EventHandlerRegistration<?>) o;
        return Objects.equals(handlerId, that.handlerId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(handlerId);
    }
}

// Type-Safe Event Bus
@Component
public class TypeSafeEventBus {
private final Map<Class<? extends Event>, List<EventHandlerRegistration<? extends Event>>> listeners = new ConcurrentHashMap<>();
private final ExecutorService asyncExecutor;
private final List<EventInterceptor> interceptors = new CopyOnWriteArrayList<>();

    public TypeSafeEventBus() {
        this.asyncExecutor = Executors.newFixedThreadPool(10, r -> {
            Thread thread = new Thread(r, "EventBus-Async-" + r.hashCode());
            thread.setDaemon(true);
            return thread;
        });
    }
    
    // Register event listener with default priority
    public <T extends Event> EventHandlerRegistration<T> subscribe(Class<T> eventType, EventListener<T> listener) {
        return subscribe(eventType, listener, 0, false);
    }
    
    // Register async event listener
    public <T extends Event> EventHandlerRegistration<T> subscribeAsync(Class<T> eventType, AsyncEventListener<T> listener) {
        return subscribe(eventType, listener, 0, true);
    }
    
    // Register event listener with priority
    public <T extends Event> EventHandlerRegistration<T> subscribe(Class<T> eventType, EventListener<T> listener, int priority) {
        return subscribe(eventType, listener, priority, false);
    }
    
    // Register event listener with full configuration
    public <T extends Event> EventHandlerRegistration<T> subscribe(Class<T> eventType, EventListener<T> listener, int priority, boolean async) {
        EventHandlerRegistration<T> registration = new EventHandlerRegistration<>(eventType, listener, priority, async);
        
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(registration);
        
        // Sort by priority (higher priority first)
        listeners.get(eventType).sort((r1, r2) -> Integer.compare(r2.getPriority(), r1.getPriority()));
        
        return registration;
    }
    
    // Unsubscribe event listener
    public <T extends Event> boolean unsubscribe(EventHandlerRegistration<T> registration) {
        List<EventHandlerRegistration<? extends Event>> eventListeners = listeners.get(registration.getEventType());
        return eventListeners != null && eventListeners.remove(registration);
    }
    
    // Unsubscribe all listeners for an event type
    public <T extends Event> void unsubscribeAll(Class<T> eventType) {
        listeners.remove(eventType);
    }
    
    // Publish event
    public <T extends Event> void publish(T event) {
        if (event == null) return;
        
        // Apply interceptors
        for (EventInterceptor interceptor : interceptors) {
            event = interceptor.beforePublish(event);
            if (event == null) return; // Event was filtered out
        }
        
        List<EventHandlerRegistration<? extends Event>> eventListeners = getListenersForEvent(event.getClass());
        
        for (EventHandlerRegistration<? extends Event> registration : eventListeners) {
            handleEvent(event, registration);
        }
        
        // Apply post-publish interceptors
        for (EventInterceptor interceptor : interceptors) {
            interceptor.afterPublish(event);
        }
    }
    
    // Publish event and wait for completion
    public <T extends Event> CompletableFuture<Void> publishAsync(T event) {
        if (event == null) return CompletableFuture.completedFuture(null);
        
        return CompletableFuture.runAsync(() -> publish(event), asyncExecutor);
    }
    
    // Get listeners for event type (including parent types)
    @SuppressWarnings("unchecked")
    private List<EventHandlerRegistration<? extends Event>> getListenersForEvent(Class<? extends Event> eventType) {
        List<EventHandlerRegistration<? extends Event>> result = new ArrayList<>();
        
        // Get direct listeners
        List<EventHandlerRegistration<? extends Event>> directListeners = listeners.get(eventType);
        if (directListeners != null) {
            result.addAll(directListeners);
        }
        
        // Get listeners for parent types
        for (Map.Entry<Class<? extends Event>, List<EventHandlerRegistration<? extends Event>>> entry : listeners.entrySet()) {
            Class<? extends Event> listenerEventType = entry.getKey();
            if (listenerEventType != eventType && listenerEventType.isAssignableFrom(eventType)) {
                result.addAll(entry.getValue());
            }
        }
        
        // Sort by priority
        result.sort((r1, r2) -> Integer.compare(r2.getPriority(), r1.getPriority()));
        
        return result;
    }
    
    // Handle individual event
    @SuppressWarnings("unchecked")
    private <T extends Event> void handleEvent(T event, EventHandlerRegistration<? extends Event> registration) {
        try {
            EventListener<T> listener = (EventListener<T>) registration.getListener();
            
            // Check if listener can handle this event
            if (!listener.canHandle(event)) {
                return;
            }
            
            if (registration.isAsync()) {
                asyncExecutor.submit(() -> {
                    try {
                        listener.onEvent(event);
                    } catch (Exception e) {
                        listener.onError(event, e);
                    }
                });
            } else {
                listener.onEvent(event);
            }
        } catch (Exception e) {
            EventListener<T> listener = (EventListener<T>) registration.getListener();
            listener.onError(event, e);
        }
    }
    
    // Add interceptor
    public void addInterceptor(EventInterceptor interceptor) {
        interceptors.add(interceptor);
    }
    
    // Remove interceptor
    public void removeInterceptor(EventInterceptor interceptor) {
        interceptors.remove(interceptor);
    }
    
    // Get statistics
    public Map<Class<? extends Event>, Integer> getListenerCounts() {
        return listeners.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().size()
            ));
    }
    
    // Shutdown
    @PreDestroy
    public void shutdown() {
        asyncExecutor.shutdown();
        try {
            if (!asyncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                asyncExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            asyncExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

// Event Interceptor Interface
public interface EventInterceptor {
<T extends Event> T beforePublish(T event);
<T extends Event> void afterPublish(T event);
}

// Logging Interceptor
@Component
public class LoggingEventInterceptor implements EventInterceptor {
private static final Logger logger = LoggerFactory.getLogger(LoggingEventInterceptor.class);

    @Override
    public <T extends Event> T beforePublish(T event) {
        logger.debug("Publishing event: {}", event);
        return event;
    }
    
    @Override
    public <T extends Event> void afterPublish(T event) {
        logger.debug("Event published successfully: {}", event);
    }
}

// Concrete Event Examples
public class UserCreatedEvent extends Event {
private final User user;

    public UserCreatedEvent(User user, String source) {
        super(source);
        this.user = user;
    }
    
    public User getUser() {
        return user;
    }
    
    @Override
    public String toString() {
        return "UserCreatedEvent{" +
                "user=" + user +
                ", " + super.toString() +
                '}';
    }
}

public class UserUpdatedEvent extends Event {
private final User oldUser;
private final User newUser;

    public UserUpdatedEvent(User oldUser, User newUser, String source) {
        super(source);
        this.oldUser = oldUser;
        this.newUser = newUser;
    }
    
    public User getOldUser() { return oldUser; }
    public User getNewUser() { return newUser; }
    
    @Override
    public String toString() {
        return "UserUpdatedEvent{" +
                "oldUser=" + oldUser +
                ", newUser=" + newUser +
                ", " + super.toString() +
                '}';
    }
}

public class UserDeletedEvent extends Event {
private final User user;
private final boolean softDelete;

    public UserDeletedEvent(User user, boolean softDelete, String source) {
        super(source);
        this.user = user;
        this.softDelete = softDelete;
    }
    
    public User getUser() { return user; }
    public boolean isSoftDelete() { return softDelete; }
    
    @Override
    public String toString() {
        return "UserDeletedEvent{" +
                "user=" + user +
                ", softDelete=" + softDelete +
                ", " + super.toString() +
                '}';
    }
}

// Event Handlers
@Component
public class UserEventHandlers {
private static final Logger logger = LoggerFactory.getLogger(UserEventHandlers.class);

    @Autowired
    private TypeSafeEventBus eventBus;
    
    @PostConstruct
    public void registerHandlers() {
        // High priority sync handler for user creation
        eventBus.subscribe(UserCreatedEvent.class, this::onUserCreated, 100);
        
        // Async handler for sending welcome email
        eventBus.subscribeAsync(UserCreatedEvent.class, this::sendWelcomeEmailAsync);
        
        // Sync handler for user updates
        eventBus.subscribe(UserUpdatedEvent.class, this::onUserUpdated);
        
        // Async handler for audit logging
        eventBus.subscribeAsync(UserUpdatedEvent.class, this::auditUserUpdateAsync);
        
        // Handler for user deletion
        eventBus.subscribe(UserDeletedEvent.class, this::onUserDeleted);
    }
    
    private void onUserCreated(UserCreatedEvent event) {
        logger.info("User created: {}", event.getUser().getEmail());
        // Immediate processing logic
    }
    
    private CompletableFuture<Void> sendWelcomeEmailAsync(UserCreatedEvent event) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Simulate email sending
                Thread.sleep(1000);
                logger.info("Welcome email sent to: {}", event.getUser().getEmail());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Email sending interrupted", e);
            }
        });
    }
    
    private void onUserUpdated(UserUpdatedEvent event) {
        logger.info("User updated: {} -> {}", event.getOldUser().getEmail(), event.getNewUser().getEmail());
        // Immediate processing logic
    }
    
    private CompletableFuture<Void> auditUserUpdateAsync(UserUpdatedEvent event) {
        return CompletableFuture.runAsync(() -> {
            logger.info("Audit: User {} was updated", event.getNewUser().getId());
            // Audit logging logic
        });
    }
    
    private void onUserDeleted(UserDeletedEvent event) {
        String deleteType = event.isSoftDelete() ? "soft deleted" : "hard deleted";
        logger.info("User {}: {}", deleteType, event.getUser().getEmail());
        // Cleanup logic
    }
}

// ==========================================
// 3. FLUENT API WITH SELF-BOUNDED GENERICS
// ==========================================

// Self-bounded builder base class
public abstract class FluentBuilder<T, B extends FluentBuilder<T, B>> {
protected abstract B self();
public abstract T build();

    // Common validation method
    protected void validate() {
        // Override in subclasses for validation
    }
    
    // Conditional building
    public B when(boolean condition, Function<B, B> action) {
        return condition ? action.apply(self()) : self();
    }
    
    // Apply transformation
    public B apply(Function<B, B> transformation) {
        return transformation.apply(self());
    }
}

// HTTP Request Builder with Fluent API
public class HttpRequestBuilder extends FluentBuilder<HttpRequest, HttpRequestBuilder> {
private String url;
private HttpMethod method = HttpMethod.GET;
private final Map<String, String> headers = new HashMap<>();
private final Map<String, String> queryParams = new HashMap<>();
private String body;
private Duration timeout = Duration.ofSeconds(30);
private int maxRetries = 0;
private boolean followRedirects = true;

    private HttpRequestBuilder() {}
    
    public static HttpRequestBuilder create() {
        return new HttpRequestBuilder();
    }
    
    @Override
    protected HttpRequestBuilder self() {
        return this;
    }
    
    public HttpRequestBuilder url(String url) {
        this.url = url;
        return self();
    }
    
    public HttpRequestBuilder method(HttpMethod method) {
        this.method = method;
        return self();
    }
    
    public HttpRequestBuilder get() {
        return method(HttpMethod.GET);
    }
    
    public HttpRequestBuilder post() {
        return method(HttpMethod.POST);
    }
    
    public HttpRequestBuilder put() {
        return method(HttpMethod.PUT);
    }
    
    public HttpRequestBuilder delete() {
        return method(HttpMethod.DELETE);
    }
    
    public HttpRequestBuilder header(String name, String value) {
        headers.put(name, value);
        return self();
    }
    
    public HttpRequestBuilder headers(Map<String, String> headers) {
        this.headers.putAll(headers);
        return self();
    }
    
    public HttpRequestBuilder contentType(String contentType) {
        return header("Content-Type", contentType);
    }
    
    public HttpRequestBuilder json() {
        return contentType("application/json");
    }
    
    public HttpRequestBuilder xml() {
        return contentType("application/xml");
    }
    
    public HttpRequestBuilder authorization(String token) {
        return header("Authorization", "Bearer " + token);
    }
    
    public HttpRequestBuilder basicAuth(String username, String password) {
        String credentials = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        return header("Authorization", "Basic " + credentials);
    }
    
    public HttpRequestBuilder queryParam(String name, String value) {
        queryParams.put(name, value);
        return self();
    }
    
    public HttpRequestBuilder queryParams(Map<String, String> params) {
        queryParams.putAll(params);
        return self();
    }
    
    public HttpRequestBuilder body(String body) {
        this.body = body;
        return self();
    }
    
    public HttpRequestBuilder jsonBody(Object object) {
        // Assume we have a JSON serializer
        this.body = JsonUtil.toJson(object);
        return json();
    }
    
    public HttpRequestBuilder timeout(Duration timeout) {
        this.timeout = timeout;
        return self();
    }
    
    public HttpRequestBuilder timeout(long seconds) {
        return timeout(Duration.ofSeconds(seconds));
    }
    
    public HttpRequestBuilder maxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return self();
    }
    
    public HttpRequestBuilder followRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
        return self();
    }
    
    @Override
    protected void validate() {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalStateException("URL is required");
        }
        
        if (method == null) {
            throw new IllegalStateException("HTTP method is required");
        }
        
        if ((method == HttpMethod.POST || method == HttpMethod.PUT) && body == null) {
            // This is just a warning, not an error
            System.out.println("Warning: POST/PUT request without body");
        }
    }
    
    @Override
    public HttpRequest build() {
        validate();
        return new HttpRequest(url, method, headers, queryParams, body, timeout, maxRetries, followRedirects);
    }
}

// SQL Query Builder with Fluent API
public class SqlQueryBuilder extends FluentBuilder<String, SqlQueryBuilder> {
private QueryType queryType;
private String tableName;
private final List<String> selectColumns = new ArrayList<>();
private final List<String> whereConditions = new ArrayList<>();
private final List<String> joinClauses = new ArrayList<>();
private final List<String> orderByColumns = new ArrayList<>();
private final List<String> groupByColumns = new ArrayList<>();
private String havingCondition;
private Integer limitValue;
private Integer offsetValue;
private final Map<String, Object> insertValues = new HashMap<>();
private final Map<String, Object> updateValues = new HashMap<>();

    private enum QueryType {
        SELECT, INSERT, UPDATE, DELETE
    }
    
    private SqlQueryBuilder() {}
    
    public static SqlQueryBuilder create() {
        return new SqlQueryBuilder();
    }
    
    @Override
    protected SqlQueryBuilder self() {
        return this;
    }
    
    // SELECT operations
    public SqlQueryBuilder select(String... columns) {
        this.queryType = QueryType.SELECT;
        selectColumns.addAll(Arrays.asList(columns));
        return self();
    }
    
    public SqlQueryBuilder selectAll() {
        return select("*");
    }
    
    public SqlQueryBuilder selectCount() {
        return select("COUNT(*)");
    }
    
    public SqlQueryBuilder selectDistinct(String... columns) {
        this.queryType = QueryType.SELECT;
        for (String column : columns) {
            selectColumns.add("DISTINCT " + column);
        }
        return self();
    }
    
    // FROM clause
    public SqlQueryBuilder from(String tableName) {
        this.tableName = tableName;
        return self();
    }
    
    // WHERE clauses
    public SqlQueryBuilder where(String condition) {
        whereConditions.add(condition);
        return self();
    }
    
    public SqlQueryBuilder whereEquals(String column, Object value) {
        return where(column + " = " + formatValue(value));
    }
    
    public SqlQueryBuilder whereNotEquals(String column, Object value) {
        return where(column + " != " + formatValue(value));
    }
    
    public SqlQueryBuilder whereLike(String column, String pattern) {
        return where(column + " LIKE " + formatValue(pattern));
    }
    
    public SqlQueryBuilder whereIn(String column, Object... values) {
        String valueList = Arrays.stream(values)
            .map(this::formatValue)
            .collect(Collectors.joining(", "));
        return where(column + " IN (" + valueList + ")");
    }
    
    public SqlQueryBuilder whereBetween(String column, Object start, Object end) {
        return where(column + " BETWEEN " + formatValue(start) + " AND " + formatValue(end));
    }
    
    public SqlQueryBuilder whereIsNull(String column) {
        return where(column + " IS NULL");
    }
    
    public SqlQueryBuilder whereIsNotNull(String column) {
        return where(column + " IS NOT NULL");
    }
    
    public SqlQueryBuilder and(String condition) {
        if (!whereConditions.isEmpty()) {
            whereConditions.add("AND " + condition);
        } else {
            whereConditions.add(condition);
        }
        return self();
    }
    
    public SqlQueryBuilder or(String condition) {
        if (!whereConditions.isEmpty()) {
            whereConditions.add("OR " + condition);
        } else {
            whereConditions.add(condition);
        }
        return self();
    }
    
    // JOIN clauses
    public SqlQueryBuilder innerJoin(String table, String condition) {
        joinClauses.add("INNER JOIN " + table + " ON " + condition);
        return self();
    }
    
    public SqlQueryBuilder leftJoin(String table, String condition) {
        joinClauses.add("LEFT JOIN " + table + " ON " + condition);
        return self();
    }
    
    public SqlQueryBuilder rightJoin(String table, String condition) {
        joinClauses.add("RIGHT JOIN " + table + " ON " + condition);
        return self();
    }
    
    public SqlQueryBuilder fullJoin(String table, String condition) {
        joinClauses.add("FULL OUTER JOIN " + table + " ON " + condition);
        return self();
    }
    
    // ORDER BY clause
    public SqlQueryBuilder orderBy(String... columns) {
        orderByColumns.addAll(Arrays.asList(columns));
        return self();
    }
    
    public SqlQueryBuilder orderByAsc(String column) {
        return orderBy(column + " ASC");
    }
    
    public SqlQueryBuilder orderByDesc(String column) {
        return orderBy(column + " DESC");
    }
    
    // GROUP BY clause
    public SqlQueryBuilder groupBy(String... columns) {
        groupByColumns.addAll(Arrays.asList(columns));
        return self();
    }
    
    // HAVING clause
    public SqlQueryBuilder having(String condition) {
        this.havingCondition = condition;
        return self();
    }
    
    // LIMIT and OFFSET
    public SqlQueryBuilder limit(int limit) {
        this.limitValue = limit;
        return self();
    }
    
    public SqlQueryBuilder offset(int offset) {
        this.offsetValue = offset;
        return self();
    }
    
    public SqlQueryBuilder page(int pageNumber, int pageSize) {
        return limit(pageSize).offset(pageNumber * pageSize);
    }
    
    // INSERT operations
    public SqlQueryBuilder insertInto(String tableName) {
        this.queryType = QueryType.INSERT;
        this.tableName = tableName;
        return self();
    }
    
    public SqlQueryBuilder value(String column, Object value) {
        insertValues.put(column, value);
        return self();
    }
    
    public SqlQueryBuilder values(Map<String, Object> values) {
        insertValues.putAll(values);
        return self();
    }
    
    // UPDATE operations
    public SqlQueryBuilder update(String tableName) {
        this.queryType = QueryType.UPDATE;
        this.tableName = tableName;
        return self();
    }
    
    public SqlQueryBuilder set(String column, Object value) {
        updateValues.put(column, value);
        return self();
    }
    
    public SqlQueryBuilder setValues(Map<String, Object> values) {
        updateValues.putAll(values);
        return self();
    }
    
    // DELETE operations
    public SqlQueryBuilder deleteFrom(String tableName) {
        this.queryType = QueryType.DELETE;
        this.tableName = tableName;
        return self();
    }
    
    @Override
    protected void validate() {
        if (queryType == null) {
            throw new IllegalStateException("Query type must be specified");
        }
        
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalStateException("Table name is required");
        }
        
        switch (queryType) {
            case SELECT:
                if (selectColumns.isEmpty()) {
                    throw new IllegalStateException("SELECT queries must specify columns");
                }
                break;
            case INSERT:
                if (insertValues.isEmpty()) {
                    throw new IllegalStateException("INSERT queries must specify values");
                }
                break;
            case UPDATE:
                if (updateValues.isEmpty()) {
                    throw new IllegalStateException("UPDATE queries must specify values");
                }
                if (whereConditions.isEmpty()) {
                    System.out.println("Warning: UPDATE without WHERE clause will affect all rows");
                }
                break;
            case DELETE:
                if (whereConditions.isEmpty()) {
                    System.out.println("Warning: DELETE without WHERE clause will affect all rows");
                }
                break;
        }
    }
    
    @Override
    public String build() {
        validate();
        
        StringBuilder sql = new StringBuilder();
        
        switch (queryType) {
            case SELECT:
                buildSelectQuery(sql);
                break;
            case INSERT:
                buildInsertQuery(sql);
                break;
            case UPDATE:
                buildUpdateQuery(sql);
                break;
            case DELETE:
                buildDeleteQuery(sql);
                break;
        }
        
        return sql.toString();
    }
    
    private void buildSelectQuery(StringBuilder sql) {
        sql.append("SELECT ");
        sql.append(String.join(", ", selectColumns));
        sql.append(" FROM ").append(tableName);
        
        if (!joinClauses.isEmpty()) {
            sql.append(" ").append(String.join(" ", joinClauses));
        }
        
        if (!whereConditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" ", whereConditions));
        }
        
        if (!groupByColumns.isEmpty()) {
            sql.append(" GROUP BY ").append(String.join(", ", groupByColumns));
        }
        
        if (havingCondition != null) {
            sql.append(" HAVING ").append(havingCondition);
        }
        
        if (!orderByColumns.isEmpty()) {
            sql.append(" ORDER BY ").append(String.join(", ", orderByColumns));
        }
        
        if (limitValue != null) {
            sql.append(" LIMIT ").append(limitValue);
        }
        
        if (offsetValue != null) {
            sql.append(" OFFSET ").append(offsetValue);
        }
    }
    
    private void buildInsertQuery(StringBuilder sql) {
        sql.append("INSERT INTO ").append(tableName);
        sql.append(" (").append(String.join(", ", insertValues.keySet())).append(")");
        sql.append(" VALUES (");
        sql.append(insertValues.values().stream()
            .map(this::formatValue)
            .collect(Collectors.joining(", ")));
        sql.append(")");
    }
    
    private void buildUpdateQuery(StringBuilder sql) {
        sql.append("UPDATE ").append(tableName).append(" SET ");
        sql.append(updateValues.entrySet().stream()
            .map(entry -> entry.getKey() + " = " + formatValue(entry.getValue()))
            .collect(Collectors.joining(", ")));
        
        if (!whereConditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" ", whereConditions));
        }
    }
    
    private void buildDeleteQuery(StringBuilder sql) {
        sql.append("DELETE FROM ").append(tableName);
        
        if (!whereConditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" ", whereConditions));
        }
    }
    
    private String formatValue(Object value) {
        if (value == null) {
            return "NULL";
        } else if (value instanceof String) {
            return "'" + value.toString().replace("'", "''") + "'";
        } else if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        } else {
            return "'" + value.toString().replace("'", "''") + "'";
        }
    }
}

// Configuration Builder with Fluent API
public class ConfigurationBuilder extends FluentBuilder<Configuration, ConfigurationBuilder> {
private final Map<String, Object> properties = new HashMap<>();
private String environment = "development";
private boolean enableLogging = true;
private LogLevel logLevel = LogLevel.INFO;
private final List<String> profiles = new ArrayList<>();

    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }
    
    private ConfigurationBuilder() {}
    
    public static ConfigurationBuilder create() {
        return new ConfigurationBuilder();
    }
    
    @Override
    protected ConfigurationBuilder self() {
        return this;
    }
    
    public ConfigurationBuilder environment(String environment) {
        this.environment = environment;
        return self();
    }
    
    public ConfigurationBuilder development() {
        return environment("development");
    }
    
    public ConfigurationBuilder production() {
        return environment("production");
    }
    
    public ConfigurationBuilder testing() {
        return environment("testing");
    }
    
    public ConfigurationBuilder property(String key, Object value) {
        properties.put(key, value);
        return self();
    }
    
    public ConfigurationBuilder properties(Map<String, Object> properties) {
        this.properties.putAll(properties);
        return self();
    }
    
    public ConfigurationBuilder enableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
        return self();
    }
    
    public ConfigurationBuilder enableLogging() {
        return enableLogging(true);
    }
    
    public ConfigurationBuilder disableLogging() {
        return enableLogging(false);
    }
    
    public ConfigurationBuilder logLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
        return self();
    }
    
    public ConfigurationBuilder trace() {
        return logLevel(LogLevel.TRACE);
    }
    
    public ConfigurationBuilder debug() {
        return logLevel(LogLevel.DEBUG);
    }
    
    public ConfigurationBuilder info() {
        return logLevel(LogLevel.INFO);
    }
    
    public ConfigurationBuilder warn() {
        return logLevel(LogLevel.WARN);
    }
    
    public ConfigurationBuilder error() {
        return logLevel(LogLevel.ERROR);
    }
    
    public ConfigurationBuilder profile(String profile) {
        profiles.add(profile);
        return self();
    }
    
    public ConfigurationBuilder profiles(String... profiles) {
        this.profiles.addAll(Arrays.asList(profiles));
        return self();
    }
    
    // Database configuration
    public ConfigurationBuilder database(String url, String username, String password) {
        return property("database.url", url)
               .property("database.username", username)
               .property("database.password", password);
    }
    
    public ConfigurationBuilder databasePool(int minSize, int maxSize) {
        return property("database.pool.min", minSize)
               .property("database.pool.max", maxSize);
    }
    
    // Cache configuration
    public ConfigurationBuilder cache(String provider, int maxSize, long ttlMinutes) {
        return property("cache.provider", provider)
               .property("cache.maxSize", maxSize)
               .property("cache.ttl", ttlMinutes);
    }
    
    // Security configuration
    public ConfigurationBuilder security(String algorithm, int keySize) {
        return property("security.algorithm", algorithm)
               .property("security.keySize", keySize);
    }
    
    @Override
    protected void validate() {
        if (environment == null || environment.trim().isEmpty()) {
            throw new IllegalStateException("Environment must be specified");
        }
        
        if (logLevel == null) {
            throw new IllegalStateException("Log level must be specified");
        }
    }
    
    @Override
    public Configuration build() {
        validate();
        return new Configuration(environment, properties, enableLogging, logLevel, profiles);
    }
}

// Supporting Classes
public enum HttpMethod {
GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS
}

public class HttpRequest {
private final String url;
private final HttpMethod method;
private final Map<String, String> headers;
private final Map<String, String> queryParams;
private final String body;
private final Duration timeout;
private final int maxRetries;
private final boolean followRedirects;

    public HttpRequest(String url, HttpMethod method, Map<String, String> headers, 
                      Map<String, String> queryParams, String body, Duration timeout, 
                      int maxRetries, boolean followRedirects) {
        this.url = url;
        this.method = method;
        this.headers = new HashMap<>(headers);
        this.queryParams = new HashMap<>(queryParams);
        this.body = body;
        this.timeout = timeout;
        this.maxRetries = maxRetries;
        this.followRedirects = followRedirects;
    }
    
    // Getters
    public String getUrl() { return url; }
    public HttpMethod getMethod() { return method; }
    public Map<String, String> getHeaders() { return headers; }
    public Map<String, String> getQueryParams() { return queryParams; }
    public String getBody() { return body; }
    public Duration getTimeout() { return timeout; }
    public int getMaxRetries() { return maxRetries; }
    public boolean isFollowRedirects() { return followRedirects; }
    
    @Override
    public String toString() {
        return "HttpRequest{" +
                "method=" + method +
                ", url='" + url + '\'' +
                ", headers=" + headers +
                ", queryParams=" + queryParams +
                ", timeout=" + timeout +
                '}';
    }
}

public class Configuration {
private final String environment;
private final Map<String, Object> properties;
private final boolean enableLogging;
private final ConfigurationBuilder.LogLevel logLevel;
private final List<String> profiles;

    public Configuration(String environment, Map<String, Object> properties, 
                        boolean enableLogging, ConfigurationBuilder.LogLevel logLevel, 
                        List<String> profiles) {
        this.environment = environment;
        this.properties = new HashMap<>(properties);
        this.enableLogging = enableLogging;
        this.logLevel = logLevel;
        this.profiles = new ArrayList<>(profiles);
    }
    
    public String getEnvironment() { return environment; }
    public Map<String, Object> getProperties() { return properties; }
    public boolean isEnableLogging() { return enableLogging; }
    public ConfigurationBuilder.LogLevel getLogLevel() { return logLevel; }
    public List<String> getProfiles() { return profiles; }
    
    public <T> T getProperty(String key, Class<T> type) {
        Object value = properties.get(key);
        return type.cast(value);
    }
    
    public String getProperty(String key, String defaultValue) {
        return (String) properties.getOrDefault(key, defaultValue);
    }
    
    @Override
    public String toString() {
        return "Configuration{" +
                "environment='" + environment + '\'' +
                ", enableLogging=" + enableLogging +
                ", logLevel=" + logLevel +
                ", profiles=" + profiles +
                ", properties=" + properties +
                '}';
    }
}

// Utility class for JSON operations (placeholder)
public class JsonUtil {
public static String toJson(Object object) {
// This would use a real JSON library like Jackson or Gson
return object.toString();
}

    public static <T> T fromJson(String json, Class<T> type) {
        // This would use a real JSON library
        throw new UnsupportedOperationException("JSON deserialization not implemented");
    }
}

// ==========================================
// 4. COMPREHENSIVE TESTS FOR EDGE CASES
// ==========================================

// Generic Test Base Class
public abstract class GenericTestBase<T, ID, R extends GenericRepository<T, ID>> {
protected R repository;
protected abstract T createValidEntity();
protected abstract T createInvalidEntity();
protected abstract ID getEntityId(T entity);
protected abstract void modifyEntity(T entity);
protected abstract boolean entitiesEqual(T entity1, T entity2);

    @BeforeEach
    void setUp() {
        // Initialize test data
    }
    
    @Test
    void testSaveValidEntity() {
        T entity = createValidEntity();
        T saved = repository.save(entity);
        
        assertThat(saved).isNotNull();
        assertThat(getEntityId(saved)).isNotNull();
    }
    
    @Test
    void testSaveInvalidEntity() {
        T entity = createInvalidEntity();
        
        assertThatThrownBy(() -> repository.save(entity))
            .isInstanceOf(Exception.class);
    }
    
    @Test
    void testFindByIdExisting() {
        T entity = createValidEntity();
        T saved = repository.save(entity);
        ID id = getEntityId(saved);
        
        Optional<T> found = repository.findById(id);
        
        assertThat(found).isPresent();
        assertThat(entitiesEqual(saved, found.get())).isTrue();
    }
    
    @Test
    void testFindByIdNonExisting() {
        // Assuming ID is Long and using a very large number
        @SuppressWarnings("unchecked")
        ID nonExistingId = (ID) Long.valueOf(999999999L);
        
        Optional<T> found = repository.findById(nonExistingId);
        
        assertThat(found).isEmpty();
    }
    
    @Test
    void testUpdateEntity() {
        T entity = createValidEntity();
        T saved = repository.save(entity);
        
        modifyEntity(saved);
        T updated = repository.save(saved);
        
        assertThat(updated).isNotNull();
        assertThat(getEntityId(updated)).isEqualTo(getEntityId(saved));
    }
    
    @Test
    void testDeleteEntity() {
        T entity = createValidEntity();
        T saved = repository.save(entity);
        ID id = getEntityId(saved);
        
        repository.deleteById(id);
        
        Optional<T> found = repository.findById(id);
        assertThat(found).isEmpty();
    }
    
    @Test
    void testSaveAllEntities() {
        List<T> entities = Arrays.asList(
            createValidEntity(),
            createValidEntity(),
            createValidEntity()
        );
        
        List<T> saved = repository.saveAll(entities);
        
        assertThat(saved).hasSize(3);
        assertThat(saved).allMatch(entity -> getEntityId(entity) != null);
    }
    
    @Test
    void testFindAllWithPagination() {
        // Create multiple entities
        for (int i = 0; i < 25; i++) {
            repository.save(createValidEntity());
        }
        
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));
        Page<T> page = repository.findAll(pageable);
        
        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(25);
        assertThat(page.getTotalPages()).isGreaterThanOrEqualTo(3);
    }
    
    @Test
    void testCountEntities() {
        long initialCount = repository.count();
        
        repository.save(createValidEntity());
        repository.save(createValidEntity());
        
        long finalCount = repository.count();
        
        assertThat(finalCount).isEqualTo(initialCount + 2);
    }
    
    @Test
    void testExistsById() {
        T entity = createValidEntity();
        T saved = repository.save(entity);
        ID id = getEntityId(saved);
        
        assertThat(repository.existsById(id)).isTrue();
        
        repository.deleteById(id);
        
        assertThat(repository.existsById(id)).isFalse();
    }
}

// Event Bus Test Class
@ExtendWith(MockitoExtension.class)
public class TypeSafeEventBusTest {
private TypeSafeEventBus eventBus;

    @Mock
    private EventListener<TestEvent> mockListener;
    
    @Mock
    private AsyncEventListener<TestEvent> mockAsyncListener;
    
    @BeforeEach
    void setUp() {
        eventBus = new TypeSafeEventBus();
    }
    
    @AfterEach
    void tearDown() {
        eventBus.shutdown();
    }
    
    @Test
    void testSubscribeAndPublish() {
        eventBus.subscribe(TestEvent.class, mockListener);
        
        TestEvent event = new TestEvent("test-source");
        eventBus.publish(event);
        
        verify(mockListener).onEvent(event);
    }
    
    @Test
    void testAsyncSubscribeAndPublish() throws InterruptedException {
        when(mockAsyncListener.onEventAsync(any(TestEvent.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        
        eventBus.subscribeAsync(TestEvent.class, mockAsyncListener);
        
        TestEvent event = new TestEvent("test-source");
        eventBus.publish(event);
        
        Thread.sleep(100); // Give async processing time
        
        verify(mockAsyncListener).onEventAsync(event);
    }
    
    @Test
    void testPriorityOrdering() {
        List<String> executionOrder = new ArrayList<>();
        
        EventListener<TestEvent> lowPriorityListener = event -> executionOrder.add("low");
        EventListener<TestEvent> highPriorityListener = event -> executionOrder.add("high");
        EventListener<TestEvent> normalPriorityListener = event -> executionOrder.add("normal");
        
        eventBus.subscribe(TestEvent.class, lowPriorityListener, -10);
        eventBus.subscribe(TestEvent.class, highPriorityListener, 10);
        eventBus.subscribe(TestEvent.class, normalPriorityListener, 0);
        
        eventBus.publish(new TestEvent("test"));
        
        assertThat(executionOrder).containsExactly("high", "normal", "low");
    }
    
    @Test
    void testUnsubscribe() {
        EventHandlerRegistration<TestEvent> registration = eventBus.subscribe(TestEvent.class, mockListener);
        
        TestEvent event = new TestEvent("test-source");
        eventBus.publish(event);
        verify(mockListener).onEvent(event);
        
        eventBus.unsubscribe(registration);
        
        eventBus.publish(new TestEvent("test-source-2"));
        verifyNoMoreInteractions(mockListener);
    }
    
    @Test
    void testErrorHandling() {
        EventListener<TestEvent> faultyListener = new EventListener<TestEvent>() {
            @Override
            public void onEvent(TestEvent event) {
                throw new RuntimeException("Test error");
            }
            
            @Override
            public void onError(TestEvent event, Exception exception) {
                // Custom error handling
                assertThat(exception.getMessage()).isEqualTo("Test error");
            }
        };
        
        eventBus.subscribe(TestEvent.class, faultyListener);
        
        // Should not throw exception
        assertThatCode(() -> eventBus.publish(new TestEvent("test")))
            .doesNotThrowAnyException();
    }
    
    @Test
    void testEventFiltering() {
        EventListener<TestEvent> filteringListener = new EventListener<TestEvent>() {
            @Override
            public void onEvent(TestEvent event) {
                // This should not be called for filtered events
                fail("Should not process filtered events");
            }
            
            @Override
            public boolean canHandle(TestEvent event) {
                return event.getSource().startsWith("allowed");
            }
        };
        
        eventBus.subscribe(TestEvent.class, filteringListener);
        
        // This should be filtered out
        eventBus.publish(new TestEvent("denied-source"));
        
        // This should be processed
        assertThatCode(() -> eventBus.publish(new TestEvent("allowed-source")))
            .doesNotThrowAnyException();
    }
    
    @Test
    void testInheritanceHandling() {
        EventListener<Event> baseListener = mock(EventListener.class);
        EventListener<TestEvent> specificListener = mock(EventListener.class);
        
        eventBus.subscribe(Event.class, baseListener);
        eventBus.subscribe(TestEvent.class, specificListener);
        
        TestEvent event = new TestEvent("test");
        eventBus.publish(event);
        
        verify(baseListener).onEvent(event);
        verify(specificListener).onEvent(event);
    }
    
    private static class TestEvent extends Event {
        public TestEvent(String source) {
            super(source);
        }
    }
}

// Fluent API Test Class
public class FluentAPITest {

    @Test
    void testHttpRequestBuilder() {
        HttpRequest request = HttpRequestBuilder.create()
            .url("https://api.example.com/users")
            .post()
            .json()
            .authorization("token123")
            .queryParam("page", "1")
            .queryParam("size", "10")
            .body("{\"name\":\"John\"}")
            .timeout(30)
            .maxRetries(3)
            .build();
        
        assertThat(request.getUrl()).isEqualTo("https://api.example.com/users");
        assertThat(request.getMethod()).isEqualTo(HttpMethod.POST);
        assertThat(request.getHeaders()).containsEntry("Content-Type", "application/json");
        assertThat(request.getHeaders()).containsEntry("Authorization", "Bearer token123");
        assertThat(request.getQueryParams()).containsEntry("page", "1");
        assertThat(request.getBody()).isEqualTo("{\"name\":\"John\"}");
        assertThat(request.getTimeout()).isEqualTo(Duration.ofSeconds(30));
        assertThat(request.getMaxRetries()).isEqualTo(3);
    }
    
    @Test
    void testHttpRequestBuilderValidation() {
        assertThatThrownBy(() -> 
            HttpRequestBuilder.create()
                .post()
                .build() // Missing URL
        ).isInstanceOf(IllegalStateException.class)
         .hasMessageContaining("URL is required");
    }
    
    @Test
    void testSqlQueryBuilder() {
        String sql = SqlQueryBuilder.create()
            .select("id", "name", "email")
            .from("users")
            .whereEquals("active", true)
            .and("role = 'ADMIN'")
            .orderByDesc("created_at")
            .limit(10)
            .build();
        
        String expected = "SELECT id, name, email FROM users WHERE active = true AND role = 'ADMIN' ORDER BY created_at DESC LIMIT 10";
        assertThat(sql).isEqualTo(expected);
    }
    
    @Test
    void testSqlQueryBuilderInsert() {
        String sql = SqlQueryBuilder.create()
            .insertInto("users")
            .value("name", "John Doe")
            .value("email", "john@example.com")
            .value("active", true)
            .build();
        
        String expected = "INSERT INTO users (name, email, active) VALUES ('John Doe', 'john@example.com', true)";
        assertThat(sql).isEqualTo(expected);
    }
    
    @Test
    void testSqlQueryBuilderUpdate() {
        String sql = SqlQueryBuilder.create()
            .update("users")
            .set("name", "Jane Doe")
            .set("email", "jane@example.com")
            .whereEquals("id", 1)
            .build();
        
        String expected = "UPDATE users SET name = 'Jane Doe', email = 'jane@example.com' WHERE id = 1";
        assertThat(sql).isEqualTo(expected);
    }
    
    @Test
    void testConfigurationBuilder() {
        Configuration config = ConfigurationBuilder.create()
            .production()
            .enableLogging()
            .error()
            .database("jdbc:postgresql://localhost/prod", "user", "pass")
            .cache("redis", 1000, 60)
            .profile("web", "security")
            .property("custom.setting", "value")
            .build();
        
        assertThat(config.getEnvironment()).isEqualTo("production");
        assertThat(config.isEnableLogging()).isTrue();
        assertThat(config.getLogLevel()).isEqualTo(ConfigurationBuilder.LogLevel.ERROR);
        assertThat(config.getProperty("database.url", String.class)).isEqualTo("jdbc:postgresql://localhost/prod");
        assertThat(config.getProfiles()).containsExactly("web", "security");
    }
    
    @Test
    void testConditionalBuilding() {
        boolean enableSecurity = true;
        boolean enableCache = false;
        
        Configuration config = ConfigurationBuilder.create()
            .development()
            .when(enableSecurity, builder -> builder.security("AES", 256))
            .when(enableCache, builder -> builder.cache("memory", 100, 30))
            .build();
        
        assertThat(config.getProperties()).containsKey("security.algorithm");
        assertThat(config.getProperties()).doesNotContainKey("cache.provider");
    }
    
    @Test
    void testFluentTransformation() {
        HttpRequest request = HttpRequestBuilder.create()
            .url("https://api.example.com")
            .apply(builder -> {
                // Complex transformation logic
                if (System.getenv("API_KEY") != null) {
                    return builder.authorization(System.getenv("API_KEY"));
                }
                return builder.basicAuth("user", "pass");
            })
            .build();
        
        assertThat(request).isNotNull();
    }
}

// Wildcard and Variance Test Class
public class WildcardVarianceTest {

    @Test
    void testProducerExtends() {
        List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5);
        List<Double> doubles = Arrays.asList(1.1, 2.2, 3.3, 4.4, 5.5);
        
        double sumInts = sumNumbers(integers);
        double sumDoubles = sumNumbers(doubles);
        
        assertThat(sumInts).isEqualTo(15.0);
        assertThat(sumDoubles).isEqualTo(16.5);
    }
    
    private double sumNumbers(List<? extends Number> numbers) {
        return numbers.stream()
            .mapToDouble(Number::doubleValue)
            .sum();
    }
    
    @Test
    void testConsumerSuper() {
        List<Number> numbers = new ArrayList<>();
        List<Object> objects = new ArrayList<>();
        
        addIntegers(numbers);
        addIntegers(objects);
        
        assertThat(numbers).hasSize(3);
        assertThat(objects).hasSize(3);
    }
    
    private void addIntegers(List<? super Integer> list) {
        list.add(1);
        list.add(2);
        list.add(3);
    }
    
    @Test
    void testWildcardCapture() {
        List<String> strings = Arrays.asList("a", "b", "c");
        List<Integer> integers = Arrays.asList(1, 2, 3);
        
        swap(strings, 0, 2);
        swap(integers, 0, 2);
        
        assertThat(strings).containsExactly("c", "b", "a");
        assertThat(integers).containsExactly(3, 2, 1);
    }
    
    private void swap(List<?> list, int i, int j) {
        swapHelper(list, i, j);
    }
    
    private <T> void swapHelper(List<T> list, int i, int j) {
        T temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }
    
    @Test
    void testComplexWildcardScenario() {
        // Testing complex producer-consumer scenario
        List<Apple> apples = Arrays.asList(new Apple(), new Apple());
        List<Orange> oranges = Arrays.asList(new Orange(), new Orange());
        
        List<Fruit> fruits = new ArrayList<>();
        
        // Apples can be added to fruit basket (producer extends)
        addFruits(fruits, apples);
        addFruits(fruits, oranges);
        
        assertThat(fruits).hasSize(4);
    }
    
    private void addFruits(List<? super Fruit> basket, List<? extends Fruit> fruits) {
        basket.addAll(fruits);
    }
    
    // Helper classes for testing
    private static class Fruit {}
    private static class Apple extends Fruit {}
    private static class Orange extends Fruit {}
}

// Type Erasure and Bridge Methods Test
public class TypeErasureTest {

    @Test
    void testTypeErasureReflection() {
        GenericClass<String> stringInstance = new GenericClass<>();
        GenericClass<Integer> integerInstance = new GenericClass<>();
        
        // At runtime, both instances have the same class
        assertThat(stringInstance.getClass()).isEqualTo(integerInstance.getClass());
        
        // Type parameters are erased
        TypeVariable<?>[] typeParameters = stringInstance.getClass().getTypeParameters();
        assertThat(typeParameters).hasSize(1);
        assertThat(typeParameters[0].getName()).isEqualTo("T");
    }
    
    @Test
    void testBridgeMethodGeneration() throws NoSuchMethodException {
        ConcreteProcessor processor = new ConcreteProcessor();
        
        // Both the bridge method and the actual method should exist
        Method[] methods = ConcreteProcessor.class.getDeclaredMethods();
        
        long processMethodCount = Arrays.stream(methods)
            .filter(method -> method.getName().equals("process"))
            .count();
        
        // Should have both bridge method (Object parameter) and actual method (String parameter)
        assertThat(processMethodCount).isGreaterThanOrEqualTo(1);
    }
    
    @Test
    void testGenericArrayLimitations() {
        // Cannot create arrays of parameterized types
        // List<String>[] arrays = new List<String>[10]; // Compile error
        
        // But can create arrays of raw types (with warnings)
        @SuppressWarnings("unchecked")
        List<String>[] arrays = new List[10];
        
        arrays[0] = Arrays.asList("test");
        assertThat(arrays[0]).containsExactly("test");
    }
    
    @Test
    void testInstanceofLimitations() {
        List<String> stringList = Arrays.asList("a", "b", "c");
        Object obj = stringList;
        
        // Cannot use instanceof with parameterized types
        // if (obj instanceof List<String>) { } // Compile error
        
        // But can use instanceof with wildcards
        assertThat(obj instanceof List<?>).isTrue();
        
        // At runtime, we can only check the raw type
        assertThat(obj instanceof List).isTrue();
    }
    
    // Helper classes for testing
    private static class GenericClass<T> {
        private T value;
        
        public void setValue(T value) {
            this.value = value;
        }
        
        public T getValue() {
            return value;
        }
    }
    
    private interface Processor<T> {
        void process(T item);
    }
    
    private static class ConcreteProcessor implements Processor<String> {
        @Override
        public void process(String item) {
            System.out.println("Processing: " + item);
        }
    }
}

// ==========================================
// 5. FRAMEWORK USAGE ANALYSIS
// ==========================================

// Spring Framework Generic Usage Examples
public class SpringFrameworkGenericsAnalysis {

    // 1. Spring Data JPA Repository Pattern
    /*
     * Spring's repository pattern heavily uses generics:
     * 
     * interface JpaRepository<T, ID> extends PagingAndSortingRepository<T, ID>, QueryByExampleExecutor<T>
     * 
     * This allows type-safe repository operations without casting.
     */
    
    @Repository
    public interface ProductRepository extends JpaRepository<Product, Long> {
        // Spring generates implementation automatically
        List<Product> findByNameContaining(String name);
        
        @Query("SELECT p FROM Product p WHERE p.price > :price")
        List<Product> findExpensiveProducts(@Param("price") BigDecimal price);
    }
    
    // 2. Spring's RestTemplate and WebClient Generics
    /*
     * Spring uses generics extensively for HTTP client operations:
     * 
     * <T> ResponseEntity<T> exchange(String url, HttpMethod method, 
     *                                HttpEntity<?> requestEntity, Class<T> responseType)
     */
    
    @Service
    public class ApiClientService {
        private final RestTemplate restTemplate;
        
        public ApiClientService(RestTemplate restTemplate) {
            this.restTemplate = restTemplate;
        }
        
        public <T> T get(String url, Class<T> responseType) {
            ResponseEntity<T> response = restTemplate.getForEntity(url, responseType);
            return response.getBody();
        }
        
        public <T> T post(String url, Object request, Class<T> responseType) {
            return restTemplate.postForObject(url, request, responseType);
        }
        
        // Modern WebClient approach
        public <T> Mono<T> getAsync(String url, Class<T> responseType) {
            return WebClient.create()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(responseType);
        }
    }
    
    // 3. Spring's Event System (similar to our implementation)
    /*
     * Spring's ApplicationEventPublisher and @EventListener use generics:
     */
    
    @Component
    public class SpringEventExample {
        
        @EventListener
        public void handleUserCreated(UserCreatedEvent event) {
            // Type-safe event handling
            User user = event.getUser();
            // Process user creation
        }
        
        @EventListener
        @Async
        public void handleUserCreatedAsync(UserCreatedEvent event) {
            // Async processing
        }
        
        // Conditional event listening
        @EventListener(condition = "#event.user.role == 'ADMIN'")
        public void handleAdminUserCreated(UserCreatedEvent event) {
            // Only process admin users
        }
    }
    
    // 4. Spring Security Generic Configurations
    /*
     * Spring Security uses generics for type-safe configuration:
     */
    
    @Configuration
    @EnableWebSecurity
    public class SecurityConfig {
        
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return http
                .authorizeHttpRequests(authz -> authz
                    .requestMatchers("/api/public/**").permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> jwt.decoder(jwtDecoder()))
                )
                .build();
        }
        
        @Bean
        public JwtDecoder jwtDecoder() {
            return NimbusJwtDecoder.withJwkSetUri("https://example.com/.well-known/jwks.json")
                .build();
        }
    }
}

// Hibernate/JPA Generic Usage Examples
public class HibernateJPAGenericsAnalysis {

    // 1. Generic Entity Mapping
    @MappedSuperclass
    public abstract class BaseEntity<T> {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private T id;
        
        @Column(name = "created_at")
        private LocalDateTime createdAt;
        
        @Column(name = "updated_at")
        private LocalDateTime updatedAt;
        
        // Generic getters and setters
        public T getId() { return id; }
        public void setId(T id) { this.id = id; }
        
        // Other methods...
    }
    
    @Entity
    public class Product extends BaseEntity<Long> {
        @Column(nullable = false)
        private String name;
        
        @Column(precision = 10, scale = 2)
        private BigDecimal price;
        
        // Product-specific fields and methods
    }
    
    // 2. Generic Criteria API Usage
    @Repository
    public class GenericCriteriaRepository<T, ID> {
        
        @PersistenceContext
        private EntityManager entityManager;
        
        private final Class<T> entityClass;
        
        public GenericCriteriaRepository(Class<T> entityClass) {
            this.entityClass = entityClass;
        }
        
        public List<T> findByField(String fieldName, Object value) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<T> query = cb.createQuery(entityClass);
            Root<T> root = query.from(entityClass);
            
            query.where(cb.equal(root.get(fieldName), value));
            
            return entityManager.createQuery(query).getResultList();
        }
        
        public <R> List<R> findProjected(Class<R> projectionClass, String... fields) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<R> query = cb.createQuery(projectionClass);
            Root<T> root = query.from(entityClass);
            
            // Build constructor expression for projection
            // This would require more complex logic in real implementation
            
            return entityManager.createQuery(query).getResultList();
        }
    }
    
    // 3. Generic DTO Mapping
    public class GenericMapper<E, D> {
        private final Class<E> entityClass;
        private final Class<D> dtoClass;
        private final Function<E, D> toDto;
        private final Function<D, E> toEntity;
        
        public GenericMapper(Class<E> entityClass, Class<D> dtoClass,
                           Function<E, D> toDto, Function<D, E> toEntity) {
            this.entityClass = entityClass;
            this.dtoClass = dtoClass;
            this.toDto = toDto;
            this.toEntity = toEntity;
        }
        
        public D toDto(E entity) {
            return entity != null ? toDto.apply(entity) : null;
        }
        
        public E toEntity(D dto) {
            return dto != null ? toEntity.apply(dto) : null;
        }
        
        public List<D> toDtoList(List<E> entities) {
            return entities.stream()