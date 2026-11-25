package task2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Спільний стан симуляції: колекція об'єктів і розміри області. */
public class World {
    final Object lock = new Object();                // монітор для потокобезпечного доступу
    final List<Entity> entities = new ArrayList<>();
    final Random random = new Random(10);
    volatile int width = 720, height = 440;          // оновлюються з EDT, читаються потоками
    private int nextId = 1;

    /** Створює count нових об'єктів виду ai.kind у випадкових точках області. */
    void spawn(BaseAI ai, int count) {
        synchronized (lock) {
            for (int i = 0; i < count; i++) {
                Entity e = new Entity(ai.kind, nextId++, 15 + random.nextInt(width - 30),
                        15 + random.nextInt(height - 30));
                ai.initEntity(e);                    // вибір цілі за правилами свого виду
                entities.add(e);
            }
        }
    }

    int count(Entity.Kind kind, boolean movingOnly) {
        synchronized (lock) {
            int n = 0;
            for (Entity e : entities) {
                if (e.kind == kind && (!movingOnly || e.moving)) n++;
            }
            return n;
        }
    }
}
