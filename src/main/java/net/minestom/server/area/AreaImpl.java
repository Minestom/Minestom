package net.minestom.server.area;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

class AreaImpl {

    static Area fromCollection(Collection<? extends Point> collection) {
        // Detect any nested nxnxn areas, and create them
        Set<Point> points = collection.stream()
                .map(point -> new Vec(point.blockX(), point.blockY(), point.blockZ()))
                .collect(Collectors.toSet());

        Point largest = findLargestNxNxN(points);

        Set<Area> newAreas = new HashSet<>();
        while (largest != null) {
            Area area = createNxNxNArea(largest, points);
            newAreas.add(area);
            area.allBlocks(points::remove);
            largest = findLargestNxNxN(points);
        }
        newAreas.add(new SetArea(points));

        return new Union(newAreas);
    }

    private static Area createNxNxNArea(Point largest, Set<Point> points) {
        int size = 1;
        while (findNxNxN(points, largest, size)) {
            size++;
        }
        return new AreaImpl.Fill(largest, largest.add(size, size, size));
    }

    private static @Nullable Point findLargestNxNxN(Set<Point> points) {
        Point largestNxNxN = null;
        int largestNxNxNSize = 1;

        for (Point point : points) {
            int size = 1;
            while (findNxNxN(points, point, size)) {
                size++;
            }
            if (size > largestNxNxNSize) {
                largestNxNxN = point;
                largestNxNxNSize = size;
            }
        }

        return largestNxNxN;
    }

    private static boolean findNxNxN(Set<Point> points, Point min, int n) {
        for (int x = min.blockX(); x < min.blockX() + n; x++) {
            for (int y = min.blockY(); y < min.blockY() + n; y++) {
                for (int z = min.blockZ(); z < min.blockZ() + n; z++) {
                    if (!points.contains(new Vec(x, y, z))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static Point findMin(Collection<Point> children) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;

        for (Point point : children) {
            minX = Math.min(minX, point.blockX());
            minY = Math.min(minY, point.blockY());
            minZ = Math.min(minZ, point.blockZ());
        }

        return new Vec(minX, minY, minZ);
    }

    private static Point findMax(Collection<Point> children) {
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        for (Point point : children) {
            maxX = Math.max(maxX, point.blockX());
            maxY = Math.max(maxY, point.blockY());
            maxZ = Math.max(maxZ, point.blockZ());
        }

        return new Vec(maxX, maxY, maxZ);
    }

    interface HasChildren {
        @NotNull Collection<Area> children();
    }

    record SetArea(Set<Point> points, Point min, Point max) implements Area {

        public SetArea {
            if (!isFullyConnected()) {
                throw new IllegalStateException("Area is not fully connected");
            }
        }

        public SetArea(Set<Point> points) {
            this(points, findMin(points), findMax(points));
        }

        public boolean isFullyConnected() {
            Map<Point, Boolean> isConnected = new HashMap<>();

            for (Point point : points) {
                isConnected.putIfAbsent(point, false);
                isConnected.put(point.add(1, 0, 0), true);
                isConnected.put(point.add(-1, 0, 0), true);
                isConnected.put(point.add(0, 1, 0), true);
                isConnected.put(point.add(0, -1, 0), true);
                isConnected.put(point.add(0, 0, 1), true);
                isConnected.put(point.add(0, 0, -1), true);
            }

            return isConnected.values()
                    .stream()
                    .allMatch(Boolean::booleanValue);
        }

        @Override
        public void allBlocks(@NotNull Consumer<@NotNull Point> consumer) {
            points.forEach(consumer);
        }
    }

    static class Fill implements Area {
        private final Point min, max;

        protected Fill(Point pos1, Point pos2) {
            this.min = new Vec(Math.min(pos1.x(), pos2.x()),
                    Math.min(pos1.y(), pos2.y()),
                    Math.min(pos1.z(), pos2.z()));
            this.max = new Vec(Math.max(pos1.x(), pos2.x()),
                    Math.max(pos1.y(), pos2.y()),
                    Math.max(pos1.z(), pos2.z()));
        }

        @Override
        public void allBlocks(@NotNull Consumer<@NotNull Point> consumer) {
            for (int x = min.blockX(); x < max.blockX(); x++) {
                for (int y = min.blockY(); y < max.blockY(); y++) {
                    for (int z = min.blockZ(); z < max.blockZ(); z++) {
                        consumer.accept(new Vec(x, y, z));
                    }
                }
            }
        }

        @Override
        public @NotNull Point min() {
            return min;
        }

        @Override
        public @NotNull Point max() {
            return max;
        }
    }

    static class Path implements Area.Path {
        private final List<Point> positions = new ArrayList<>();
        private Point currentPosition;

        @Override
        public Area.@NotNull Path north(double factor) {
            return with(blockPosition -> blockPosition.add(0, 0, -factor));
        }

        @Override
        public Area.@NotNull Path south(double factor) {
            return with(blockPosition -> blockPosition.add(0, 0, factor));
        }

        @Override
        public Area.@NotNull Path east(double factor) {
            return with(blockPosition -> blockPosition.add(factor, 0, 0));
        }

        @Override
        public Area.@NotNull Path west(double factor) {
            return with(blockPosition -> blockPosition.add(-factor, 0, 0));
        }

        @Override
        public Area.@NotNull Path up(double factor) {
            return with(blockPosition -> blockPosition.add(0, factor, 0));
        }

        @Override
        public Area.@NotNull Path down(double factor) {
            return with(blockPosition -> blockPosition.add(0, -factor, 0));
        }

        @Override
        public @NotNull Area end() {
            return fromCollection(positions);
        }

        private Area.Path with(UnaryOperator<Point> operator) {
            this.currentPosition = operator.apply(currentPosition);
            this.positions.add(currentPosition);
            return this;
        }
    }

    record Union(Collection<Area> children, Point min, Point max) implements Area, HasChildren {

        public Union(Collection<Area> children) {
            this(children,
                    findMin(children.stream().map(Area::min).toList()),
                    findMax(children.stream().map(Area::max).toList()));
        }

        @Override
        public void allBlocks(@NotNull Consumer<@NotNull Point> consumer) {
            children.forEach(area -> area.allBlocks(consumer));
        }
    }

    record Intersection(Area area) implements Area {

        @SafeVarargs
        public Intersection(Collection<Area>... children) {
            this(areaFromChildren(children));
        }

        private static Area areaFromChildren(Collection<Area>[] children) {
            int size = children.length;

            Map<Point, Integer> pointCount = new HashMap<>();

            for (Collection<Area> child : children) {
                for (Area area : child) {
                    area.allBlocks(point -> pointCount.merge(point, 1, Integer::sum));
                }
            }

            Set<Point> points = new HashSet<>();

            for (Map.Entry<Point, Integer> entry : pointCount.entrySet()) {
                if (entry.getValue() == size) {
                    points.add(entry.getKey());
                }
            }

            return fromCollection(points);
        }

        @Override
        public void allBlocks(@NotNull Consumer<@NotNull Point> consumer) {
            area.allBlocks(consumer);
        }

        @Override
        public @NotNull Point min() {
            return area.min();
        }

        @Override
        public @NotNull Point max() {
            return area.max();
        }
    }
}