package net.minestom.server.coordinate;

import org.jetbrains.annotations.NotNull;

import java.util.*;

final class AreaImpl {
    private static List<Area.Cuboid> splitIterable(Area area) {
        int sectionSize = BlockVec.SECTION.blockX();
        Map<Integer, List<BlockVec>> groups = new HashMap<>();
        for (BlockVec v : area) {
            int sx = Math.floorDiv(v.blockX(), sectionSize);
            groups.computeIfAbsent(sx, k -> new ArrayList<>()).add(v);
        }
        List<Area.Cuboid> result = new ArrayList<>();
        for (List<BlockVec> list : groups.values()) {
            // Compute bounding cuboid for this section slice
            int minX = list.stream().mapToInt(BlockVec::blockX).min().getAsInt();
            int maxX = list.stream().mapToInt(BlockVec::blockX).max().getAsInt();
            int minY = list.stream().mapToInt(BlockVec::blockY).min().getAsInt();
            int maxY = list.stream().mapToInt(BlockVec::blockY).max().getAsInt();
            int minZ = list.stream().mapToInt(BlockVec::blockZ).min().getAsInt();
            int maxZ = list.stream().mapToInt(BlockVec::blockZ).max().getAsInt();
            result.add(Area.cuboid(new BlockVec(minX, minY, minZ), new BlockVec(maxX, maxY, maxZ)));
        }
        return result;
    }

    record Single(BlockVec point) implements Area.Single {
        public Single {
            if (point == null) throw new IllegalArgumentException("Point cannot be null");
        }

        @Override
        public @NotNull Iterator<BlockVec> iterator() {
            return new Iterator<>() {
                private boolean hasNext = true;

                @Override
                public boolean hasNext() {
                    return hasNext;
                }

                @Override
                public BlockVec next() {
                    if (!hasNext) throw new NoSuchElementException();
                    hasNext = false;
                    return point;
                }
            };
        }

        @Override
        public List<Area.Cuboid> split() {
            return List.of(new AreaImpl.Cuboid(point, point));
        }
    }

    record Line(BlockVec start, BlockVec end) implements Area.Line {
        public Line {
            if (start == null || end == null) {
                throw new IllegalArgumentException("Points cannot be null");
            }
        }

        @Override
        public @NotNull Iterator<BlockVec> iterator() {
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

                @Override
                public boolean hasNext() {
                    return !done;
                }

                @Override
                public BlockVec next() {
                    if (done) throw new NoSuchElementException();
                    BlockVec result = new BlockVec(x, y, z);
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

        @Override
        public List<Area.Cuboid> split() {
            return splitIterable(this);
        }
    }

    record Cuboid(BlockVec min, BlockVec max) implements Area.Cuboid {
        public Cuboid {
            if (min == null || max == null) {
                throw new IllegalArgumentException("Points cannot be null");
            }
            // Preserve original parameters for correct comparison
            BlockVec origMin = min;
            BlockVec origMax = max;
            // Compute sorted bounds
            BlockVec sortedMin = new BlockVec(
                    Math.min(origMin.blockX(), origMax.blockX()),
                    Math.min(origMin.blockY(), origMax.blockY()),
                    Math.min(origMin.blockZ(), origMax.blockZ())
            );
            BlockVec sortedMax = new BlockVec(
                    Math.max(origMin.blockX(), origMax.blockX()),
                    Math.max(origMin.blockY(), origMax.blockY()),
                    Math.max(origMin.blockZ(), origMax.blockZ())
            );
            // Assign to record components
            min = sortedMin;
            max = sortedMax;
        }

        @Override
        public @NotNull Iterator<BlockVec> iterator() {
            final int minX = min.blockX(), minY = min.blockY(), minZ = min.blockZ();
            final int maxX = max.blockX(), maxY = max.blockY(), maxZ = max.blockZ();
            return new Iterator<>() {
                private int x = minX;
                private int y = minY;
                private int z = minZ;
                private boolean hasNext = true;

                @Override
                public boolean hasNext() {
                    return hasNext;
                }

                @Override
                public BlockVec next() {
                    if (!hasNext) throw new NoSuchElementException();
                    BlockVec vec = new BlockVec(x, y, z);
                    // Determine next position or finish
                    if (x == maxX && y == maxY && z == maxZ) {
                        hasNext = false;
                    } else if (x < maxX) {
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

        @Override
        public List<Area.Cuboid> split() {
            return splitIterable(this);
        }
    }

    record Sphere(BlockVec center, int radius) implements Area.Sphere {
        public Sphere {
            if (center == null || radius < 0) {
                throw new IllegalArgumentException("Center cannot be null and radius must be non-negative");
            }
        }

        @Override
        public @NotNull Iterator<BlockVec> iterator() {
            final int minX = center.blockX() - radius;
            final int minY = center.blockY() - radius;
            final int minZ = center.blockZ() - radius;
            final int maxX = center.blockX() + radius;
            final int maxY = center.blockY() + radius;
            final int maxZ = center.blockZ() + radius;
            return new Iterator<>() {
                private int x = minX;
                private int y = minY;
                private int z = minZ;
                private boolean hasNext = true;

                @Override
                public boolean hasNext() {
                    return hasNext;
                }

                @Override
                public BlockVec next() {
                    if (!hasNext) throw new NoSuchElementException();
                    BlockVec vec = new BlockVec(x, y, z);
                    if (x == maxX && y == maxY && z == maxZ) {
                        hasNext = false;
                    } else if (x < maxX) {
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

        @Override
        public List<Area.Cuboid> split() {
            return splitIterable(this);
        }
    }
}
