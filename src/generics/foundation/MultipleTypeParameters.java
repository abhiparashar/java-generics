package generics.foundation;

class Pair<K,V>{
    private K key;
    private V value;
    public Pair(K key, V value){
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
}
public class MultipleTypeParameters {
    public static void main(String[] args) {
        Pair<String, Integer> score = new Pair<>("Alice", 95);
//        Pair<Employee, Department> assignment = new Pair<>(emp, dept);
    }
}
