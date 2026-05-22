package net.minestom.server.coordinate;

import net.minestom.server.utils.validate.Check;

import java.util.*;

import static net.minestom.server.coordinate.CoordConversion.*;

final class AreaImpl {

    static Area.Cuboid section(int sectionX, int sectionY, int sectionZ) {
        final BlockVec min = new BlockVec(sectionMin(sectionX), sectionMin(sectionY), sectionMin(sectionZ));
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
            final int x1 = start.blockX(), y1 = start.blockY(), z1 = start.blockZ();
            final int x2 = end.blockX(), y2 = end.blockY(), z2 = end.blockZ();
            final int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1), dz = Math.abs(z2 - z1);
            final int sx = x1 < x2 ? 1 : -1, sy = y1 < y2 ? 1 : -1, sz = z1 < z2 ? 1 : -1;
            final int errInit = Math.max(dx, Math.max(dy, dz)) / 2;
            return new Iterator<>() {
                private int x = x1, y = y1, z = z1;
                private int err1 = errInit, err2 = errInit;
                private boolean done;

                @Override
                public boolean hasNext() {
                    return !done;
                }

                @Override
                public BlockVec next() {
                    if (done) throw new NoSuchElementException();
                    final BlockVec result = new BlockVec(x, y, z);
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
                    return result;
                }
            };
        }

        @Override
        public List<Area.Cuboid> split() {
            final int x1 = start.blockX(), y1 = start.blockY(), z1 = start.blockZ();
            final int x2 = end.blockX(), y2 = end.blockY(), z2 = end.blockZ();
            if (x1 == x2 && y1 == y2 && z1 == z2) {
                return List.of(new AreaImpl.Cuboid(start, end));
            }
            final List<Area.Cuboid> result = new ArrayList<>();
            final int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1), dz = Math.abs(z2 - z1);
            final int sx = x1 < x2 ? 1 : -1, sy = y1 < y2 ? 1 : -1, sz = z1 < z2 ? 1 : -1;
            int err1 = Math.max(dx, Math.max(dy, dz)) / 2, err2 = err1;
            int runStartX = x1, runStartY = y1, runStartZ = z1;
            int runEndX = x1, runEndY = y1, runEndZ = z1;
            while (runEndX != x2 || runEndY != y2 || runEndZ != z2) {
                int nextX = runEndX, nextY = runEndY, nextZ = runEndZ;
                if (dx >= dy && dx >= dz) {
                    nextX += sx;
                    err1 -= dy;
                    err2 -= dz;
                    if (err1 < 0) {
                        nextY += sy;
                        err1 += dx;
                    }
                    if (err2 < 0) {
                        nextZ += sz;
                        err2 += dx;
                    }
                } else if (dy >= dz) {
                    nextY += sy;
                    err1 -= dx;
                    err2 -= dz;
                    if (err1 < 0) {
                        nextX += sx;
                        err1 += dy;
                    }
                    if (err2 < 0) {
                        nextZ += sz;
                        err2 += dy;
                    }
                } else {
                    nextZ += sz;
                    err1 -= dx;
                    err2 -= dy;
                    if (err1 < 0) {
                        nextX += sx;
                        err1 += dz;
                    }
                    if (err2 < 0) {
                        nextY += sy;
                        err2 += dz;
                    }
                }
                final boolean sameSection = globalToSection(runStartX) == globalToSection(nextX)
                        && globalToSection(runStartY) == globalToSection(nextY)
                        && globalToSection(runStartZ) == globalToSection(nextZ);
                if (sameSection && canExtendAxisAlignedRun(runStartX, runStartY, runStartZ, runEndX, runEndY, runEndZ, nextX, nextY, nextZ)) {
                    runEndX = nextX;
                    runEndY = nextY;
                    runEndZ = nextZ;
                } else {
                    result.add(buildRunCuboid(runStartX, runStartY, runStartZ, runEndX, runEndY, runEndZ));
                    runStartX = nextX;
                    runStartY = nextY;
                    runStartZ = nextZ;
                    runEndX = nextX;
                    runEndY = nextY;
                    runEndZ = nextZ;
                }
            }
            result.add(buildRunCuboid(runStartX, runStartY, runStartZ, runEndX, runEndY, runEndZ));
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
            final int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1), dz = Math.abs(z2 - z1);
            final int sx = x1 < x2 ? 1 : -1, sy = y1 < y2 ? 1 : -1, sz = z1 < z2 ? 1 : -1;
            int x = x1, y = y1, z = z1;
            int err1 = Math.max(dx, Math.max(dy, dz)) / 2, err2 = err1;
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
            // Fast-path: when callers (split, cube, box, section) pass ordered inputs we keep
            // the original BlockVec references and skip allocating reordered copies.
            if (min.blockX() > max.blockX() || min.blockY() > max.blockY() || min.blockZ() > max.blockZ()) {
                final BlockVec origMin = min;
                min = origMin.min(max);
                max = origMin.max(max);
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
                    final BlockVec vec = new BlockVec(x, y, z);
                    if (x < maxX) {
                        x++;
                    } else if (y < maxY) {
                        x = minX;
                        y++;
                    } else if (z < maxZ) {
                        x = minX;
                        y = minY;
                        z++;
                    } else {
                        hasNext = false;
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
            if (minSecX == maxSecX && minSecY == maxSecY && minSecZ == maxSecZ) {
                return List.of(this);
            }
            final List<Area.Cuboid> result = new ArrayList<>(estimatedSectionCount(minSecX, minSecY, minSecZ, maxSecX, maxSecY, maxSecZ));
            for (int sx = minSecX; sx <= maxSecX; sx++) {
                for (int sy = minSecY; sy <= maxSecY; sy++) {
                    for (int sz = minSecZ; sz <= maxSecZ; sz++) {
                        final int sectionMinX = sectionMin(sx), sectionMinY = sectionMin(sy), sectionMinZ = sectionMin(sz);
                        final int sectionMaxX = sectionMax(sectionMinX), sectionMaxY = sectionMax(sectionMinY), sectionMaxZ = sectionMax(sectionMinZ);
                        final int intersectMinX = Math.max(minX, sectionMinX), intersectMinY = Math.max(minY, sectionMinY), intersectMinZ = Math.max(minZ, sectionMinZ);
                        final int intersectMaxX = Math.min(maxX, sectionMaxX), intersectMaxY = Math.min(maxY, sectionMaxY), intersectMaxZ = Math.min(maxZ, sectionMaxZ);
                        if (intersectMinX <= intersectMaxX && intersectMinY <= intersectMaxY && intersectMinZ <= intersectMaxZ) {
                            result.add(new AreaImpl.Cuboid(
                                    new BlockVec(intersectMinX, intersectMinY, intersectMinZ),
                                    new BlockVec(intersectMaxX, intersectMaxY, intersectMaxZ)));
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
            final int centerX = center.blockX(), centerY = center.blockY(), centerZ = center.blockZ();
            final long radiusSquared = (long) radius * radius;
            final int minSecX = globalToSection(centerX - radius), minSecY = globalToSection(centerY - radius), minSecZ = globalToSection(centerZ - radius);
            final int maxSecX = globalToSection(centerX + radius), maxSecY = globalToSection(centerY + radius), maxSecZ = globalToSection(centerZ + radius);
            final List<Area.Cuboid> result = new ArrayList<>(estimatedSectionCount(minSecX, minSecY, minSecZ, maxSecX, maxSecY, maxSecZ));
            for (int sx = minSecX; sx <= maxSecX; sx++) {
                for (int sy = minSecY; sy <= maxSecY; sy++) {
                    for (int sz = minSecZ; sz <= maxSecZ; sz++) {
                        final int sectionMinX = sectionMin(sx), sectionMinY = sectionMin(sy), sectionMinZ = sectionMin(sz);
                        final int sectionMaxX = sectionMax(sectionMinX), sectionMaxY = sectionMax(sectionMinY), sectionMaxZ = sectionMax(sectionMinZ);
                        if (sectionInsideSphere(sectionMinX, sectionMinY, sectionMinZ, sectionMaxX, sectionMaxY, sectionMaxZ, centerX, centerY, centerZ, radius)) {
                            result.add(AreaImpl.section(sx, sy, sz));
                            continue;
                        }
                        for (int y = sectionMinY; y <= sectionMaxY; y++) {
                            final long dy = (long) y - centerY;
                            for (int z = sectionMinZ; z <= sectionMaxZ; z++) {
                                final long dz = (long) z - centerZ;
                                final long remaining = radiusSquared - dy * dy - dz * dz;
                                if (remaining < 0) continue;
                                final int halfWidth = (int) floorSqrt(remaining);
                                final int minX = Math.max(sectionMinX, centerX - halfWidth);
                                final int maxX = Math.min(sectionMaxX, centerX + halfWidth);
                                if (minX <= maxX) {
                                    result.add(new AreaImpl.Cuboid(new BlockVec(minX, y, z), new BlockVec(maxX, y, z)));
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
            final int radius = this.radius;
            if (radius == 0) return 1;
            final long radiusSquared = (long) radius * radius;
            long count = 2L * radius + 1; // center column
            for (int d = 1; d <= radius; d++) {
                final long remaining = radiusSquared - (long) d * d;
                count += 4L * (2L * floorSqrt(remaining) + 1);
            }
            for (int dx = 1; dx < radius; dx++) {
                final long dxSquared = (long) dx * dx;
                for (int dy = 1; dy < radius; dy++) {
                    final long remaining = radiusSquared - dxSquared - (long) dy * dy;
                    if (remaining < 0) break; // dy only grows; further values stay negative
                    count += 4L * (2L * floorSqrt(remaining) + 1);
                }
            }
            return count;
        }
    }

    private static Cuboid buildRunCuboid(int startX, int startY, int startZ,
                                         int endX, int endY, int endZ) {
        final BlockVec startVec = new BlockVec(startX, startY, startZ);
        final BlockVec endVec = (startX == endX && startY == endY && startZ == endZ)
                ? startVec
                : new BlockVec(endX, endY, endZ);
        return new Cuboid(startVec, endVec);
    }

    private static boolean withinSphereRadius(long dx, long dy, long dz, int radius) {
        if (dx < -radius || dx > radius || dy < -radius || dy > radius || dz < -radius || dz > radius) {
            return false;
        }
        return dx * dx + dy * dy + dz * dz <= (long) radius * radius;
    }

    private static boolean canExtendAxisAlignedRun(int startX, int startY, int startZ,
                                                   int endX, int endY, int endZ,
                                                   int nextX, int nextY, int nextZ) {
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
        long sqrt = (long) Math.sqrt(value);
        // Math.sqrt may round up to an exact integer for value > 2^52; correct by one if so.
        if (sqrt > 0 && sqrt * sqrt > value) sqrt--;
        return sqrt;
    }

    private static int sectionMin(int section) {
        return section * SECTION_SIZE;
    }

    private static int sectionMax(int sectionMin) {
        return sectionMin + SECTION_BOUND;
    }

    private static int estimatedSectionCount(int minSecX, int minSecY, int minSecZ, int maxSecX, int maxSecY, int maxSecZ) {
        final long count = (long) (maxSecX - minSecX + 1) * (maxSecY - minSecY + 1) * (maxSecZ - minSecZ + 1);
        // Clamp to avoid huge initial allocations on pathologically large bounding boxes.
        return count > 1_000_000 ? 10 : (int) count;
    }
}
