package net.minestom.server.coordinate;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

final class AreaImpl {

    record Single(Point point) implements Area.Single {
        public Single {
            if (point == null) throw new IllegalArgumentException("Point cannot be null");
        }

        @Override
        public @NotNull Iterator<Vec> iterator() {
            return new Iterator<>() {
                private boolean hasNext = true;

                @Override
                public boolean hasNext() {
                    return hasNext;
                }

                @Override
                public Vec next() {
                    if (!hasNext) throw new NoSuchElementException();
                    hasNext = false;
                    return new Vec(point.blockX(), point.blockY(), point.blockZ());
                }
            };
        }
    }

    record Line(Point start, Point end) implements Area.Line {
        public Line {
            if (start == null || end == null) {
                throw new IllegalArgumentException("Points cannot be null");
            }
        }

        @Override
        public @NotNull Iterator<Vec> iterator() {
            return new Iterator<>() {
                private final int x1 = start.blockX(), y1 = start.blockY(), z1 = start.blockZ();
                private final int x2 = end.blockX(), y2 = end.blockY(), z2 = end.blockZ();
                private int x = x1, y = y1, z = z1;
                private boolean done = false;

                // Bresenham setup
                private final int dx = Math.abs(x2 - x1);
                private final int dy = Math.abs(y2 - y1);
                private final int dz = Math.abs(z2 - z1);
                private final int sx = Integer.compare(x2, x1);
                private final int sy = Integer.compare(y2, y1);
                private final int sz = Integer.compare(z2, z1);
                private int err1 = (dx >= dy && dx >= dz) ? dx / 2 : 0;
                private int err2 = (dy >= dx && dy >= dz) ? dy / 2 : 0;
                private int err3 = (dz >= dx && dz >= dy) ? dz / 2 : 0;

                @Override
                public boolean hasNext() {
                    return !done;
                }

                @Override
                public Vec next() {
                    if (done) throw new NoSuchElementException();
                    Vec result = new Vec(x, y, z);
                    if (x == x2 && y == y2 && z == z2) {
                        done = true;
                        return result;
                    }
                    if (dx >= dy && dx >= dz) {
                        x += sx;
                        err1 -= dy;
                        err2 -= dz;
                        if (err1 < 0) {
                            y += sy;
                            err1 += dx;
                        }
                        if (err2 < 0) {
                            z += sz;
                            err2 += dx;
                        }
                    } else if (dy >= dx && dy >= dz) {
                        y += sy;
                        err1 -= dx;
                        err2 -= dz;
                        if (err1 < 0) {
                            x += sx;
                            err1 += dy;
                        }
                        if (err2 < 0) {
                            z += sz;
                            err2 += dy;
                        }
                    } else {
                        z += sz;
                        err1 -= dx;
                        err2 -= dy;
                        if (err1 < 0) {
                            x += sx;
                            err1 += dz;
                        }
                        if (err2 < 0) {
                            y += sy;
                            err2 += dz;
                        }
                    }
                    return result;
                }
            };
        }
    }

    record Cuboid(Point min, Point max) implements Area.Cuboid {
        public Cuboid {
            if (min == null || max == null) {
                throw new IllegalArgumentException("Points cannot be null");
            }
            min = new Vec(
                    Math.min(min.blockX(), max.blockX()),
                    Math.min(min.blockY(), max.blockY()),
                    Math.min(min.blockZ(), max.blockZ())
            );
            max = new Vec(
                    Math.max(min.blockX(), max.blockX()),
                    Math.max(min.blockY(), max.blockY()),
                    Math.max(min.blockZ(), max.blockZ())
            );
        }

        @Override
        public @NotNull Iterator<Vec> iterator() {
            final int minX = min.blockX(), minY = min.blockY(), minZ = min.blockZ();
            final int maxX = max.blockX(), maxY = max.blockY(), maxZ = max.blockZ();
            return new Iterator<>() {
                private int x = minX;
                private int y = minY;
                private int z = minZ;

                @Override
                public boolean hasNext() {
                    return x <= maxX && y <= maxY && z <= maxZ;
                }

                @Override
                public Vec next() {
                    Vec vec = new Vec(x, y, z);
                    if (x < maxX) {
                        x++;
                    } else if (y < maxY) {
                        x = minX;
                        y++;
                    } else if (z < maxZ) {
                        x = minX;
                        y = minY;
                        z++;
                    }
                    return vec;
                }
            };
        }
    }

    record Sphere(Point center, int radius) implements Area.Sphere {
        public Sphere {
            if (center == null || radius < 0) {
                throw new IllegalArgumentException("Center cannot be null and radius must be non-negative");
            }
        }

        @Override
        public @NotNull Iterator<Vec> iterator() {
            return new Iterator<>() {
                private int x = center.blockX() - radius;
                private int y = center.blockY() - radius;
                private int z = center.blockZ() - radius;

                @Override
                public boolean hasNext() {
                    return x <= center.blockX() + radius && y <= center.blockY() + radius && z <= center.blockZ() + radius;
                }

                @Override
                public Vec next() {
                    Vec vec = new Vec(x, y, z);
                    if (x < center.blockX() + radius) {
                        x++;
                    } else if (y < center.blockY() + radius) {
                        x = center.blockX() - radius;
                        y++;
                    } else if (z < center.blockZ() + radius) {
                        x = center.blockX() - radius;
                        y = center.blockY() - radius;
                        z++;
                    }
                    return vec;
                }
            };
        }
    }

    record Mesh(List<Point> vertices) implements Area.Mesh {
        public Mesh {
            if (vertices == null || vertices.isEmpty()) {
                throw new IllegalArgumentException("Vertices cannot be null or empty");
            }
            if (vertices.size() < 3) {
                throw new IllegalArgumentException("Mesh must have at least 3 vertices");
            }
        }

        @Override
        public @NotNull Iterator<Vec> iterator() {
            // Calculate bounding box of the mesh
            int minXTemp = Integer.MAX_VALUE, maxXTemp = Integer.MIN_VALUE;
            int minYTemp = Integer.MAX_VALUE, maxYTemp = Integer.MIN_VALUE;
            int minZTemp = Integer.MAX_VALUE, maxZTemp = Integer.MIN_VALUE;

            for (Point vertex : vertices) {
                minXTemp = Math.min(minXTemp, vertex.blockX());
                maxXTemp = Math.max(maxXTemp, vertex.blockX());
                minYTemp = Math.min(minYTemp, vertex.blockY());
                maxYTemp = Math.max(maxYTemp, vertex.blockY());
                minZTemp = Math.min(minZTemp, vertex.blockZ());
                maxZTemp = Math.max(maxZTemp, vertex.blockZ());
            }

            final int minX = minXTemp;
            final int maxX = maxXTemp;
            final int minY = minYTemp;
            final int maxY = maxYTemp;
            final int minZ = minZTemp;
            final int maxZ = maxZTemp;

            return new Iterator<>() {
                private int currentX = minX;
                private int currentY = minY;
                private int currentZ = minZ;
                private boolean hasNextPoint = findNextValidPoint();

                private boolean findNextValidPoint() {
                    while (currentY <= maxY) {
                        while (currentZ <= maxZ) {
                            while (currentX <= maxX) {
                                if (isPointInMesh(currentX, currentY, currentZ)) {
                                    return true;
                                }
                                currentX++;
                            }
                            currentX = minX;
                            currentZ++;
                        }
                        currentX = minX;
                        currentZ = minZ;
                        currentY++;
                    }
                    return false;
                }

                @Override
                public boolean hasNext() {
                    return hasNextPoint;
                }

                @Override
                public Vec next() {
                    if (!hasNextPoint) {
                        throw new NoSuchElementException();
                    }

                    Vec result = new Vec(currentX, currentY, currentZ);
                    currentX++;
                    hasNextPoint = findNextValidPoint();
                    return result;
                }

                // Simple point-in-mesh test using 2D projection to XZ plane
                // For each Y level, check if point is inside the polygon formed by vertices at that level
                private boolean isPointInMesh(int x, int y, int z) {
                    // Find vertices that bracket this Y level
                    List<Point> relevantVertices = new ArrayList<>();

                    // For simplicity, we'll use vertices that are close to this Y level
                    // This is a basic implementation - a more sophisticated approach would
                    // use proper 3D mesh algorithms like ray-triangle intersection
                    for (Point vertex : vertices) {
                        if (Math.abs(vertex.blockY() - y) <= 1) {
                            relevantVertices.add(vertex);
                        }
                    }

                    // If we don't have enough vertices at this level, not inside
                    if (relevantVertices.size() < 3) {
                        return false;
                    }

                    // Use 2D ray casting on the XZ plane
                    boolean inside = false;
                    int j = relevantVertices.size() - 1;

                    for (int i = 0; i < relevantVertices.size(); i++) {
                        Point vi = relevantVertices.get(i);
                        Point vj = relevantVertices.get(j);

                        if (((vi.z() > z) != (vj.z() > z)) &&
                                (x < (vj.x() - vi.x()) * (z - vi.z()) / (vj.z() - vi.z()) + vi.x())) {
                            inside = !inside;
                        }
                        j = i;
                    }

                    return inside;
                }
            };
        }
    }
}
