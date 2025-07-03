package questions;

import java.io.*;
import java.util.*;

// T must implement both Comparable and Serializable
public class SortableBox<T extends Comparable<T> & Serializable> {
    private List<T> items = new ArrayList<>();

    public void add(T item) {
        items.add(item);
    }

    public void sort() {
        Collections.sort(items); // T is Comparable
    }

    public void serialize(String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(items);
            System.out.println("Serialized to " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printItems() {
        for (T item : items) {
            System.out.println(item);
        }
    }

    // Test main method
    public static void main(String[] args) {
        SortableBox<Person> box = new SortableBox<>();
        box.add(new Person("Alice", 30));
        box.add(new Person("Bob", 25));
        box.add(new Person("Charlie", 35));

        System.out.println("Before sorting:");
        box.printItems();

        box.sort();

        System.out.println("\nAfter sorting:");
        box.printItems();

        box.serialize("people.ser");
    }
}

// A class that is Comparable and Serializable
class Person implements Comparable<Person>, Serializable {
    private String name;
    private int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public int compareTo(Person other) {
        return Integer.compare(this.age, other.age); // Sort by age
    }

    @Override
    public String toString() {
        return name + " (" + age + ")";
    }
}
