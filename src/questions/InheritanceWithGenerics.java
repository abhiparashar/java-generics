package questions;

abstract class Animal<T>{
    protected String characteristics;
    public abstract void makeSound(T sound);
}

//concrete class
class Dog extends Animal<String>{

    @Override
    public void makeSound(String sound) {
        System.out.println("Dog says: " + sound);
    }
}

//Generic subclass
class Cat<T> extends Animal<T>{

    @Override
    public void makeSound(T sound) {
        System.out.println("Cat says: " + sound);
    }
}

public class InheritanceWithGenerics {
    public static void main(String[] args){
        Dog dog = new Dog();
        dog.makeSound("bark");

        Cat<String>cat = new Cat<>();
        cat.makeSound("meow meow");

        Cat<Integer>cat2 = new Cat<>();
        cat2.makeSound(123);
    }
}
