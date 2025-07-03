package questions;

public class UpperBounds {
    public static void main(String[] args){
        System.out.println("upper bound");
        NumberBox<Integer>intBox = new NumberBox<>(42);
        NumberBox<Double>dblBox = new NumberBox<>(0.9);
//        NumberBox<String>strBox = new NumberBox<>("Hello");
    }

   public static class NumberBox<T extends Number>{
        private T number;
        public NumberBox(T number){
            this.number = number;
        }
        public double getDoubleValue(){
            return number.doubleValue();
        }
        public boolean isPositive(){
            return number.doubleValue()>0.8;
        }
    }
}
