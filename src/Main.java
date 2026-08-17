import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Entry point. Wires together the Simulation (model), SimulationPanel
 * (canvas / game loop) and ControlPanel (sidebar), plus a title bar and
 * a bottom status strip that shows the current effect in play.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGui);
    }

    private static void createAndShowGui() {
        Simulation sim = new Simulation();
        SimulationPanel canvas = new SimulationPanel(sim);
        ControlPanel controls = new ControlPanel(sim, canvas);

        JLabel title = new JLabel("Zombie vs Human", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setForeground(Color.WHITE);
        title.setOpaque(true);
        title.setBackground(new Color(20, 30, 24));
        title.setBorder(new EmptyBorder(14, 0, 14, 0));

        JLabel status = new JLabel(sim.getPhaseLabel());
        status.setBorder(new EmptyBorder(8, 16, 8, 16));
        status.setOpaque(true);
        status.setBackground(new Color(235, 233, 226));
        Timer statusTimer = new Timer(150, e -> {
            status.setText(canvas.getStatusMessage());
            title.setText("Zombie vs Human \u2014 " + sim.getPhaseLabel());
        });
        statusTimer.start();

        JFrame frame = new JFrame("Zombie vs Human");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(title, BorderLayout.NORTH);
        frame.add(controls, BorderLayout.WEST);
        frame.add(canvas, BorderLayout.CENTER);
        frame.add(status, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
