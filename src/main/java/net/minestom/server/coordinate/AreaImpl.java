package net.minestom.server.coordinate;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

final class AreaImpl {

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
}
