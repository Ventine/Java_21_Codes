// Person.java
public record Person(String name, int age) {
    // Constructor compacto para validaciones y normalización.
    public Person {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name no puede ser nulo o vacío");
        }
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("age fuera de rango");
        }
        // normalización opcional: trim
        name = name.trim();
    }

    // Método de utilidad: se puede añadir comportamiento.
    public Person withAge(int newAge) {
        return new Person(this.name, newAge); // devuelve una nueva instancia (inmutable)
    }

    // Método derivado (no cambia estado)
    public boolean isAdult() {
        return this.age >= 18;
    }
}
