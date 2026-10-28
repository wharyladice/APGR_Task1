package view;

import javax.swing.JFrame;
import java.awt.BorderLayout;

@SuppressWarnings("serial")
public final class Window extends JFrame {

    private final Panel panel;
    private final ControlsPanel controlsPanel;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    public Window() {
        setTitle("APGR1 - Assignment 01 - Raster Graphics");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        controlsPanel = new ControlsPanel();
        panel = new Panel(WIDTH, HEIGHT);

        add(controlsPanel, BorderLayout.NORTH);
        add(panel, BorderLayout.CENTER);

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    public Panel getPanel() {
        return panel;
    }

    public ControlsPanel getControlsPanel() {
        return controlsPanel;
    }
}
