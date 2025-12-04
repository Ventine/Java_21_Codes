public class Main {
    public static void main(String[] args) {
        Person p = new Person(" Ana López ", 30);
        System.out.println(p.name());       // "Ana López" (trim aplicado)
        System.out.println(p.age());        // 30
        System.out.println(p.isAdult());    // true
        System.out.println(p);              // Person[name=Ana López, age=30]

        Person younger = p.withAge(25);
        System.out.println(younger);        // Person[name=Ana López, age=25]

        // equals/hashCode automáticos
        Person p2 = new Person("Ana López", 30);
        System.out.println(p.equals(p2));   // true
    }
}
