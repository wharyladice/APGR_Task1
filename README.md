# APGR1 Assignment 01

Java/Swing raster graphics project.

## Run

Open the project in IntelliJ IDEA with JDK 21+ and run `src/Main.java`.

## Controls

- `L` Line - draw; hold `Shift` for horizontal, vertical or 45-degree direction.
- `P` Polygon - start a subject polygon.
- `K` Clip Polygon - start a convex clipping polygon.
- `F` / `B` - background/boundary seed fill.
- `S` Scan Fill - fill the current polygon.
- `X` Clip - Sutherland-Hodgman clipping.
- `E` Edit Lines - drag an existing line endpoint.
- `V` Edit Points - drag polygon/clip vertices; right-click a vertex to delete it.
- `O` Reverse Clip - switch clipping-polygon orientation.
- `T` Pattern - checker pattern for seed and scan-line fills.
- `C` Clear - clear canvas and all stored structures.

`Fill A`, `Fill B` and `Boundary` use the standard Swing color chooser.
