package task1;

/** Транспортний засіб на сцені: автомобіль (рух по X) або мотоцикл (рух по Y). */
public class Vehicle {
    public enum Kind { CAR, MOTO }

    final Kind kind;
    final String name;
    int x, y;        // лівий верхній кут
    int dir = 1;     // напрямок руху: +1 або -1

    Vehicle(Kind kind, String name, int x, int y) {
        this.kind = kind;
        this.name = name;
        this.x = x;
        this.y = y;
    }

    int width() {
        return kind == Kind.CAR ? 46 : 22;
    }

    int height() {
        return kind == Kind.CAR ? 24 : 34;
    }
}
