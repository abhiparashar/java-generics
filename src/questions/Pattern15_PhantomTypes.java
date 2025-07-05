package questions;

public class Pattern15_PhantomTypes {
    public static class Meter{};
    public static class Foot{};
    public static class Celsius{};
    public static class Fahrenheit{};

    public static class Distance<Unit> {
        private final double value;

        private Distance(double value) {
            this.value = value;
        }

        public static Distance<Meter> meters(double value) {
            return new Distance<>(value);
        }

        public static Distance<Foot> feet(double value) {
            return new Distance<>(value);
        }

        public double getValue() {
            return value;
        }

        // Type-safe conversion
        public Distance<Foot> toFeet() {
            // Only works if this is meters
            return new Distance<>(value * 3.28084);
        }

        public Distance<Meter> toMeters() {
            // Only works if this is feet
            return new Distance<>(value / 3.28084);
        }

        @Override
        public String toString() {
            return String.format("Distance[%.2f]", value);
        }
    }

    public static class Temperature<Unit> {
        private final double value;

        private Temperature(double value) {
            this.value = value;
        }

        public static Temperature<Celsius> celsius(double value) {
            return new Temperature<>(value);
        }

        public static Temperature<Fahrenheit> fahrenheit(double value) {
            return new Temperature<>(value);
        }

        public double getValue() {
            return value;
        }

        public Temperature<Fahrenheit> toFahrenheit() {
            // Only works if this is Celsius
            return new Temperature<>(value * 9.0 / 5.0 + 32);
        }

        public Temperature<Celsius> toCelsius() {
            // Only works if this is Fahrenheit
            return new Temperature<>((value - 32) * 5.0 / 9.0);
        }

        @Override
        public String toString() {
            return String.format("Temperature[%.2f]", value);
        }
    }

    public static void main(String[] args){
        System.out.println("=== PATTERN 15: Phantom Types ===");

        // Type-safe distance operations
        Distance<Meter> meters = Distance.meters(100);
        Distance<Foot> feet = Distance.feet(328);

        System.out.println("Distance in meters: " + meters);
        System.out.println("Distance in feet: " + feet);

        // Convert meters to feet
        Distance<Foot> convertedFeet = meters.toFeet();
        System.out.println("100 meters in feet: " + convertedFeet);

        // Type-safe temperature operations
        Temperature<Celsius> celsius = Temperature.celsius(25);
        Temperature<Fahrenheit> fahrenheit = Temperature.fahrenheit(77);

        System.out.println("Temperature in Celsius: " + celsius);
        System.out.println("Temperature in Fahrenheit: " + fahrenheit);

        // Convert Celsius to Fahrenheit
        Temperature<Fahrenheit> convertedF = celsius.toFahrenheit();
        System.out.println("25°C in Fahrenheit: " + convertedF);

        // Convert Fahrenheit to Celsius
        Temperature<Celsius> convertedC = fahrenheit.toCelsius();
        System.out.println("77°F in Celsius: " + convertedC);

        // The compiler prevents mixing units
        // Distance<Meter> wrong = feet; // Compile error!
        // Temperature<Celsius> wrong2 = fahrenheit; // Compile error!

        System.out.println();
    }
}
