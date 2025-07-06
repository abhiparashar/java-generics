package practise;

import java.util.ArrayList;
import java.util.List;

/*
    Generic Method: Write a generic method that swaps two elements in an array.
 */
public class GenericMethod {
    public static <T> void swapElems(List<T> list, int i, int j){
        T temp = list.get(i);
        list.set(i,list.get(j));
        list.set(j, temp);

        for (int k = 0; k < list.size(); k++) {
            System.out.println(list.get(k));
        }
    }
    public static void main(String[] args){
        List<String>list = new ArrayList<>();
        list.add("abhishek");
        list.add("parashar");
        int i = 0;
        int j = 1;
        swapElems(list,i,j);
    }
}
