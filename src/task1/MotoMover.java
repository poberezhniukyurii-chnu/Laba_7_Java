package task1;

/** Рух мотоциклів уздовж осі Y — спосіб 2: реалізація інтерфейсу Runnable. */
public class MotoMover implements Runnable {
    static final int V = 4;            // швидкість, пікселів за крок
    static final int DELAY = 20;       // інтервал між кроками N, мс

    private final TrafficPanel panel;
    private volatile boolean running = true;

    MotoMover(TrafficPanel panel) {
        this.panel = panel;
    }

    void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {
            synchronized (panel.lock) {
                for (Vehicle v : panel.vehicles) {
                    if (v.kind != Vehicle.Kind.MOTO) continue;
                    v.y += v.dir * V;
                    if (v.y <= 0 || v.y + v.height() >= panel.areaHeight) {  // розворот біля межі
                        v.dir = -v.dir;
                        v.y = Math.max(0, Math.min(v.y, panel.areaHeight - v.height()));
                    }
                }
            }
            panel.repaint();
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
