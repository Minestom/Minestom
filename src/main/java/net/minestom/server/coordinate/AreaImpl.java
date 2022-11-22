package net.minestom.server.coordinate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

class AreaImpl {

    static Area fromCollection(Collection<? extends Point> collection) {
        // Detect any nested nxnxn areas, and create them
        Set<Point> points = collection.stream()
                .map(point -> new Vec(point.blockX(), point.blockY(), point.blockZ()))
                .collect(Collectors.toSet());
        return new SetArea(points);
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

    static Area intersection(Area[] children) {
        if (children.length == 0) {
            throw new IllegalArgumentException("Must have at least one child");
        }
        Set<Point> points = new HashSet<>(StreamSupport.stream(children[0].spliterator(), false).toList());

        for (Area child : children) {
            points.retainAll(StreamSupport.stream(child.spliterator(), false).toList());
        }

        return fromCollection(points);
    }

    record SetArea(Set<Point> points, Point min, Point max) implements Area {

        public SetArea(Set<Point> points) {
            this(points, findMin(points), findMax(points));

            if (!isFullyConnected()) {
                throw new IllegalArgumentException("Points must be fully connected");
            }
        }

        public boolean isFullyConnected() {
            if (points.size() == 1) return true;
            Set<Point> connected = new HashSet<>();

            for (Point point : points) {
                connected.add(point.add(1, 0, 0));
                connected.add(point.add(-1, 0, 0));
                connected.add(point.add(0, 1, 0));
                connected.add(point.add(0, -1, 0));
                connected.add(point.add(0, 0, 1));
                connected.add(point.add(0, 0, -1));
            }

            return connected.containsAll(points);
        }

        @NotNull
        @Override
        public Iterator<Point> iterator() {
            return points.iterator();
        }
    }

    static final class Fill implements Area {
        private final Point min, max;

        Fill(Point pos1, Point pos2) {
            this.min = new Vec(Math.min(pos1.x(), pos2.x()),
                    Math.min(pos1.y(), pos2.y()),
                    Math.min(pos1.z(), pos2.z()));
            this.max = new Vec(Math.max(pos1.x(), pos2.x()),
                    Math.max(pos1.y(), pos2.y()),
                    Math.max(pos1.z(), pos2.z()));
        }

        @Override
        public @NotNull Point min() {
            return min;
        }

        @Override
        public @NotNull Point max() {
            return max;
        }

        public boolean contains(Point pos) {
            return pos.x() >= min.x() && pos.x() <= max.x() &&
                    pos.y() >= min.y() && pos.y() <= max.y() &&
                    pos.z() >= min.z() && pos.z() <= max.z();
        }

        @NotNull
        @Override
        public Iterator<Point> iterator() {
            return new Iterator<>() {
                private int x = min.blockX();
                private int y = min.blockY();
                private int z = min.blockZ();

                @Override
                public boolean hasNext() {
                    return x < max.blockX() && y < max.blockY() && z < max.blockZ();
                }

                @Override
                public Point next() {
                    Point point = new Vec(x, y, z);
                    z++;
                    if (z >= max.blockZ()) {
                        z = min.blockZ();
                        y++;
                        if (y >= max.blockY()) {
                            y = min.blockY();
                            x++;
                        }
                    }
                    return point;
                }
            };
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

    record Union(Collection<Area> children, Point min, Point max) implements Area, Area.HasChildren {

        public Union(Collection<Area> children) {
            this(children,
                    findMin(children.stream().map(Area::min).toList()),
                    findMax(children.stream().map(Area::max).toList()));
        }

        @Override
        public @NotNull Iterator<Point> iterator() {
            return new Iterator<>() {
                private final Iterator<Area> areaIterator = children.iterator();
                private Iterator<Point> currentIterator = areaIterator.next().iterator();

                @Override
                public boolean hasNext() {
                    if (currentIterator.hasNext()) {
                        return true;
                    }

                    while (areaIterator.hasNext()) {
                        currentIterator = areaIterator.next().iterator();
                        if (currentIterator.hasNext()) {
                            return true;
                        }
                    }

                    return false;
                }

                @Override
                public Point next() {
                    return currentIterator.next();
                }
            };
        }
    }
}