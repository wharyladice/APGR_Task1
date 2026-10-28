package view;

import model.raster.Raster;
import model.raster.RasterBI;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public final class Panel extends JPanel {

    private final RasterBI raster;
    private String modeText = "LINE";
    private String statusText = "Drag with the left mouse button to draw a line.";

    Panel(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        setFocusable(true);
        raster = new RasterBI(width, height);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        graphics.drawImage(raster.getImage(), 0, 0, null);
        drawOverlay((Graphics2D) graphics);
    }

    private void drawOverlay(Graphics2D graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        String controls = "Shortcuts: L line | P polygon | K clip polygon | F seed BG | "
                + "B seed boundary | S scan fill | X clip | E edit lines | V edit points | "
                + "O reverse clip | T pattern | C clear | Shift constrain | Right-click vertex delete";

        FontMetrics metrics = g.getFontMetrics();
        int padding = 8;
        int lineHeight = metrics.getHeight();
        int maxTextWidth = Math.max(1, getWidth() - 2 * padding);

        List<String> lines = new ArrayList<>();
        lines.add("Mode: " + modeText);
        lines.addAll(wrapText(statusText, metrics, maxTextWidth));
        lines.addAll(wrapText(controls, metrics, maxTextWidth));

        int boxHeight = lines.size() * lineHeight + padding * 2;
        int y = Math.max(0, getHeight() - boxHeight);

        g.setColor(new Color(0, 0, 0, 175));
        g.fillRect(0, y, getWidth(), boxHeight);

        g.setColor(Color.WHITE);
        int textY = y + padding + metrics.getAscent();
        for (String line : lines) {
            g.drawString(line, padding, textY);
            textY += lineHeight;
        }

        g.dispose();
    }

    private List<String> wrapText(String text, FontMetrics metrics, int maxWidth) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String word : text.split("\\s+")) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (!current.isEmpty() && metrics.stringWidth(candidate) > maxWidth) {
                result.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = new StringBuilder(candidate);
            }
        }

        if (!current.isEmpty()) {
            result.add(current.toString());
        }

        if (result.isEmpty()) {
            result.add("");
        }

        return result;
    }

    public Raster getRaster() {
        return raster;
    }

    public void setModeText(String modeText) {
        this.modeText = modeText;
        repaint();
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
        repaint();
    }
}
