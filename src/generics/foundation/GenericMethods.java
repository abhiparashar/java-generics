package generics.foundation;

public class GenericMethods {
    public static <T> T identity(T value){
        return  value;
    }

    public static  <T> T firstNonNull(T value1, T value2){
        return value1 != null ? value1 : value2;
    }
    public static void main(String[] args) {
        String string = identity("abc");
        Integer integer = identity(10);

        String result = firstNonNull(null, "fallback");

        System.out.println(string);
        System.out.println(integer);
        System.out.println(result);
    }
}
