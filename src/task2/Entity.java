package task2;

/** Об'єкт симуляції: юридична або фізична особа. Доступ до полів — лише під World.lock. */
public class Entity {
    public enum Kind { LEGAL, NATURAL }

    final Kind kind;
    final int id;
    double x, y;          // поточне положення (центр)
    double tx, ty;        // цільова точка
    boolean moving;       // false — об'єкт стоїть (у своїй чверті або вже досяг цілі)

    Entity(Kind kind, int id, double x, double y) {
        this.kind = kind;
        this.id = id;
        this.x = x;
        this.y = y;
    }
}
