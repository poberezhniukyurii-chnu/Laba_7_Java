package task1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * ЛР7, завдання 1 (варіант 8): два окремі потоки керують рухом об'єктів —
 * автомобілі рухаються вздовж осі X, мотоцикли — вздовж осі Y, з розворотом біля меж.
 * Побережнюк Ю.В., група 241б.
 */
public class TrafficFrame extends JFrame {
    final TrafficPanel panel = new TrafficPanel();
    final CarThread carThread = new CarThread(panel);
    final MotoMover motoMover = new MotoMover(panel);
    final Thread motoThread = new Thread(motoMover, "Мотоцикли");
    final JLabel status = new JLabel(" ");
    private final Timer statusTimer;

    public TrafficFrame() {
        super("Рух автомобілів (вісь X) і мотоциклів (вісь Y)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        synchronized (panel.lock) {
            for (int i = 0; i < 3; i++) {
                panel.vehicles.add(new Vehicle(Vehicle.Kind.CAR, "А" + (i + 1),
                        40 + 170 * i, 60 + 120 * i));
                panel.vehicles.add(new Vehicle(Vehicle.Kind.MOTO, "М" + (i + 1),
                        150 + 200 * i, 30 + 110 * i));
            }
        }
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(panel, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);

        // javax.swing.Timer спрацьовує в EDT — оновлює підпис зі станами потоків
        statusTimer = new Timer(200, e -> status.setText(String.format(
                "Потік «%s»: %s, пріоритет %d   |   Потік «%s»: %s, пріоритет %d",
                carThread.getName(), carThread.getState(), carThread.getPriority(),
                motoThread.getName(), motoThread.getState(), motoThread.getPriority())));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                stopAll();
            }
        });
    }

    void startAll() {
        carThread.start();
        motoThread.start();
        statusTimer.start();
    }

    void stopAll() {
        statusTimer.stop();
        carThread.shutdown();
        motoMover.stop();
        motoThread.interrupt();
    }

    public static void main(String[] args) {
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        SwingUtilities.invokeLater(() -> {
            TrafficFrame f = new TrafficFrame();
            f.setVisible(true);
            f.startAll();
        });
    }
}
