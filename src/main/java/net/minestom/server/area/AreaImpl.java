package net.minestom.server.area;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.UnaryOperator;

class AreaImpl {

    @SuppressWarnings("unchecked")
    static Area fromList(List<? extends Point> list) {
        final var copy = List.copyOf(list); // Defensive copy
        return new Area() {
            @Override
            public @NotNull List<@NotNull Point> asList() {
                return (List<Point>) copy;
            }

            @NotNull
            @Override
            public Iterator<Point> iterator() {
                return (Iterator<Point>) copy.iterator();
            }
        };
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

        @NotNull
        @Override
        public Iterator<Point> iterator() {
            // TODO implement
            return new Iterator<>() {

                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public Point next() {
                    return null;
                }
            };
        }

        @Override
        public @NotNull List<@NotNull Point> asList() {
            // TODO
            return null;
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
            return fromList(positions);
        }

        private Area.Path with(UnaryOperator<Point> operator) {
            this.currentPosition = operator.apply(currentPosition);
            this.positions.add(currentPosition);
            return this;
        }
    }

    static Area Randomize(Area area, double probability) {
        List<Point> points = new ArrayList<>();
        var localRandom = ThreadLocalRandom.current();
        area.forEach(blockPosition -> {
            final double value = localRandom.nextDouble();
            if (value <= probability) {
                points.add(blockPosition);
            }
        });
        return fromList(points);
    }
}
