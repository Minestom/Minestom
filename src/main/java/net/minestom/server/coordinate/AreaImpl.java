package net.minestom.server.coordinate;

import java.util.*;

import static net.minestom.server.coordinate.CoordConversion.sectionIndex;

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
            if (start.samePoint(end)) return List.of(start).iterator();
            return new Iterator<>() {
                private final int x1 = start.blockX(), y1 = start.blockY(), z1 = start.blockZ();
                private final int x2 = end.blockX(), y2 = end.blockY(), z2 = end.blockZ();
                private int x = x1, y = y1, z = z1;
                private boolean done = false;

                // 3D Bresenham algorithm
                private final int dx = Math.abs(x2 - x1);
                private final int dy = Math.abs(y2 - y1);
                private final int dz = Math.abs(z2 - z1);
                private final int sx = x1 < x2 ? 1 : -1;
                private final int sy = y1 < y2 ? 1 : -1;
                private final int sz = z1 < z2 ? 1 : -1;
                private int err1, err2;

                {
                    // Initialize error terms based on the dominant axis
                    if (dx >= dy && dx >= dz) {
                        err1 = dx / 2;
                        err2 = dx / 2;
                    } else if (dy >= dx && dy >= dz) {
                        err1 = dy / 2;
                        err2 = dy / 2;
                    } else {
                        err1 = dz / 2;
                        err2 = dz / 2;
                    }
                }

                @Override
                public boolean hasNext() {
                    return !done;
                }

                @Override
                public BlockVec next() {
                    if (done) throw new NoSuchElementException();
                    BlockVec result = new BlockVec(x, y, z);
                    // Check if we've reached the end
                    if (x == x2 && y == y2 && z == z2) {
                        done = true;
                        return result;
                    }
                    // Move to next position using 3D Bresenham
                    if (dx >= dy && dx >= dz) {
                        // X is the dominant axis
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
                        // Y is the dominant axis
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
                        // Z is the dominant axis
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
            // Collect all actual line blocks
            Set<BlockVec> lineBlocks = new HashSet<>();
            for (BlockVec block : this) {
                lineBlocks.add(block);
            }

            // Group blocks by section coordinates
            int sectionSize = BlockVec.SECTION.blockX();
            Map<Long, Set<BlockVec>> sectionGroups = new HashMap<>();

            for (BlockVec block : lineBlocks) {
                int sectionX = Math.floorDiv(block.blockX(), sectionSize);
                int sectionY = Math.floorDiv(block.blockY(), sectionSize);
                int sectionZ = Math.floorDiv(block.blockZ(), sectionSize);
                long sectionKey = sectionIndex(sectionX, sectionY, sectionZ);
                sectionGroups.computeIfAbsent(sectionKey, k -> new HashSet<>()).add(block);
            }

            List<Area.Cuboid> result = new ArrayList<>();
            for (Set<BlockVec> blocks : sectionGroups.values()) {
                for (BlockVec block : blocks) {
                    result.add(Area.cuboid(block, block));
                }
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
            min = origMin.min(origMax);
            max = origMin.max(origMax);
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
            final double radiusSquared = radius * radius;
            return new Iterator<>() {
                private int x = minX;
                private int y = minY;
                private int z = minZ;
                private boolean hasNextValue = true;
                private BlockVec nextVec = findNext();

                private BlockVec findNext() {
                    while (z <= maxZ) {
                        while (y <= maxY) {
                            while (x <= maxX) {
                                // Check if this block is within the sphere
                                double dx = x - center.blockX();
                                double dy = y - center.blockY();
                                double dz = z - center.blockZ();
                                double distanceSquared = dx * dx + dy * dy + dz * dz;

                                if (distanceSquared <= radiusSquared) {
                                    BlockVec result = new BlockVec(x, y, z);
                                    // Advance to next position
                                    x++;
                                    return result;
                                }
                                x++;
                            }
                            x = minX;
                            y++;
                        }
                        y = minY;
                        z++;
                    }
                    hasNextValue = false;
                    return new BlockVec(0, 0, 0); // dummy value, won't be used
                }

                @Override
                public boolean hasNext() {
                    return hasNextValue;
                }

                @Override
                public BlockVec next() {
                    if (!hasNextValue) throw new NoSuchElementException();
                    BlockVec result = nextVec;
                    nextVec = findNext();
                    return result;
                }
            };
        }

        @Override
        public List<Area.Cuboid> split() {
            int sectionSize = BlockVec.SECTION.blockX();

            // Calculate the bounding sections for the sphere
            int minSecX = Math.floorDiv(center.blockX() - radius, sectionSize);
            int maxSecX = Math.floorDiv(center.blockX() + radius, sectionSize);
            int minSecY = Math.floorDiv(center.blockY() - radius, sectionSize);
            int maxSecY = Math.floorDiv(center.blockY() + radius, sectionSize);
            int minSecZ = Math.floorDiv(center.blockZ() - radius, sectionSize);
            int maxSecZ = Math.floorDiv(center.blockZ() + radius, sectionSize);

            List<Area.Cuboid> result = new ArrayList<>();
            double radiusSquared = radius * radius;

            // For each section that might contain sphere blocks
            for (int sx = minSecX; sx <= maxSecX; sx++) {
                for (int sy = minSecY; sy <= maxSecY; sy++) {
                    for (int sz = minSecZ; sz <= maxSecZ; sz++) {
                        int sectionMinX = sx * sectionSize;
                        int sectionMinY = sy * sectionSize;
                        int sectionMinZ = sz * sectionSize;
                        int sectionMaxX = sectionMinX + sectionSize - 1;
                        int sectionMaxY = sectionMinY + sectionSize - 1;
                        int sectionMaxZ = sectionMinZ + sectionSize - 1;

                        // Check if this entire section is within the sphere
                        boolean fullSection = true;
                        for (int x = sectionMinX; x <= sectionMaxX && fullSection; x++) {
                            for (int y = sectionMinY; y <= sectionMaxY && fullSection; y++) {
                                for (int z = sectionMinZ; z <= sectionMaxZ && fullSection; z++) {
                                    double dx = x - center.blockX();
                                    double dy = y - center.blockY();
                                    double dz = z - center.blockZ();
                                    if (dx * dx + dy * dy + dz * dz > radiusSquared) {
                                        fullSection = false;
                                    }
                                }
                            }
                        }

                        if (fullSection) {
                            // Entire section is within sphere
                            result.add(Area.cuboid(
                                    new BlockVec(sectionMinX, sectionMinY, sectionMinZ),
                                    new BlockVec(sectionMaxX, sectionMaxY, sectionMaxZ)
                            ));
                        } else {
                            // Partial section - create individual cuboids for each sphere block
                            // This ensures we only include blocks that are actually part of the sphere
                            for (int x = sectionMinX; x <= sectionMaxX; x++) {
                                for (int y = sectionMinY; y <= sectionMaxY; y++) {
                                    for (int z = sectionMinZ; z <= sectionMaxZ; z++) {
                                        double dx = x - center.blockX();
                                        double dy = y - center.blockY();
                                        double dz = z - center.blockZ();
                                        if (dx * dx + dy * dy + dz * dz <= radiusSquared) {
                                            BlockVec block = new BlockVec(x, y, z);
                                            result.add(Area.cuboid(block, block));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return result;
        }
    }
}
