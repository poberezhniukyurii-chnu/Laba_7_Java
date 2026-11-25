package task2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * ЛР7, завдання 2 (варіант 10): об'єкти «Юридична особа» і «Фізична особа» рухаються
 * у свої чверті області; кожен вид керується окремим потоком-нащадком BaseAI.
 * Панель керування: пауза/продовження потоків, зміна пріоритету, додавання об'єктів.
 * Побережнюк Ю.В., група 241б.
 */
public class SimulationFrame extends JFrame {
    static final String[] PRIORITIES = {"MIN_PRIORITY (1)", "NORM_PRIORITY (5)", "MAX_PRIORITY (10)"};
    static final int[] PRIORITY_VALUES =
            {Thread.MIN_PRIORITY, Thread.NORM_PRIORITY, Thread.MAX_PRIORITY};

    final World world = new World();
    final ScenePanel scene = new ScenePanel();
    final LegalEntityAI legalAI = new LegalEntityAI(world, scene::repaint);
    final NaturalPersonAI naturalAI = new NaturalPersonAI(world, scene::repaint);
    final Controls legalControls = new Controls(legalAI, new Color(40, 90, 200));
    final Controls naturalControls = new Controls(naturalAI, new Color(220, 110, 20));
    private final Timer statusTimer;

    /** Сцена: малювання виконується лише в EDT (paintComponent). */
    class ScenePanel extends JPanel {
        ScenePanel() {
            setPreferredSize(new Dimension(world.width, world.height));
            setBackground(Color.WHITE);
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    world.width = getWidth();
                    world.height = getHeight();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(new Color(225, 235, 255));
            g2.fillRect(0, 0, w / 2, h / 2);
            g2.setColor(new Color(255, 236, 215));
            g2.fillRect(w / 2, h / 2, w - w / 2, h - h / 2);
            g2.setColor(Color.GRAY);
            g2.drawString("зона юридичних осіб", 8, 16);
            g2.drawString("зона фізичних осіб", w - 125, h - 8);
            Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10, new float[]{4, 4}, 0);
            synchronized (world.lock) {
                for (Entity e : world.entities) {
                    boolean legal = e.kind == Entity.Kind.LEGAL;
                    Color c = legal ? new Color(40, 90, 200) : new Color(220, 110, 20);
                    int x = (int) Math.round(e.x), y = (int) Math.round(e.y);
                    if (e.moving) {                  // лінія до цільової точки
                        Stroke old = g2.getStroke();
                        g2.setStroke(dashed);
                        g2.setColor(c.brighter());
                        g2.drawLine(x, y, (int) e.tx, (int) e.ty);
                        g2.setStroke(old);
                    }
                    g2.setColor(c);
                    if (legal) g2.fillRect(x - 9, y - 9, 18, 18);
                    else g2.fillOval(x - 9, y - 9, 18, 18);
                    g2.setColor(Color.WHITE);
                    g2.drawString(legal ? "Ю" : "Ф", x - 4, y + 5);
                }
            }
        }
    }

    /** Рядок керування одним потоком. */
    class Controls extends JPanel {
        final JButton pause = new JButton("Пауза");
        final JButton resume = new JButton("Продовжити");
        final JComboBox<String> priority = new JComboBox<>(PRIORITIES);
        final JButton add = new JButton("Додати 5");
        final JLabel state = new JLabel();
        final BaseAI ai;

        Controls(BaseAI ai, Color color) {
            super(new FlowLayout(FlowLayout.LEFT, 6, 2));
            this.ai = ai;
            JLabel name = new JLabel(ai.getName() + ":");
            name.setForeground(color);
            name.setFont(name.getFont().deriveFont(Font.BOLD));
            name.setPreferredSize(new Dimension(120, 22));
            state.setPreferredSize(new Dimension(260, 22));
            priority.setSelectedIndex(1);
            pause.addActionListener(e -> ai.pauseAI());
            resume.addActionListener(e -> ai.resumeAI());
            priority.addActionListener(
                    e -> ai.setPriority(PRIORITY_VALUES[priority.getSelectedIndex()]));
            add.addActionListener(e -> {
                world.spawn(ai, 5);
                scene.repaint();
            });
            add(name);
            add(pause);
            add(resume);
            add(new JLabel("Пріоритет:"));
            add(priority);
            add(add);
            add(state);
        }

        void refresh() {
            state.setText(String.format("стан: %s, рухаються %d з %d", ai.getState(),
                    world.count(ai.kind, true), world.count(ai.kind, false)));
        }
    }

    public SimulationFrame() {
        super("Юридичні та фізичні особи — багатопотокова симуляція");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel controls = new JPanel(new GridLayout(2, 1));
        controls.setBorder(BorderFactory.createTitledBorder("Керування потоками"));
        controls.add(legalControls);
        controls.add(naturalControls);
        add(scene, BorderLayout.CENTER);
        add(controls, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);

        // після pack() відомий реальний розмір сцени — від нього залежать цільові чверті
        world.width = scene.getWidth();
        world.height = scene.getHeight();
        world.spawn(legalAI, 8);
        world.spawn(naturalAI, 8);

        statusTimer = new Timer(200, e -> {          // оновлення підписів у потоці EDT
            legalControls.refresh();
            naturalControls.refresh();
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                stopAll();
            }
        });
    }

    void startAll() {
        legalAI.start();
        naturalAI.start();
        statusTimer.start();
    }

    void stopAll() {
        statusTimer.stop();
        legalAI.shutdown();
        naturalAI.shutdown();
    }

    public static void main(String[] args) {
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        SwingUtilities.invokeLater(() -> {
            SimulationFrame f = new SimulationFrame();
            f.setVisible(true);
            f.startAll();
        });
    }
}
