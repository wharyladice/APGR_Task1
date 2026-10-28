package view;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

@SuppressWarnings("serial")
public final class ControlsPanel extends JPanel {

    private final JButton lineButton = new JButton("Line");
    private final JButton polygonButton = new JButton("Polygon");
    private final JButton clipPolygonButton = new JButton("Clip Polygon");
    private final JButton seedBackgroundButton = new JButton("Seed BG");
    private final JButton seedBoundaryButton = new JButton("Seed Boundary");
    private final JButton scanFillButton = new JButton("Scan Fill");
    private final JButton clipButton = new JButton("Clip");
    private final JButton editLinesButton = new JButton("Edit Lines");
    private final JButton editPointsButton = new JButton("Edit Points");
    private final JButton reverseClipButton = new JButton("Reverse Clip");
    private final JButton clearButton = new JButton("Clear");

    private final JCheckBox patternCheckBox = new JCheckBox("Pattern");
    private final JButton fillColorButton = new JButton("Fill A");
    private final JButton patternColorButton = new JButton("Fill B");
    private final JButton boundaryColorButton = new JButton("Boundary");

    public ControlsPanel() {
        setLayout(new GridLayout(3, 1, 0, 0));

        JPanel drawing = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
        drawing.add(lineButton);
        drawing.add(polygonButton);
        drawing.add(clipPolygonButton);
        drawing.add(seedBackgroundButton);
        drawing.add(seedBoundaryButton);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
        actions.add(scanFillButton);
        actions.add(clipButton);
        actions.add(editLinesButton);
        actions.add(editPointsButton);
        actions.add(reverseClipButton);
        actions.add(clearButton);

        JPanel styles = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
        styles.add(patternCheckBox);
        styles.add(fillColorButton);
        styles.add(patternColorButton);
        styles.add(boundaryColorButton);

        lineButton.setToolTipText("Line mode (L)");
        polygonButton.setToolTipText("Start a new subject polygon (P)");
        clipPolygonButton.setToolTipText("Start a convex clipping polygon (K)");
        seedBackgroundButton.setToolTipText("Background-restricted seed fill (F)");
        seedBoundaryButton.setToolTipText("Boundary-restricted seed fill (B)");
        scanFillButton.setToolTipText("Scan-line fill current polygon (S)");
        clipButton.setToolTipText("Sutherland-Hodgman clipping (X)");
        editLinesButton.setToolTipText("Move existing line endpoints (E)");
        editPointsButton.setToolTipText("Move polygon/clip vertices; right-click a vertex to delete it (V)");
        reverseClipButton.setToolTipText("Reverse clipping polygon orientation (O)");
        clearButton.setToolTipText("Clear canvas and data (C)");
        patternCheckBox.setToolTipText("Use repeating checker pattern for seed and scan-line fills (T)");
        fillColorButton.setToolTipText("Choose primary fill color");
        patternColorButton.setToolTipText("Choose secondary pattern color");
        boundaryColorButton.setToolTipText("Choose geometry/boundary color");

        add(drawing);
        add(actions);
        add(styles);
    }

    public void onLine(Runnable action) { addAction(lineButton, action); }
    public void onPolygon(Runnable action) { addAction(polygonButton, action); }
    public void onClipPolygon(Runnable action) { addAction(clipPolygonButton, action); }
    public void onSeedBackground(Runnable action) { addAction(seedBackgroundButton, action); }
    public void onSeedBoundary(Runnable action) { addAction(seedBoundaryButton, action); }
    public void onScanFill(Runnable action) { addAction(scanFillButton, action); }
    public void onClip(Runnable action) { addAction(clipButton, action); }
    public void onEditLines(Runnable action) { addAction(editLinesButton, action); }
    public void onEditPoints(Runnable action) { addAction(editPointsButton, action); }
    public void onReverseClip(Runnable action) { addAction(reverseClipButton, action); }
    public void onClear(Runnable action) { addAction(clearButton, action); }
    public void onFillColor(Runnable action) { addAction(fillColorButton, action); }
    public void onPatternColor(Runnable action) { addAction(patternColorButton, action); }
    public void onBoundaryColor(Runnable action) { addAction(boundaryColorButton, action); }

    public void onPatternChanged(Consumer<Boolean> action) {
        patternCheckBox.addActionListener(event -> action.accept(patternCheckBox.isSelected()));
    }

    public void setPatternEnabled(boolean enabled) { patternCheckBox.setSelected(enabled); }
    public void setFillColor(int color) { updateColorButton(fillColorButton, color); }
    public void setPatternColor(int color) { updateColorButton(patternColorButton, color); }
    public void setBoundaryColor(int color) { updateColorButton(boundaryColorButton, color); }

    private void addAction(JButton button, Runnable action) {
        ActionListener listener = event -> action.run();
        button.addActionListener(listener);
    }

    private void updateColorButton(JButton button, int rgb) {
        Color color = new Color(rgb & 0xFFFFFF);
        button.setBackground(color);
        button.setOpaque(true);

        double luminance = 0.2126 * color.getRed()
                + 0.7152 * color.getGreen()
                + 0.0722 * color.getBlue();
        button.setForeground(luminance < 140 ? Color.WHITE : Color.BLACK);
    }
}
