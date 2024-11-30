package net.minestom.server.area;

import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Area extends Iterable<Point> {

    static @NotNull Area fromList(@NotNull List<? extends Point> list) {
        return AreaImpl.fromList(list);
    }

    static @NotNull Area fill(@NotNull Point point1, @NotNull Point point2) {
        return new AreaImpl.Fill(point1, point2);
    }

    static @NotNull Area.Path path() {
        return new AreaImpl.Path();
    }

    static @NotNull Area randomizer(@NotNull Area area, double probability) {
        return AreaImpl.Randomize(area, probability);
    }

    @NotNull List<@NotNull Point> asList();

    interface Path {
        @NotNull Area.Path north(double factor);

        @NotNull Area.Path south(double factor);

        @NotNull Area.Path east(double factor);

        @NotNull Area.Path west(double factor);

        @NotNull Area.Path up(double factor);

        @NotNull Area.Path down(double factor);

        @NotNull Area end();

        default @NotNull Area.Path north() {
            return north(1);
        }

        default @NotNull Area.Path south() {
            return south(1);
        }

        default @NotNull Area.Path east() {
            return east(1);
        }

        default @NotNull Area.Path west() {
            return west(1);
        }

        default @NotNull Area.Path up() {
            return up(1);
        }

        default @NotNull Area.Path down() {
            return down(1);
        }
    }
}
