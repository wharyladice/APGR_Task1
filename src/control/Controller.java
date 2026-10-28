package control;

import model.objects.Line;
import model.objects.Point;
import model.objects.Polygon;
import model.raster.BresenhamLiner;
import model.raster.Liner;
import model.raster.PolygonRasterizer;
import model.raster.RasterBI;
import model.raster.clip.SutherlandHodgman;
import model.raster.fill.CheckerPatternFill;
import model.raster.fill.PatternFill;
import model.raster.fill.ScanLineFill;
import model.raster.fill.SeedFill;
import model.raster.fill.SolidPatternFill;
import view.ControlsPanel;
import view.Panel;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JColorChooser;
import javax.swing.KeyStroke;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class Controller {

    private static final int BACKGROUND_COLOR = 0x161633;
    private static final int DEFAULT_BOUNDARY_COLOR = 0xFFFFFF;
    private static final int DEFAULT_FILL_COLOR = 0xFFD166;
    private static final int DEFAULT_PATTERN_COLOR = 0x2A9D8F;
    private static final int CLIP_POLYGON_COLOR = 0xFF9F1C;
    private static final int PREVIEW_COLOR = 0x66E3FF;
    private static final int CLIPPED_FILL_COLOR = 0x31572C;
    private static final int CLIPPED_BOUNDARY_COLOR = 0x80ED99;
    private static final int PATTERN_CELL_SIZE = 8;
    private static final double PICK_RADIUS = 10.0;

    private final Panel panel;
    private final ControlsPanel controlsPanel;
    private final RasterBI raster;
    private final Liner liner;
    private final PolygonRasterizer polygonRasterizer;
    private final SeedFill seedFill;
    private final SutherlandHodgman clipper;

    private final List<Line> lines = new ArrayList<>();
    private final List<Polygon> polygons = new ArrayList<>();
    private final List<ScanFillOperation> scanFillOperations = new ArrayList<>();
    private final List<SeedFillOperation> seedFillOperations = new ArrayList<>();

    private Mode mode = Mode.LINE;
    private Mode modeBeforeSeed = Mode.LINE;
    private Polygon activePolygon;
    private Polygon clippingPolygon;
    private Polygon clippedPolygon;

    private Point dragStart;
    private Point previewPoint;
    private boolean dragging;

    private int selectedLineIndex = -1;
    private boolean selectedLineStart;
    private VertexSelection selectedVertex;

    private int fillColor = DEFAULT_FILL_COLOR;
    private int patternColor = DEFAULT_PATTERN_COLOR;
    private int boundaryColor = DEFAULT_BOUNDARY_COLOR;
    private boolean patternEnabled;

    public Controller(Panel panel) {
        this(panel, null);
    }

    public Controller(Panel panel, ControlsPanel controlsPanel) {
        this.panel = panel;
        this.controlsPanel = controlsPanel;
        this.raster = (RasterBI) panel.getRaster();
        this.raster.setClearColor(BACKGROUND_COLOR);
        this.liner = new BresenhamLiner(raster);
        this.polygonRasterizer = new PolygonRasterizer(liner);
        this.seedFill = new SeedFill();
        this.clipper = new SutherlandHodgman();

        initMouseControls();
        initKeyboardControls();
        initToolbarControls();
        syncControlStyles();
        clearAll();
        setMode(Mode.LINE, "Drag with the left mouse button to draw a line. Hold Shift to constrain it.");
    }

    private void initMouseControls() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                Point point = clampToCanvas(event.getX(), event.getY());

                if (mode == Mode.EDIT_POINTS && event.getButton() == MouseEvent.BUTTON3) {
                    deleteNearestVertex(point);
                    return;
                }

                if (event.getButton() != MouseEvent.BUTTON1) {
                    return;
                }

                if (mode == Mode.SEED_BACKGROUND) {
                    applySeedFill(point, SeedRule.BACKGROUND);
                    return;
                }

                if (mode == Mode.SEED_BOUNDARY) {
                    applySeedFill(point, SeedRule.BOUNDARY);
                    return;
                }

                if (mode == Mode.EDIT_LINE) {
                    LineEndpointSelection selection = findNearestLineEndpoint(point);
                    if (selection == null) {
                        setStatus("No line endpoint nearby. Click closer to an endpoint.");
                        return;
                    }
                    selectedLineIndex = selection.lineIndex();
                    selectedLineStart = selection.startEndpoint();
                    dragging = true;
                    setStatus("Line endpoint selected. Drag it to a new position.");
                    redraw();
                    return;
                }

                if (mode == Mode.EDIT_POINTS) {
                    selectedVertex = findNearestVertex(point);
                    if (selectedVertex == null) {
                        setStatus("No polygon vertex nearby. Drag a highlighted vertex or right-click one to delete it.");
                        return;
                    }
                    dragging = true;
                    setStatus(selectedVertex.clipping()
                            ? "Clipping polygon vertex selected. Drag to edit it."
                            : "Polygon vertex selected. Drag to edit it.");
                    redraw();
                    return;
                }

                dragStart = point;
                previewPoint = point;
                dragging = true;
                redraw();
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                Point mousePoint = clampToCanvas(event.getX(), event.getY());

                if (mode == Mode.EDIT_LINE && dragging && selectedLineIndex >= 0) {
                    moveSelectedLineEndpoint(mousePoint);
                    return;
                }

                if (mode == Mode.EDIT_POINTS && dragging && selectedVertex != null) {
                    moveSelectedVertex(mousePoint);
                    return;
                }

                if (!dragging || dragStart == null) {
                    return;
                }

                previewPoint = mode == Mode.LINE && event.isShiftDown()
                        ? constrainToNearestDirection(dragStart, mousePoint)
                        : mousePoint;
                redraw();
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                if (event.getButton() != MouseEvent.BUTTON1) {
                    return;
                }

                if (mode == Mode.EDIT_LINE && dragging) {
                    dragging = false;
                    selectedLineIndex = -1;
                    setStatus("Line endpoint updated. Choose another endpoint or switch mode.");
                    redraw();
                    return;
                }

                if (mode == Mode.EDIT_POINTS && dragging) {
                    boolean wasClipping = selectedVertex != null && selectedVertex.clipping();
                    dragging = false;
                    selectedVertex = null;
                    setStatus(wasClipping
                            ? "Clipping polygon vertex updated. The clip result was invalidated."
                            : "Polygon vertex updated. Dependent fills were invalidated.");
                    redraw();
                    return;
                }

                if (!dragging || dragStart == null) {
                    return;
                }

                Point mousePoint = clampToCanvas(event.getX(), event.getY());
                Point releasedPoint = mode == Mode.LINE && event.isShiftDown()
                        ? constrainToNearestDirection(dragStart, mousePoint)
                        : mousePoint;

                switch (mode) {
                    case LINE -> finishLine(releasedPoint);
                    case POLYGON -> finishPolygonPoint(activePolygon, releasedPoint, "Polygon");
                    case CLIP_POLYGON -> finishPolygonPoint(clippingPolygon, releasedPoint, "Clipping polygon");
                    default -> {
                    }
                }

                dragging = false;
                dragStart = null;
                previewPoint = null;
                redraw();
            }
        };

        panel.addMouseListener(mouseAdapter);
        panel.addMouseMotionListener(mouseAdapter);
    }

    private void initKeyboardControls() {
        bindKey("L", "lineMode", this::activateLineMode);
        bindKey("P", "newPolygon", this::startPolygon);
        bindKey("K", "newClipPolygon", this::startClipPolygon);
        bindKey("F", "seedBackground", this::startSeedBackground);
        bindKey("B", "seedBoundary", this::startSeedBoundary);
        bindKey("S", "scanLineFill", this::applyScanLineFill);
        bindKey("X", "clip", this::applyClipping);
        bindKey("E", "editLines", this::activateEditLineMode);
        bindKey("V", "editPoints", this::activateEditPointsMode);
        bindKey("O", "reverseClip", this::reverseClippingPolygonOrientation);
        bindKey("C", "clear", this::clearAll);
        bindKey("T", "togglePattern", this::togglePattern);
        bindKey("ESCAPE", "cancelMode", () -> {
            if (mode == Mode.SEED_BACKGROUND || mode == Mode.SEED_BOUNDARY) {
                restoreModeAfterSeed("Seed fill cancelled. Previous drawing mode restored.");
            } else {
                setMode(Mode.LINE, "Returned to line drawing mode.");
            }
        });
    }

    private void initToolbarControls() {
        if (controlsPanel == null) {
            return;
        }

        controlsPanel.onLine(this::activateLineMode);
        controlsPanel.onPolygon(this::startPolygon);
        controlsPanel.onClipPolygon(this::startClipPolygon);
        controlsPanel.onSeedBackground(this::startSeedBackground);
        controlsPanel.onSeedBoundary(this::startSeedBoundary);
        controlsPanel.onScanFill(this::applyScanLineFill);
        controlsPanel.onClip(this::applyClipping);
        controlsPanel.onEditLines(this::activateEditLineMode);
        controlsPanel.onEditPoints(this::activateEditPointsMode);
        controlsPanel.onReverseClip(this::reverseClippingPolygonOrientation);
        controlsPanel.onClear(this::clearAll);
        controlsPanel.onFillColor(this::chooseFillColor);
        controlsPanel.onPatternColor(this::choosePatternColor);
        controlsPanel.onBoundaryColor(this::chooseBoundaryColor);
        controlsPanel.onPatternChanged(enabled -> {
            patternEnabled = enabled;
            setStatus("Pattern fill " + (enabled ? "enabled" : "disabled")
                    + ". It is used by seed and scan-line fills.");
        });
    }

    private void activateLineMode() {
        setMode(
                Mode.LINE,
                "Drag with the left mouse button to draw a line. Hold Shift to constrain it."
        );
    }

    private void activateEditLineMode() {
        setMode(
                Mode.EDIT_LINE,
                "Drag a highlighted line endpoint to move it. Existing derived fills are cleared when geometry changes."
        );
    }

    private void activateEditPointsMode() {
        setMode(
                Mode.EDIT_POINTS,
                "Drag subject or clipping polygon vertices. Right-click a highlighted vertex to delete it."
        );
    }

    private void reverseClippingPolygonOrientation() {
        cancelPendingSeedMode();
        if (clippingPolygon == null || clippingPolygon.size() < 3) {
            setStatus("Create a clipping polygon with at least 3 vertices before reversing its orientation.");
            return;
        }

        clippingPolygon.reverse();
        clippedPolygon = null;
        redraw();
        setStatus("Clipping polygon orientation reversed to " + clipOrientationName() + ".");
    }

    private String clipOrientationName() {
        if (clippingPolygon == null || clippingPolygon.size() < 3) {
            return "undefined";
        }
        return clippingPolygon.signedArea() > 0 ? "counter-clockwise" : "clockwise";
    }

    private void startPolygon() {
        activePolygon = new Polygon();
        polygons.add(activePolygon);
        clippedPolygon = null;
        setMode(Mode.POLYGON,
                "New polygon started. Drag/click to add vertices; it remains closed after 3 vertices.");
        redraw();
    }

    private void startClipPolygon() {
        clippingPolygon = new Polygon();
        clippedPolygon = null;
        setMode(Mode.CLIP_POLYGON,
                "New clipping polygon started. Add at least 3 vertices and keep it convex.");
        redraw();
    }

    private void startSeedBackground() {
        enterSeedMode(
                Mode.SEED_BACKGROUND,
                "Click once inside a CLOSED area. Outside-background clicks are rejected automatically."
        );
    }

    private void startSeedBoundary() {
        enterSeedMode(
                Mode.SEED_BOUNDARY,
                "Click once inside a CLOSED boundary. Outside-background clicks are rejected automatically."
        );
    }

    private void bindKey(String key, String actionName, Runnable action) {
        InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = panel.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("pressed " + key), actionName);
        actionMap.put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                action.run();
            }
        });
    }

    private void finishLine(Point endPoint) {
        if (!dragStart.equals(endPoint)) {
            lines.add(new Line(dragStart, endPoint));
            setStatus("Line stored: [" + dragStart.x() + ", " + dragStart.y()
                    + "] -> [" + endPoint.x() + ", " + endPoint.y() + "].");
        }
    }

    private void finishPolygonPoint(Polygon polygon, Point point, String name) {
        if (polygon == null) {
            return;
        }

        if (polygon.size() == 0 || !polygon.get(polygon.size() - 1).equals(point)) {
            polygon.add(point);
            clippedPolygon = null;
            setStatus(name + " vertex " + polygon.size() + " added at ["
                    + point.x() + ", " + point.y() + "].");
        }
    }

    private LineEndpointSelection findNearestLineEndpoint(Point point) {
        LineEndpointSelection best = null;
        double bestDistance = PICK_RADIUS;

        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            double startDistance = line.a().distanceTo(point);
            if (startDistance <= bestDistance) {
                bestDistance = startDistance;
                best = new LineEndpointSelection(i, true);
            }

            double endDistance = line.b().distanceTo(point);
            if (endDistance <= bestDistance) {
                bestDistance = endDistance;
                best = new LineEndpointSelection(i, false);
            }
        }
        return best;
    }

    private VertexSelection findNearestVertex(Point point) {
        VertexSelection best = null;
        double bestDistance = PICK_RADIUS;

        for (Polygon polygon : polygons) {
            for (int i = 0; i < polygon.size(); i++) {
                double distance = polygon.get(i).distanceTo(point);
                if (distance <= bestDistance) {
                    bestDistance = distance;
                    best = new VertexSelection(polygon, i, false);
                }
            }
        }

        if (clippingPolygon != null) {
            for (int i = 0; i < clippingPolygon.size(); i++) {
                double distance = clippingPolygon.get(i).distanceTo(point);
                if (distance <= bestDistance) {
                    bestDistance = distance;
                    best = new VertexSelection(clippingPolygon, i, true);
                }
            }
        }
        return best;
    }

    private void moveSelectedLineEndpoint(Point point) {
        if (selectedLineIndex < 0 || selectedLineIndex >= lines.size()) {
            return;
        }

        Line current = lines.get(selectedLineIndex);
        Line updated = selectedLineStart
                ? new Line(point, current.b())
                : new Line(current.a(), point);
        lines.set(selectedLineIndex, updated);
        invalidateDerivedResults();
        redraw();
    }

    private void moveSelectedVertex(Point point) {
        if (selectedVertex == null || selectedVertex.index() >= selectedVertex.polygon().size()) {
            return;
        }

        selectedVertex.polygon().set(selectedVertex.index(), point);
        invalidateDerivedResults();
        redraw();
    }

    private void deleteNearestVertex(Point point) {
        VertexSelection selection = findNearestVertex(point);
        if (selection == null) {
            setStatus("No polygon vertex nearby to delete.");
            return;
        }

        Polygon polygon = selection.polygon();
        polygon.remove(selection.index());

        if (selection.clipping()) {
            if (polygon.isEmpty()) {
                clippingPolygon = null;
            }
        } else if (polygon.isEmpty()) {
            polygons.remove(polygon);
            if (activePolygon == polygon) {
                activePolygon = polygons.isEmpty() ? null : polygons.get(polygons.size() - 1);
            }
        }

        invalidateDerivedResults();
        redraw();
        setStatus(selection.clipping()
                ? "Clipping polygon vertex deleted."
                : "Polygon vertex deleted.");
    }

    private void invalidateDerivedResults() {
        scanFillOperations.clear();
        seedFillOperations.clear();
        clippedPolygon = null;
    }

    private void applySeedFill(Point point, SeedRule rule) {
        List<Point> region = rule == SeedRule.BACKGROUND
                ? seedFill.findClosedBackgroundRegion(
                        raster,
                        point.x(),
                        point.y(),
                        BACKGROUND_COLOR
                )
                : seedFill.findClosedBoundaryRegion(
                        raster,
                        point.x(),
                        point.y(),
                        boundaryColor
                );

        if (region.isEmpty()) {
            restoreModeAfterSeed(
                    "Fill cancelled: click inside a closed area. The outside background was left unchanged."
            );
            return;
        }

        PatternFill patternFill = currentFillPattern();
        seedFillOperations.add(new SeedFillOperation(List.copyOf(region), patternFill));
        String ruleName = rule == SeedRule.BACKGROUND
                ? "Background-restricted"
                : "Boundary-restricted";

        restoreModeAfterSeed(
                ruleName + " seed fill applied with " + fillStyleName() + " at "
                        + point.x() + ", " + point.y() + ". Returned automatically to "
                        + modeBeforeSeed.displayName + " mode."
        );
    }

    private void applyScanLineFill() {
        cancelPendingSeedMode();

        if (activePolygon == null || activePolygon.size() < 3) {
            setStatus("Scan-line fill needs a polygon with at least 3 vertices. Press P first.");
            return;
        }

        scanFillOperations.add(new ScanFillOperation(
                activePolygon.copy(),
                currentFillPattern(),
                boundaryColor
        ));
        redraw();
        setStatus("Scan-line fill applied with " + fillStyleName() + ".");
    }

    private void applyClipping() {
        cancelPendingSeedMode();

        if (activePolygon == null || activePolygon.size() < 3) {
            setStatus("Clipping needs a subject polygon with at least 3 vertices.");
            return;
        }
        if (clippingPolygon == null || clippingPolygon.size() < 3) {
            setStatus("Clipping needs a convex clipping polygon with at least 3 vertices. Press K.");
            return;
        }
        if (!clippingPolygon.isConvex()) {
            clippedPolygon = null;
            redraw();
            setStatus("Clipping polygon is not convex. Press K and draw a convex polygon.");
            return;
        }

        try {
            clippedPolygon = clipper.clip(activePolygon, clippingPolygon);
            redraw();

            if (clippedPolygon.size() < 3) {
                setStatus("The polygons do not overlap, so the clipped result is empty.");
            } else {
                setStatus("Sutherland-Hodgman clipping completed. Green area is the result.");
            }
        } catch (IllegalArgumentException exception) {
            clippedPolygon = null;
            redraw();
            setStatus(exception.getMessage());
        }
    }

    private void clearAll() {
        lines.clear();
        polygons.clear();
        scanFillOperations.clear();
        seedFillOperations.clear();
        activePolygon = null;
        clippingPolygon = null;
        clippedPolygon = null;
        dragStart = null;
        previewPoint = null;
        dragging = false;
        selectedLineIndex = -1;
        selectedVertex = null;
        mode = Mode.LINE;
        modeBeforeSeed = Mode.LINE;
        raster.clear();
        panel.setModeText(Mode.LINE.displayName);
        panel.repaint();
        setStatus("Canvas and all stored data were cleared. Line mode is active.");
    }


    private void redraw() {
        raster.clear();

        for (ScanFillOperation operation : scanFillOperations) {
            new ScanLineFill(operation.polygon())
                    .fill(raster, operation.patternFill(), operation.boundaryColor());
        }

        drawStoredGeometry(false);

        for (SeedFillOperation operation : seedFillOperations) {
            seedFill.applyPattern(raster, operation.pixels(), operation.patternFill());
        }

        if (clippedPolygon != null && clippedPolygon.size() >= 3) {
            new ScanLineFill(clippedPolygon)
                    .fill(raster, CLIPPED_FILL_COLOR, CLIPPED_BOUNDARY_COLOR);
        }

        drawStoredGeometry(true);
        drawEditHandles();
        drawPreview();
        panel.repaint();
    }

    private void drawStoredGeometry(boolean finalPass) {
        liner.setColor(boundaryColor);
        for (Line line : lines) {
            liner.drawLine(line);
        }

        for (Polygon polygon : polygons) {
            if (dragging && mode == Mode.POLYGON && polygon == activePolygon) {
                drawOpenPolygon(polygon, boundaryColor);
            } else {
                polygonRasterizer.rasterize(polygon);
            }
        }

        if (clippingPolygon != null) {
            liner.setColor(CLIP_POLYGON_COLOR);
            if (dragging && mode == Mode.CLIP_POLYGON) {
                drawOpenPolygon(clippingPolygon, CLIP_POLYGON_COLOR);
            } else {
                polygonRasterizer.rasterize(clippingPolygon);
            }
        }

        if (finalPass && clippedPolygon != null && clippedPolygon.size() >= 2) {
            liner.setColor(CLIPPED_BOUNDARY_COLOR);
            polygonRasterizer.rasterize(clippedPolygon);
        }
    }

    private void drawEditHandles() {
        if (mode == Mode.EDIT_LINE) {
            for (Line line : lines) {
                drawHandle(line.a(), PREVIEW_COLOR);
                drawHandle(line.b(), PREVIEW_COLOR);
            }
            return;
        }

        if (mode == Mode.EDIT_POINTS) {
            for (Polygon polygon : polygons) {
                for (Point point : polygon.getPoints()) {
                    drawHandle(point, PREVIEW_COLOR);
                }
            }

            if (clippingPolygon != null) {
                for (Point point : clippingPolygon.getPoints()) {
                    drawHandle(point, CLIP_POLYGON_COLOR);
                }
            }
        }
    }

    private void drawHandle(Point point, int color) {
        liner.setColor(color);
        Point left = clampToCanvas(point.x() - 4, point.y());
        Point right = clampToCanvas(point.x() + 4, point.y());
        Point top = clampToCanvas(point.x(), point.y() - 4);
        Point bottom = clampToCanvas(point.x(), point.y() + 4);
        liner.drawLine(left, right);
        liner.drawLine(top, bottom);
    }

    private void drawOpenPolygon(Polygon polygon, int color) {
        liner.setColor(color);

        if (polygon.size() == 1) {
            liner.drawLine(polygon.get(0), polygon.get(0));
            return;
        }

        for (int i = 0; i + 1 < polygon.size(); i++) {
            liner.drawLine(polygon.get(i), polygon.get(i + 1));
        }
    }

    private void drawPreview() {
        if (!dragging || previewPoint == null) {
            return;
        }

        liner.setColor(PREVIEW_COLOR);

        if (mode == Mode.LINE && dragStart != null) {
            liner.drawLine(dragStart, previewPoint);
            return;
        }

        if (mode == Mode.POLYGON && activePolygon != null) {
            drawPolygonCandidate(activePolygon, previewPoint);
            return;
        }

        if (mode == Mode.CLIP_POLYGON && clippingPolygon != null) {
            drawPolygonCandidate(clippingPolygon, previewPoint);
        }
    }

    private void drawPolygonCandidate(Polygon polygon, Point candidate) {
        if (polygon.size() == 0) {
            liner.drawLine(candidate, candidate);
            return;
        }

        Point last = polygon.get(polygon.size() - 1);
        liner.drawLine(last, candidate);

        if (polygon.size() >= 2) {
            liner.drawLine(candidate, polygon.get(0));
        }
    }


    private Point constrainToNearestDirection(Point start, Point mouse) {
        double dx = mouse.x() - start.x();
        double dy = mouse.y() - start.y();

        Point horizontal = new Point(mouse.x(), start.y());
        Point vertical = new Point(start.x(), mouse.y());

        double positiveT = clamp(
                (dx + dy) / 2.0,
                Math.max(-start.x(), -start.y()),
                Math.min(
                        raster.getWidth() - 1 - start.x(),
                        raster.getHeight() - 1 - start.y()
                )
        );
        Point diagonalPositive = new Point(
                start.x() + (int) Math.round(positiveT),
                start.y() + (int) Math.round(positiveT)
        );

        double negativeT = clamp(
                (dx - dy) / 2.0,
                Math.max(-start.x(), start.y() - (raster.getHeight() - 1)),
                Math.min(raster.getWidth() - 1 - start.x(), start.y())
        );
        Point diagonalNegative = new Point(
                start.x() + (int) Math.round(negativeT),
                start.y() - (int) Math.round(negativeT)
        );

        Point[] candidates = {
                horizontal,
                vertical,
                diagonalPositive,
                diagonalNegative
        };

        Point best = candidates[0];
        double bestDistance = best.distanceTo(mouse);

        for (int i = 1; i < candidates.length; i++) {
            double distance = candidates[i].distanceTo(mouse);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidates[i];
            }
        }

        return best;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private Point clampToCanvas(int x, int y) {
        int clampedX = Math.max(0, Math.min(raster.getWidth() - 1, x));
        int clampedY = Math.max(0, Math.min(raster.getHeight() - 1, y));
        return new Point(clampedX, clampedY);
    }

    private PatternFill currentFillPattern() {
        if (patternEnabled) {
            return new CheckerPatternFill(fillColor, patternColor, PATTERN_CELL_SIZE);
        }
        return new SolidPatternFill(fillColor);
    }

    private String fillStyleName() {
        return patternEnabled ? "checker pattern" : "solid color";
    }

    private void togglePattern() {
        patternEnabled = !patternEnabled;
        if (controlsPanel != null) {
            controlsPanel.setPatternEnabled(patternEnabled);
        }
        setStatus("Pattern fill " + (patternEnabled ? "enabled" : "disabled")
                + ". It is used by seed and scan-line fills.");
    }

    private void chooseFillColor() {
        Integer chosen = chooseColor("Choose primary fill color", fillColor);
        if (chosen == null) {
            return;
        }

        fillColor = chosen;
        syncControlStyles();
        setStatus("Primary fill color changed. New fills will use this color.");
    }

    private void choosePatternColor() {
        Integer chosen = chooseColor("Choose secondary pattern color", patternColor);
        if (chosen == null) {
            return;
        }

        patternColor = chosen;
        syncControlStyles();
        setStatus("Secondary pattern color changed. New pattern fills will use it.");
    }

    private void chooseBoundaryColor() {
        Integer chosen = chooseColor("Choose boundary color", boundaryColor);
        if (chosen == null) {
            return;
        }

        if (chosen == BACKGROUND_COLOR) {
            setStatus("Boundary color cannot be the same as the canvas background.");
            return;
        }

        boundaryColor = chosen;
        syncControlStyles();
        redraw();
        setStatus("Boundary color changed for lines and subject polygons.");
    }

    private Integer chooseColor(String title, int currentColor) {
        Color selected = JColorChooser.showDialog(
                panel,
                title,
                new Color(currentColor & 0xFFFFFF)
        );
        return selected == null ? null : selected.getRGB() & 0xFFFFFF;
    }

    private void syncControlStyles() {
        if (controlsPanel == null) {
            return;
        }

        controlsPanel.setPatternEnabled(patternEnabled);
        controlsPanel.setFillColor(fillColor);
        controlsPanel.setPatternColor(patternColor);
        controlsPanel.setBoundaryColor(boundaryColor);
    }

    private void cancelPendingSeedMode() {
        if (mode != Mode.SEED_BACKGROUND && mode != Mode.SEED_BOUNDARY) {
            return;
        }

        Mode restoredMode = modeBeforeSeed == Mode.SEED_BACKGROUND || modeBeforeSeed == Mode.SEED_BOUNDARY
                ? Mode.LINE
                : modeBeforeSeed;

        mode = restoredMode;
        dragging = false;
        dragStart = null;
        previewPoint = null;
        panel.setModeText(restoredMode.displayName);
    }

    private void enterSeedMode(Mode seedMode, String status) {
        if (mode != Mode.SEED_BACKGROUND && mode != Mode.SEED_BOUNDARY) {
            modeBeforeSeed = mode;
        }

        mode = seedMode;
        dragging = false;
        dragStart = null;
        previewPoint = null;
        panel.setModeText(seedMode.displayName);
        setStatus(status);
        redraw();
    }

    private void restoreModeAfterSeed(String status) {
        Mode restoredMode = modeBeforeSeed == Mode.SEED_BACKGROUND || modeBeforeSeed == Mode.SEED_BOUNDARY
                ? Mode.LINE
                : modeBeforeSeed;

        mode = restoredMode;
        dragging = false;
        dragStart = null;
        previewPoint = null;
        panel.setModeText(restoredMode.displayName);
        redraw();
        setStatus(status);
    }

    private void setMode(Mode newMode, String status) {
        mode = newMode;
        dragging = false;
        dragStart = null;
        previewPoint = null;
        selectedLineIndex = -1;
        selectedVertex = null;
        panel.setModeText(newMode.displayName);
        setStatus(status);
        redraw();
    }

    private void setStatus(String status) {
        panel.setStatusText(status);
    }

    private enum Mode {
        LINE("LINE"),
        POLYGON("POLYGON"),
        CLIP_POLYGON("CLIPPING POLYGON"),
        SEED_BACKGROUND("SEED FILL - BACKGROUND"),
        SEED_BOUNDARY("SEED FILL - BOUNDARY"),
        EDIT_LINE("EDIT LINE ENDPOINTS"),
        EDIT_POINTS("EDIT POLYGON POINTS");

        private final String displayName;

        Mode(String displayName) {
            this.displayName = displayName;
        }
    }

    private enum SeedRule {
        BACKGROUND,
        BOUNDARY
    }

    private record ScanFillOperation(
            Polygon polygon,
            PatternFill patternFill,
            int boundaryColor
    ) {
    }

    private record SeedFillOperation(List<Point> pixels, PatternFill patternFill) {
    }

    private record LineEndpointSelection(int lineIndex, boolean startEndpoint) {
    }

    private record VertexSelection(Polygon polygon, int index, boolean clipping) {
    }
}
