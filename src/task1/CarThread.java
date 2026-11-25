package task1;

/** Потік руху автомобілів уздовж осі X — спосіб 1: успадкування від Thread. */
public class CarThread extends Thread {
    static final int V = 3;            // швидкість, пікселів за крок
    static final int DELAY = 20;       // інтервал між кроками N, мс (~50 кадрів/с)

    private final TrafficPanel panel;
    private volatile boolean running = true;

    CarThread(TrafficPanel panel) {
        super("Автомобілі");
        this.panel = panel;
    }

    void shutdown() {
        running = false;
        interrupt();
    }

    @Override
    public void run() {
        while (running) {
            synchronized (panel.lock) {
                for (Vehicle v : panel.vehicles) {
                    if (v.kind != Vehicle.Kind.CAR) continue;
                    v.x += v.dir * V;
                    if (v.x <= 0 || v.x + v.width() >= panel.areaWidth) {   // розворот біля межі
                        v.dir = -v.dir;
                        v.x = Math.max(0, Math.min(v.x, panel.areaWidth - v.width()));
                    }
                }
            }
            panel.repaint();                 // запит на перемальовування (виконає EDT)
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                break;                       // потік завершується природно
            }
        }
    }
}
