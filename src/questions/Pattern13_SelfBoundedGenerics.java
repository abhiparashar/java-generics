package questions;

public class Pattern13_SelfBoundedGenerics {
    public static void main(String[] args){
        System.out.println("=== PATTERN 13: Self-Bounded Generics ===");

        // Fluent builder with correct return types
        Person person = new PersonBuilder()
                .name("Alice")
                .age(30)
                .email("alice@example.com")
                .when(true, builder -> builder.age(31))
                .build();

        System.out.println("Built person: " + person);

        // Method chaining maintains correct type
        PersonBuilder builder = new PersonBuilder()
                .name("Bob")
                .age(25);

        // All methods return PersonBuilder, not Builder
        PersonBuilder complete = builder
                .email("bob@example.com")
                .apply(b -> b.age(26)); // Transform builder

        Person bob = complete.build();
        System.out.println("Built Bob: " + bob);

        System.out.println();
    }

    // ✅ Make this interface generic to preserve type safety
    public interface BuilderAction<T extends Builder<T>> {
        T doAction(T builder);
    }

    // ✅ Make return types consistent with generic T
    public abstract static class Builder<T extends Builder<T>> {
        protected abstract T self();

        public T when(boolean condition, BuilderAction<T> action) {
            return condition ? action.doAction(self()) : self();
        }

        public T apply(BuilderAction<T> transformation) {
            return transformation.doAction(self());
        }
    }

    public static class PersonBuilder extends Builder<PersonBuilder> {
        private String name;
        private int age;
        private String email;

        @Override
        protected PersonBuilder self() {
            return this;
        }

        // ✅ Make methods public so they are chainable from main()
        public PersonBuilder name(String name) {
            this.name = name;
            return self();
        }

        public PersonBuilder age(Integer age) {
            this.age = age;
            return self();
        }

        public PersonBuilder email(String email) {
            this.email = email;
            return self();
        }

        public Person build() {
            return new Person(name, age, email);
        }
    }

    public static class Person {
        private final String name;
        private final int age;
        private final String email;

        public Person(String name, int age, String email) {
            this.name = name;
            this.age = age;
            this.email = email;
        }

        @Override
        public String toString() {
            return String.format("Person{name='%s', age=%d, email='%s'}", name, age, email);
        }
    }
}