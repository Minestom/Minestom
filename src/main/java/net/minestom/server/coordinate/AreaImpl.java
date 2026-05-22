package net.minestom.server.coordinate;

import net.minestom.server.utils.validate.Check;

import java.util.*;

import static net.minestom.server.coordinate.CoordConversion.*;

final class AreaImpl {

    static Area.Cuboid section(int sectionX, int sectionY, int sectionZ) {
        final int minX = sectionX * SECTION_SIZE;
        final int minY = sectionY * SECTION_SIZE;
        final int minZ = sectionZ * SECTION_SIZE;
        final BlockVec min = new BlockVec(minX, minY, minZ);
        return new Cuboid(min, min.add(SECTION_BOUND));
    }

    static Area.Cuboid cube(Point center, int size) {
        Check.argCondition(size < 0, "Cube size must be non-negative: {0}", size);
        return new Cuboid(
                center.sub((double) size / 2).asBlockVec(),
                center.add((double) size / 2).asBlockVec());
    }

    static Area.Cuboid box(Point center, Point size) {
        Check.argCondition(size.x() < 0 || size.y() < 0 || size.z() < 0,
                "Box size must be non-negative on each axis: ({0}, {1}, {2})", size.x(), size.y(), size.z());
        final Point half = size.div(2);
        return new Cuboid(center.sub(half).asBlockVec(), center.add(half).asBlockVec());
    }

    record Single(BlockVec point) implements Area.Single {
        public Single {
            Objects.requireNonNull(point, "Point cannot be null");
        }

        @Override
        public Iterator<BlockVec> iterator() {
            return List.of(point).iterator();
        }

        @Override
        public List<Area.Cuboid> split() {
            return List.of(new AreaImpl.Cuboid(point, point));
        }

        @Override
        public boolean contains(Point point) {
            return this.point.equals(point.asBlockVec());
        }

        @Override
        public long blockCount() {
            return 1;
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
            List<Area.Cuboid> result = new ArrayList<>();
            BlockVec runStart = null;
            BlockVec runEnd = null;
            for (BlockVec block : this) {
                if (runStart == null) {
                    runStart = block;
                    runEnd = block;
                } else if (sameSection(runStart, block) && canExtendAxisAlignedRun(runStart, runEnd, block)) {
                    runEnd = block;
                } else {
                    result.add(Area.cuboid(runStart, runEnd));
                    runStart = block;
                    runEnd = block;
                }
            }
            if (runStart != null) {
                result.add(Area.cuboid(runStart, runEnd));
            }
            return result;
        }

        @Override
        public boolean contains(Point point) {
            final BlockVec block = point.asBlockVec();
            final int targetX = block.blockX(), targetY = block.blockY(), targetZ = block.blockZ();
            final int x1 = start.blockX(), y1 = start.blockY(), z1 = start.blockZ();
            final int x2 = end.blockX(), y2 = end.blockY(), z2 = end.blockZ();
            if (targetX < Math.min(x1, x2) || targetX > Math.max(x1, x2) ||
                    targetY < Math.min(y1, y2) || targetY > Math.max(y1, y2) ||
                    targetZ < Math.min(z1, z2) || targetZ > Math.max(z1, z2)) {
                return false;
            }
            // Walk Bresenham in-place to avoid BlockVec allocation per step
            final int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1), dz = Math.abs(z2 - z1);
            final int sx = x1 < x2 ? 1 : -1, sy = y1 < y2 ? 1 : -1, sz = z1 < z2 ? 1 : -1;
            int x = x1, y = y1, z = z1;
            int err1, err2;
            if (dx >= dy && dx >= dz) {
                err1 = dx / 2;
                err2 = dx / 2;
            } else if (dy >= dz) {
                err1 = dy / 2;
                err2 = dy / 2;
            } else {
                err1 = dz / 2;
                err2 = dz / 2;
            }
            while (true) {
                if (x == targetX && y == targetY && z == targetZ) return true;
                if (x == x2 && y == y2 && z == z2) return false;
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
                } else if (dy >= dz) {
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
            }
        }

        @Override
        public long blockCount() {
            final int startX = start.blockX(), startY = start.blockY(), startZ = start.blockZ();
            final int endX = end.blockX(), endY = end.blockY(), endZ = end.blockZ();
            final long dx = Math.abs((long) endX - startX), dy = Math.abs((long) endY - startY), dz = Math.abs((long) endZ - startZ);
            return Math.max(dx, Math.max(dy, dz)) + 1;
        }
    }

    record Cuboid(BlockVec min, BlockVec max) implements Area.Cuboid {
        public Cuboid {
            Objects.requireNonNull(min, "min cannot be null");
            Objects.requireNonNull(max, "max cannot be null");
            if (min.blockX() > max.blockX() || min.blockY() > max.blockY() || min.blockZ() > max.blockZ()) {
                final BlockVec origMin = min;
                final BlockVec origMax = max;
                min = origMin.min(origMax);
                max = origMin.max(origMax);
            }
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
            final int minX = min.blockX(), minY = min.blockY(), minZ = min.blockZ();
            final int maxX = max.blockX(), maxY = max.blockY(), maxZ = max.blockZ();
            final int minSecX = min.sectionX(), minSecY = min.sectionY(), minSecZ = min.sectionZ();
            final int maxSecX = max.sectionX(), maxSecY = max.sectionY(), maxSecZ = max.sectionZ();

            // Fast path: already within a single section
            if (minSecX == maxSecX && minSecY == maxSecY && minSecZ == maxSecZ) {
                return List.of(this);
            }

            List<Area.Cuboid> result = new ArrayList<>(estimatedSectionCount(minSecX, minSecY, minSecZ, maxSecX, maxSecY, maxSecZ));

            // Split into cuboids per section
            for (int sx = minSecX; sx <= maxSecX; sx++) {
                for (int sy = minSecY; sy <= maxSecY; sy++) {
                    for (int sz = minSecZ; sz <= maxSecZ; sz++) {
                        final int sectionMinX = sectionMin(sx), sectionMinY = sectionMin(sy), sectionMinZ = sectionMin(sz);
                        final int sectionMaxX = sectionMax(sectionMinX), sectionMaxY = sectionMax(sectionMinY), sectionMaxZ = sectionMax(sectionMinZ);

                        // Calculate intersection with this section
                        final int intersectMinX = Math.max(minX, sectionMinX), intersectMinY = Math.max(minY, sectionMinY), intersectMinZ = Math.max(minZ, sectionMinZ);
                        final int intersectMaxX = Math.min(maxX, sectionMaxX), intersectMaxY = Math.min(maxY, sectionMaxY), intersectMaxZ = Math.min(maxZ, sectionMaxZ);

                        // Only add if there's a valid intersection
                        if (intersectMinX <= intersectMaxX && intersectMinY <= intersectMaxY && intersectMinZ <= intersectMaxZ) {
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

        @Override
        public boolean contains(Point point) {
            final BlockVec block = point.asBlockVec();
            final int blockX = block.blockX(), blockY = block.blockY(), blockZ = block.blockZ();
            final int minX = min.blockX(), minY = min.blockY(), minZ = min.blockZ();
            final int maxX = max.blockX(), maxY = max.blockY(), maxZ = max.blockZ();
            return blockX >= minX && blockX <= maxX &&
                    blockY >= minY && blockY <= maxY &&
                    blockZ >= minZ && blockZ <= maxZ;
        }

        @Override
        public long blockCount() {
            final int minX = min.blockX(), minY = min.blockY(), minZ = min.blockZ();
            final int maxX = max.blockX(), maxY = max.blockY(), maxZ = max.blockZ();
            final long width = (long) maxX - minX + 1, height = (long) maxY - minY + 1, depth = (long) maxZ - minZ + 1;
            return width * height * depth;
        }
    }

    record Sphere(BlockVec center, int radius) implements Area.Sphere {
        public Sphere {
            Objects.requireNonNull(center, "Center cannot be null");
            Check.argCondition(radius < 0, "Radius must be non-negative: {0}", radius);
        }

        @Override
        public Iterator<BlockVec> iterator() {
            final int centerX = center.blockX(), centerY = center.blockY(), centerZ = center.blockZ();
            final int radius = this.radius;
            final long radiusSquared = (long) radius * radius;
            return new Iterator<>() {
                private int x;
                private int xEnd = Integer.MIN_VALUE;
                private int y = -radius;
                private int z = -radius;
                private boolean done;

                {
                    advance();
                }

                private void advance() {
                    while (z <= radius) {
                        while (y <= radius) {
                            final long dy = y, dz = z;
                            final long remaining = radiusSquared - dy * dy - dz * dz;
                            if (remaining >= 0) {
                                final int halfWidth = (int) floorSqrt(remaining);
                                x = -halfWidth;
                                xEnd = halfWidth;
                                return;
                            }
                            y++;
                        }
                        y = -radius;
                        z++;
                    }
                    done = true;
                }

                @Override
                public boolean hasNext() {
                    return !done;
                }

                @Override
                public BlockVec next() {
                    if (done) throw new NoSuchElementException();
                    final BlockVec result = new BlockVec(centerX + x, centerY + y, centerZ + z);
                    if (++x > xEnd) {
                        y++;
                        advance();
                    }
                    return result;
                }
            };
        }

        @Override
        public List<Area.Cuboid> split() {
            // Calculate the bounding sections for the sphere
            final int centerX = center.blockX(), centerY = center.blockY(), centerZ = center.blockZ();
            final long radiusSquared = (long) radius * radius;
            final int minSecX = globalToSection(centerX - radius), minSecY = globalToSection(centerY - radius), minSecZ = globalToSection(centerZ - radius);
            final int maxSecX = globalToSection(centerX + radius), maxSecY = globalToSection(centerY + radius), maxSecZ = globalToSection(centerZ + radius);

            List<Area.Cuboid> result = new ArrayList<>(estimatedSectionCount(minSecX, minSecY, minSecZ, maxSecX, maxSecY, maxSecZ));

            // For each section that might contain sphere blocks
            for (int sx = minSecX; sx <= maxSecX; sx++) {
                for (int sy = minSecY; sy <= maxSecY; sy++) {
                    for (int sz = minSecZ; sz <= maxSecZ; sz++) {
                        final int sectionMinX = sectionMin(sx), sectionMinY = sectionMin(sy), sectionMinZ = sectionMin(sz);
                        final int sectionMaxX = sectionMax(sectionMinX), sectionMaxY = sectionMax(sectionMinY), sectionMaxZ = sectionMax(sectionMinZ);

                        if (sectionInsideSphere(sectionMinX, sectionMinY, sectionMinZ, sectionMaxX, sectionMaxY, sectionMaxZ, centerX, centerY, centerZ, radius)) {
                            result.add(Area.section(sx, sy, sz));
                        } else {
                            for (int y = sectionMinY; y <= sectionMaxY; y++) {
                                final long dy = (long) y - centerY;
                                for (int z = sectionMinZ; z <= sectionMaxZ; z++) {
                                    final long dz = (long) z - centerZ;
                                    final long remaining = radiusSquared - dy * dy - dz * dz;
                                    if (remaining < 0) {
                                        continue;
                                    }
                                    final int halfWidth = (int) floorSqrt(remaining);
                                    final int minX = Math.max(sectionMinX, centerX - halfWidth);
                                    final int maxX = Math.min(sectionMaxX, centerX + halfWidth);
                                    if (minX <= maxX) {
                                        result.add(Area.cuboid(new BlockVec(minX, y, z), new BlockVec(maxX, y, z)));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return result;
        }

        @Override
        public boolean contains(Point point) {
            final BlockVec block = point.asBlockVec();
            final int centerX = center.blockX(), centerY = center.blockY(), centerZ = center.blockZ();
            final long dx = (long) block.blockX() - centerX, dy = (long) block.blockY() - centerY, dz = (long) block.blockZ() - centerZ;
            return withinSphereRadius(dx, dy, dz, radius);
        }

        @Override
        public long blockCount() {
            long count = 0;
            final long radiusSquared = (long) radius * radius;
            for (int dx = -radius; dx <= radius; dx++) {
                final long dxSquared = (long) dx * dx;
                for (int dy = -radius; dy <= radius; dy++) {
                    final long remaining = radiusSquared - dxSquared - (long) dy * dy;
                    if (remaining >= 0) {
                        count += 2L * floorSqrt(remaining) + 1;
                    }
                }
            }
            return count;
        }
    }

    private static boolean withinSphereRadius(long dx, long dy, long dz, int radius) {
        if (dx < -radius || dx > radius || dy < -radius || dy > radius || dz < -radius || dz > radius) {
            return false;
        }
        return dx * dx + dy * dy + dz * dz <= (long) radius * radius;
    }

    private static boolean sameSection(BlockVec first, BlockVec second) {
        final int firstX = first.sectionX(), firstY = first.sectionY(), firstZ = first.sectionZ();
        final int secondX = second.sectionX(), secondY = second.sectionY(), secondZ = second.sectionZ();
        return firstX == secondX && firstY == secondY && firstZ == secondZ;
    }

    private static boolean canExtendAxisAlignedRun(BlockVec start, BlockVec end, BlockVec next) {
        final int startX = start.blockX(), startY = start.blockY(), startZ = start.blockZ();
        final int endX = end.blockX(), endY = end.blockY(), endZ = end.blockZ();
        final int nextX = next.blockX(), nextY = next.blockY(), nextZ = next.blockZ();
        final boolean sameX = startX == endX && endX == nextX;
        final boolean sameY = startY == endY && endY == nextY;
        final boolean sameZ = startZ == endZ && endZ == nextZ;
        final boolean adjacentX = Math.abs(nextX - endX) == 1;
        final boolean adjacentY = Math.abs(nextY - endY) == 1;
        final boolean adjacentZ = Math.abs(nextZ - endZ) == 1;
        return sameY && sameZ && adjacentX || sameX && sameZ && adjacentY || sameX && sameY && adjacentZ;
    }

    private static boolean sectionInsideSphere(int sectionMinX, int sectionMinY, int sectionMinZ,
                                               int sectionMaxX, int sectionMaxY, int sectionMaxZ,
                                               int centerX, int centerY, int centerZ, int radius) {
        final long farX = Math.max(Math.abs((long) sectionMinX - centerX), Math.abs((long) sectionMaxX - centerX));
        final long farY = Math.max(Math.abs((long) sectionMinY - centerY), Math.abs((long) sectionMaxY - centerY));
        final long farZ = Math.max(Math.abs((long) sectionMinZ - centerZ), Math.abs((long) sectionMaxZ - centerZ));
        return withinSphereRadius(farX, farY, farZ, radius);
    }

    private static long floorSqrt(long value) {
        if (value == 0) return 0;
        long sqrt = (long) Math.sqrt(value);
        // Math.sqrt may be off by one due to double precision; correct in either direction.
        if (sqrt > 0 && sqrt > value / sqrt) sqrt--;
        else if (sqrt + 1 <= value / (sqrt + 1)) sqrt++;
        return sqrt;
    }

    private static int sectionMin(int section) {
        return section * SECTION_SIZE;
    }

    private static int sectionMax(int sectionMin) {
        return sectionMin + SECTION_BOUND;
    }

    private static int estimatedSectionCount(int minSecX, int minSecY, int minSecZ, int maxSecX, int maxSecY, int maxSecZ) {
        final long count = (long) (maxSecX - minSecX + 1) *
                (maxSecY - minSecY + 1) *
                (maxSecZ - minSecZ + 1);
        return count > 1_000_000 ? 10 : (int) count;
    }
}
