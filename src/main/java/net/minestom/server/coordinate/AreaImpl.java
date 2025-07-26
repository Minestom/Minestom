package net.minestom.server.coordinate;

import org.jetbrains.annotations.NotNullByDefault;

import java.util.*;

import static net.minestom.server.coordinate.CoordConversion.sectionIndex;

@NotNullByDefault
final class AreaImpl {

    record Single(BlockVec point) implements Area.Single {
        public Single {
            Objects.requireNonNull(point, "Point cannot be null");
        }

        @Override
        public Iterator<BlockVec> iterator() {
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
            Objects.requireNonNull(start, "Start point cannot be null");
            Objects.requireNonNull(end, "End point cannot be null");
        }

        @Override
        public Iterator<BlockVec> iterator() {
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
            // Group actual line blocks by section coordinates
            int sectionSize = BlockVec.SECTION.blockX();
            Map<Long, List<BlockVec>> sectionGroups = new HashMap<>();

            for (BlockVec block : this) {
                int sectionX = Math.floorDiv(block.blockX(), sectionSize);
                int sectionY = Math.floorDiv(block.blockY(), sectionSize);
                int sectionZ = Math.floorDiv(block.blockZ(), sectionSize);
                long sectionKey = sectionIndex(sectionX, sectionY, sectionZ);
                sectionGroups.computeIfAbsent(sectionKey, k -> new ArrayList<>()).add(block);
            }

            List<Area.Cuboid> result = new ArrayList<>();
            for (List<BlockVec> blocks : sectionGroups.values()) {
                int minX = blocks.stream().mapToInt(BlockVec::blockX).min().getAsInt();
                int maxX = blocks.stream().mapToInt(BlockVec::blockX).max().getAsInt();
                int minY = blocks.stream().mapToInt(BlockVec::blockY).min().getAsInt();
                int maxY = blocks.stream().mapToInt(BlockVec::blockY).max().getAsInt();
                int minZ = blocks.stream().mapToInt(BlockVec::blockZ).min().getAsInt();
                int maxZ = blocks.stream().mapToInt(BlockVec::blockZ).max().getAsInt();
                result.add(Area.cuboid(new BlockVec(minX, minY, minZ), new BlockVec(maxX, maxY, maxZ)));
            }
            return result;
        }
    }

    record Cuboid(BlockVec min, BlockVec max) implements Area.Cuboid {
        public Cuboid {
            Objects.requireNonNull(min, "min cannot be null");
            Objects.requireNonNull(max, "max cannot be null");
            final BlockVec origMin = min;
            final BlockVec origMax = max;
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
            min = sortedMin;
            max = sortedMax;
        }

        @Override
        public Iterator<BlockVec> iterator() {
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
            int sectionSize = BlockVec.SECTION.blockX();
            int minSecX = Math.floorDiv(min.blockX(), sectionSize);
            int minSecY = Math.floorDiv(min.blockY(), sectionSize);
            int minSecZ = Math.floorDiv(min.blockZ(), sectionSize);
            int maxSecX = Math.floorDiv(max.blockX(), sectionSize);
            int maxSecY = Math.floorDiv(max.blockY(), sectionSize);
            int maxSecZ = Math.floorDiv(max.blockZ(), sectionSize);

            List<Area.Cuboid> result = new ArrayList<>();

            // Split into cuboids per section
            for (int sx = minSecX; sx <= maxSecX; sx++) {
                for (int sy = minSecY; sy <= maxSecY; sy++) {
                    for (int sz = minSecZ; sz <= maxSecZ; sz++) {
                        int sectionMinX = sx * sectionSize;
                        int sectionMinY = sy * sectionSize;
                        int sectionMinZ = sz * sectionSize;
                        int sectionMaxX = sectionMinX + sectionSize - 1;
                        int sectionMaxY = sectionMinY + sectionSize - 1;
                        int sectionMaxZ = sectionMinZ + sectionSize - 1;

                        // Calculate intersection with this section
                        int intersectMinX = Math.max(min.blockX(), sectionMinX);
                        int intersectMinY = Math.max(min.blockY(), sectionMinY);
                        int intersectMinZ = Math.max(min.blockZ(), sectionMinZ);
                        int intersectMaxX = Math.min(max.blockX(), sectionMaxX);
                        int intersectMaxY = Math.min(max.blockY(), sectionMaxY);
                        int intersectMaxZ = Math.min(max.blockZ(), sectionMaxZ);

                        // Only add if there's a valid intersection
                        if (intersectMinX <= intersectMaxX &&
                                intersectMinY <= intersectMaxY &&
                                intersectMinZ <= intersectMaxZ) {
                            result.add(Area.cuboid(
                                    new BlockVec(intersectMinX, intersectMinY, intersectMinZ),
                                    new BlockVec(intersectMaxX, intersectMaxY, intersectMaxZ)
                            ));
                        }
                    }
                }
            }

            return result;
        }
    }

    record Sphere(BlockVec center, int radius) implements Area.Sphere {
        public Sphere {
            Objects.requireNonNull(center, "Center cannot be null");
            if (radius < 0) throw new IllegalArgumentException("Radius must be non-negative");
        }

        @Override
        public Iterator<BlockVec> iterator() {
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
            // Group actual sphere blocks by section coordinates
            int sectionSize = BlockVec.SECTION.blockX();
            Map<Long, List<BlockVec>> sectionGroups = new HashMap<>();

            for (BlockVec block : this) {
                int sectionX = Math.floorDiv(block.blockX(), sectionSize);
                int sectionY = Math.floorDiv(block.blockY(), sectionSize);
                int sectionZ = Math.floorDiv(block.blockZ(), sectionSize);
                long sectionKey = sectionIndex(sectionX, sectionY, sectionZ);
                sectionGroups.computeIfAbsent(sectionKey, k -> new ArrayList<>()).add(block);
            }

            List<Area.Cuboid> result = new ArrayList<>();
            for (List<BlockVec> blocks : sectionGroups.values()) {
                int minX = blocks.stream().mapToInt(BlockVec::blockX).min().getAsInt();
                int maxX = blocks.stream().mapToInt(BlockVec::blockX).max().getAsInt();
                int minY = blocks.stream().mapToInt(BlockVec::blockY).min().getAsInt();
                int maxY = blocks.stream().mapToInt(BlockVec::blockY).max().getAsInt();
                int minZ = blocks.stream().mapToInt(BlockVec::blockZ).min().getAsInt();
                int maxZ = blocks.stream().mapToInt(BlockVec::blockZ).max().getAsInt();
                result.add(Area.cuboid(new BlockVec(minX, minY, minZ), new BlockVec(maxX, maxY, maxZ)));
            }
            return result;
        }
    }
}