package net.minestom.server.coordinate;

import net.minestom.server.utils.validate.Check;

import java.util.*;

import static net.minestom.server.coordinate.CoordConversion.SECTION_BOUND;
import static net.minestom.server.coordinate.CoordConversion.SECTION_SIZE;

final class AreaImpl {

    static Area.Cuboid section(int sectionX, int sectionY, int sectionZ) {
        final BlockVec min = new BlockVec(sectionX * SECTION_SIZE, sectionY * SECTION_SIZE, sectionZ * SECTION_SIZE);
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
        public boolean contains(int x, int y, int z) {
            return this.point.blockX() == x && this.point.blockY() == y && this.point.blockZ() == z;
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
            final int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1), dz = Math.abs(z2 - z1);
            // Axis-aligned lines cover the same blocks as an inclusive cuboid; reuse Cuboid.split.
            if ((dx == 0 ? 1 : 0) + (dy == 0 ? 1 : 0) + (dz == 0 ? 1 : 0) >= 2) {
                return new AreaImpl.Cuboid(
                        new BlockVec(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2)),
                        new BlockVec(Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2))).split();
            }
            final List<Area.Cuboid> result = new ArrayList<>();
            final int sx = x1 < x2 ? 1 : -1, sy = y1 < y2 ? 1 : -1, sz = z1 < z2 ? 1 : -1;
            int err1 = Math.max(dx, Math.max(dy, dz)) / 2, err2 = err1;
            int runStartX = x1, runStartY = y1, runStartZ = z1;
            int runStartSecX = x1 >> 4, runStartSecY = y1 >> 4, runStartSecZ = z1 >> 4;
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
                final int nextSecX = nextX >> 4, nextSecY = nextY >> 4, nextSecZ = nextZ >> 4;
                final boolean sameSection = runStartSecX == nextSecX && runStartSecY == nextSecY && runStartSecZ == nextSecZ;
                if (!sameSection || !canExtendAxisAlignedRun(runStartX, runStartY, runStartZ, runEndX, runEndY, runEndZ, nextX, nextY, nextZ)) {
                    result.add(buildRunCuboid(runStartX, runStartY, runStartZ, runEndX, runEndY, runEndZ));
                    runStartX = nextX;
                    runStartY = nextY;
                    runStartZ = nextZ;
                    runStartSecX = nextSecX;
                    runStartSecY = nextSecY;
                    runStartSecZ = nextSecZ;
                }
                runEndX = nextX;
                runEndY = nextY;
                runEndZ = nextZ;
            }
            result.add(buildRunCuboid(runStartX, runStartY, runStartZ, runEndX, runEndY, runEndZ));
            return result;
        }

        @Override
        public boolean contains(int targetX, int targetY, int targetZ) {
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
            final int minSecX = minX >> 4, minSecY = minY >> 4, minSecZ = minZ >> 4;
            final int maxSecX = maxX >> 4, maxSecY = maxY >> 4, maxSecZ = maxZ >> 4;
            if (minSecX == maxSecX && minSecY == maxSecY && minSecZ == maxSecZ) {
                return List.of(this);
            }
            final long total = (long) (maxSecX - minSecX + 1) * (maxSecY - minSecY + 1) * (maxSecZ - minSecZ + 1);
            final List<Area.Cuboid> result = new ArrayList<>(total > 1_000_000 ? 16 : (int) total);
            for (int sx = minSecX; sx <= maxSecX; sx++) {
                final int sectionBaseX = sx << 4;
                final int ixMin = sx == minSecX ? minX : sectionBaseX;
                final int ixMax = sx == maxSecX ? maxX : sectionBaseX | SECTION_BOUND;
                for (int sy = minSecY; sy <= maxSecY; sy++) {
                    final int sectionBaseY = sy << 4;
                    final int iyMin = sy == minSecY ? minY : sectionBaseY;
                    final int iyMax = sy == maxSecY ? maxY : sectionBaseY | SECTION_BOUND;
                    for (int sz = minSecZ; sz <= maxSecZ; sz++) {
                        final int sectionBaseZ = sz << 4;
                        final int izMin = sz == minSecZ ? minZ : sectionBaseZ;
                        final int izMax = sz == maxSecZ ? maxZ : sectionBaseZ | SECTION_BOUND;
                        result.add(new AreaImpl.Cuboid(
                                new BlockVec(ixMin, iyMin, izMin),
                                new BlockVec(ixMax, iyMax, izMax)));
                    }
                }
            }
            return result;
        }

        @Override
        public boolean contains(int x, int y, int z) {
            final int minX = min.blockX(), minY = min.blockY(), minZ = min.blockZ();
            final int maxX = max.blockX(), maxY = max.blockY(), maxZ = max.blockZ();
            return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
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
            final int radius = this.radius;
            if (radius == 0) {
                return List.of(new AreaImpl.Cuboid(center, center));
            }
            final long radiusSquared = (long) radius * radius;
            final int bbMinX = centerX - radius, bbMinY = centerY - radius, bbMinZ = centerZ - radius;
            final int bbMaxX = centerX + radius, bbMaxY = centerY + radius, bbMaxZ = centerZ + radius;
            final int minSecX = bbMinX >> 4, minSecY = bbMinY >> 4, minSecZ = bbMinZ >> 4;
            final int maxSecX = bbMaxX >> 4, maxSecY = bbMaxY >> 4, maxSecZ = bbMaxZ >> 4;
            final long sectionCount = (long) (maxSecX - minSecX + 1) * (maxSecY - minSecY + 1) * (maxSecZ - minSecZ + 1);
            final List<Area.Cuboid> result = new ArrayList<>(sectionCount > 1_000_000 ? 16 : (int) sectionCount);
            // Scratch buffers for the current and previous z-slice rectangles (xMin, xMax, yStart, yEnd per rect).
            int[] prevSlice = new int[64];
            int[] currentSlice = new int[64];
            for (int sx = minSecX; sx <= maxSecX; sx++) {
                final int sectionMinX = sx << 4, sectionMaxX = sectionMinX | SECTION_BOUND;
                for (int sy = minSecY; sy <= maxSecY; sy++) {
                    final int sectionMinY = sy << 4, sectionMaxY = sectionMinY | SECTION_BOUND;
                    final int yLo = Math.max(bbMinY, sectionMinY), yHi = Math.min(bbMaxY, sectionMaxY);
                    for (int sz = minSecZ; sz <= maxSecZ; sz++) {
                        final int sectionMinZ = sz << 4, sectionMaxZ = sectionMinZ | SECTION_BOUND;
                        final int zLo = Math.max(bbMinZ, sectionMinZ), zHi = Math.min(bbMaxZ, sectionMaxZ);
                        if (sectionInsideSphere(sectionMinX, sectionMinY, sectionMinZ, sectionMaxX, sectionMaxY, sectionMaxZ, centerX, centerY, centerZ, radius)) {
                            result.add(AreaImpl.section(sx, sy, sz));
                            continue;
                        }
                        int prevCount = 0;
                        int prevZStart = Integer.MIN_VALUE;
                        for (int z = zLo; z <= zHi; z++) {
                            final long dz = (long) z - centerZ;
                            final long remZ = radiusSquared - dz * dz;
                            if (remZ < 0) {
                                if (prevCount > 0) {
                                    emitSliceCuboids(result, prevSlice, prevCount, prevZStart, z - 1);
                                    prevCount = 0;
                                }
                                continue;
                            }
                            // Build the (x, y) rectangles for this z slice via y-row merging.
                            int currentCount = 0;
                            int runMinX = 0, runMaxX = -1;
                            int runStartY = Integer.MIN_VALUE, runEndY = Integer.MIN_VALUE;
                            for (int y = yLo; y <= yHi; y++) {
                                final long dy = (long) y - centerY;
                                final long rem = remZ - dy * dy;
                                int stripMinX = 0, stripMaxX = -1;
                                if (rem >= 0) {
                                    final int halfWidth = (int) floorSqrt(rem);
                                    stripMinX = Math.max(sectionMinX, centerX - halfWidth);
                                    stripMaxX = Math.min(sectionMaxX, centerX + halfWidth);
                                }
                                final boolean hasStrip = stripMinX <= stripMaxX;
                                if (hasStrip && runStartY != Integer.MIN_VALUE && stripMinX == runMinX && stripMaxX == runMaxX) {
                                    runEndY = y;
                                } else {
                                    if (runStartY != Integer.MIN_VALUE) {
                                        if ((currentCount + 1) * 4 > currentSlice.length) {
                                            currentSlice = Arrays.copyOf(currentSlice, currentSlice.length * 2);
                                        }
                                        final int base = currentCount * 4;
                                        currentSlice[base] = runMinX;
                                        currentSlice[base + 1] = runMaxX;
                                        currentSlice[base + 2] = runStartY;
                                        currentSlice[base + 3] = runEndY;
                                        currentCount++;
                                        runStartY = Integer.MIN_VALUE;
                                    }
                                    if (hasStrip) {
                                        runMinX = stripMinX;
                                        runMaxX = stripMaxX;
                                        runStartY = y;
                                        runEndY = y;
                                    }
                                }
                            }
                            if (runStartY != Integer.MIN_VALUE) {
                                if ((currentCount + 1) * 4 > currentSlice.length) {
                                    currentSlice = Arrays.copyOf(currentSlice, currentSlice.length * 2);
                                }
                                final int base = currentCount * 4;
                                currentSlice[base] = runMinX;
                                currentSlice[base + 1] = runMaxX;
                                currentSlice[base + 2] = runStartY;
                                currentSlice[base + 3] = runEndY;
                                currentCount++;
                            }
                            // Compare this z slice to the prev one to extend a z-run if they match.
                            if (currentCount > 0 && currentCount == prevCount
                                    && Arrays.equals(currentSlice, 0, currentCount * 4, prevSlice, 0, currentCount * 4)) {
                                continue;
                            }
                            if (prevCount > 0) {
                                emitSliceCuboids(result, prevSlice, prevCount, prevZStart, z - 1);
                            }
                            final int[] tmp = prevSlice;
                            prevSlice = currentSlice;
                            currentSlice = tmp;
                            prevCount = currentCount;
                            prevZStart = z;
                        }
                        if (prevCount > 0) {
                            emitSliceCuboids(result, prevSlice, prevCount, prevZStart, zHi);
                        }
                    }
                }
            }
            return result;
        }

        @Override
        public boolean contains(int x, int y, int z) {
            final int centerX = center.blockX(), centerY = center.blockY(), centerZ = center.blockZ();
            final long dx = (long) x - centerX, dy = (long) y - centerY, dz = (long) z - centerZ;
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

    private static void emitSliceCuboids(List<Area.Cuboid> out, int[] slice, int count, int zStart, int zEnd) {
        for (int i = 0; i < count; i++) {
            final int base = i * 4;
            out.add(new AreaImpl.Cuboid(
                    new BlockVec(slice[base], slice[base + 2], zStart),
                    new BlockVec(slice[base + 1], slice[base + 3], zEnd)));
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
        return (sameY && sameZ && adjacentX) || (sameX && sameZ && adjacentY) || (sameX && sameY && adjacentZ);
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
}
