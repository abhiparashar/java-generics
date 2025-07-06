package practise;
/*
Basic Generic Class: Create a generic Box<T> class that can store any type of object.
Include methods to set and get the value.
 */
public class BasicGenericClass {
        public static class Box<T>{
            private T value;

            public T getValue(){
                return value;
            }

            public void setValue(T value){
                this.value = value;
            }

            @Override
            public String toString(){
                return String.format( "Box{" + "value=" + value + '}');
            }
        }
        public static void main(String[] args){
            Box<String>box = new Box<>();
            box.setValue("Hello");

            Box<Integer>box1 = new Box<>();
            box1.setValue(123);

            System.out.println(box.getValue());
            System.out.println(box1.getValue());
        }
}
