
# APGR — Raster Graphics Editor

A 2D raster graphics project developed in **Java** using **Swing and AWT**. The project focuses on implementing fundamental computer graphics algorithms from scratch and combining them into an interactive drawing environment.

The application supports line rasterization, polygon construction, region filling, polygon clipping, and basic geometry editing.

---

## Features

### Line Rasterization
- **Bresenham's Line Algorithm** — integer-based line rasterization supporting all eight octants.
- Interactive line drawing with a real-time preview.
- Horizontal, vertical, and 45° line constraints.
- Alternative line implementations using DDA and Java Graphics2D.

### Polygon Operations
- Interactive creation of polygons using vertices.
- Automatic polygon boundary rendering.
- Moving and deleting individual polygon vertices.
- Editing existing line endpoints.
- Support for separate subject and clipping polygons.

### Filling Algorithms
- **Seed Fill** — iterative, stack-based flood fill with 4-connected neighbors.
  - Background-color-based filling.
  - Boundary-color-based filling.
  - Detection of open regions to prevent unwanted filling.

- **Scan-Line Fill** — polygon filling based on edge intersections.
  - Even–odd filling rule.
  - Custom insertion sort for intersection coordinates.
  - Boundary preservation after filling.

### Polygon Clipping
- **Sutherland–Hodgman Algorithm** for clipping polygons against a convex clipping region.
- Support for clockwise and counterclockwise vertex ordering.
- Interactive clipping polygon construction.
- Visualization of the resulting clipped polygon.

### Fill Patterns
- Solid-color filling.
- Two-color checkerboard pattern.
- Customizable fill and boundary colors.
- Shared pattern interface used by both filling algorithms.

---

## Architecture

The project separates geometry, rasterization algorithms, user interaction, and rendering into dedicated components.

| Component | Responsibility |
|---|---|
| **Model** | Points, lines, polygons, and geometric operations |
| **Raster** | Pixel storage and rasterization algorithms |
| **Fill** | Seed Fill, Scan-Line Fill, and fill patterns |
| **Clipping** | Sutherland–Hodgman polygon clipping |
| **Controller** | Mouse interaction, editing, and application state |
| **View** | Java Swing interface and raster visualization |

This separation allows drawing algorithms to operate independently of the graphical user interface.

---

## Technologies

- **Java**
- **Swing / AWT**
- **BufferedImage**
- **Object-Oriented Programming**
- **Computational Geometry**

---

## Project Summary

The project demonstrates the practical implementation of fundamental computer graphics algorithms without relying on external graphics libraries.

It combines pixel-level rendering with interactive geometry manipulation, providing a foundation for understanding rasterization, region filling, and polygon clipping.
