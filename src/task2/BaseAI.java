package task2;

/**
 * Абстрактний «інтелект» виду об'єктів: окремий потік, який періодично оновлює
 * координати всіх об'єктів свого виду в спільній колекції World.
 * Пауза реалізована керуючим прапорцем і методами монітора wait()/notifyAll()
 * (застарілі suspend()/resume() не використовуються).
 * Лабораторна робота №7, завдання 2. Побережнюк Ю.В., група 241б.
 */
public abstract class BaseAI extends Thread {
    static final int DELAY = 20;                    // інтервал між кроками, мс

    final Entity.Kind kind;
    protected final World world;
    private final Runnable onUpdate;                // що зробити після кроку (repaint)

    private volatile boolean running = true;        // прапорець зупинки потоку
    private final Object pauseLock = new Object();
    private boolean paused;                          // захищено pauseLock

    protected BaseAI(String name, Entity.Kind kind, World world, Runnable onUpdate) {
        super(name);
        this.kind = kind;
        this.world = world;
        this.onUpdate = onUpdate;
        setDaemon(true);                             // не заважає завершенню програми
    }

    /** Початкові налаштування нового об'єкта (вибір цільової точки). */
    protected abstract void initEntity(Entity e);

    /** Один крок алгоритму переміщення об'єкта (викликається під world.lock). */
    protected abstract void move(Entity e);

    /** Допоміжний метод: зсув до цілі на speed пікселів; повертає true, якщо ціль досягнута. */
    protected boolean stepTowards(Entity e, double speed) {
        double dx = e.tx - e.x, dy = e.ty - e.y;
        double dist = Math.hypot(dx, dy);
        if (dist <= speed) {
            e.x = e.tx;
            e.y = e.ty;
            return true;
        }
        e.x += dx / dist * speed;
        e.y += dy / dist * speed;
        return false;
    }

    public void pauseAI() {
        synchronized (pauseLock) {
            paused = true;
        }
    }

    public void resumeAI() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();                   // будимо потік, що чекає в waitIfPaused()
        }
    }

    public boolean isPausedAI() {
        synchronized (pauseLock) {
            return paused;
        }
    }

    public void shutdown() {
        running = false;
        resumeAI();
        interrupt();
    }

    private void waitIfPaused() throws InterruptedException {
        synchronized (pauseLock) {
            while (paused) {
                pauseLock.wait();                    // потік переходить у стан WAITING
            }
        }
    }

    @Override
    public final void run() {
        try {
            while (running) {
                waitIfPaused();
                synchronized (world.lock) {          // оновлення координат — під монітором
                    for (Entity e : world.entities) {
                        if (e.kind == kind && e.moving) move(e);
                    }
                }
                onUpdate.run();                      // panel.repaint() — малює EDT
                Thread.sleep(DELAY);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();      // коректне завершення потоку
        }
    }
}
