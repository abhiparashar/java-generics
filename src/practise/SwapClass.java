package practise;

public class SwapClass {
    public static class ArrayUtil{
        public <T> void swap(T[]arr, int i, int j){
            if(i<0 || i >= arr.length||j<0|| j>=arr.length){
                throw new IndexOutOfBoundsException("Invalid indices");
            }
            T temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
    }
    public static void main(String[] args){
        String[] names = {"Alice", "Bob", "Charlie"};
        int i = 0;
        int j = 1;
        ArrayUtil arrayUtil = new ArrayUtil();
        arrayUtil.swap(names, i, j);
        for(String elem : names){
            System.out.println(elem);
        }
    }
}
