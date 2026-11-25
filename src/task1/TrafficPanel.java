package task1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

/** Панель-сцена: зберігає спільну колекцію об'єктів і малює її (лише в потоці EDT). */
public class TrafficPanel extends JPanel {
    final Object lock = new Object();                  // монітор для доступу до колекції
    final List<Vehicle> vehicles = new ArrayList<>();
    volatile int areaWidth = 720, areaHeight = 420;    // розміри сцени для фонових потоків

    TrafficPanel() {
        setPreferredSize(new Dimension(areaWidth, areaHeight));
        setBackground(new Color(236, 240, 232));
        addComponentListener(new ComponentAdapter() {  // розміри оновлюються в EDT
            @Override
            public void componentResized(ComponentEvent e) {
                areaWidth = getWidth();
                areaHeight = getHeight();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);                        // очищення попереднього кадру
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        synchronized (lock) {                           // узгоджені координати всіх об'єктів
            for (Vehicle v : vehicles) {
                if (v.kind == Vehicle.Kind.CAR) {
                    g2.setColor(new Color(40, 90, 200));
                    g2.fillRoundRect(v.x, v.y, v.width(), v.height(), 10, 10);
                    g2.setColor(Color.WHITE);
                    g2.drawString(v.name + (v.dir > 0 ? " →" : " ←"), v.x + 5, v.y + 16);
                } else {
                    g2.setColor(new Color(200, 50, 40));
                    g2.fillOval(v.x, v.y, v.width(), v.height());
                    g2.setColor(Color.WHITE);
                    g2.drawString(v.name, v.x + 3, v.y + 15);
                    g2.drawString(v.dir > 0 ? "↓" : "↑", v.x + 7, v.y + 29);
                }
            }
        }
    }
}
