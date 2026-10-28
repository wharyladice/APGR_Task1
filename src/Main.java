import control.Controller;
import view.Window;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Window window = new Window();
            new Controller(window.getPanel(), window.getControlsPanel());
            window.setVisible(true);
        });
    }
}
