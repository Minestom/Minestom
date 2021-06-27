package net.minestom.server.area;

import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;

public interface Area extends Iterable<BlockPosition> {

    static @NotNull Area fill(@NotNull BlockPosition pos1, @NotNull BlockPosition pos2) {
        return new AreaImpl.Fill(pos1, pos2);
    }

    static @NotNull Area.Path path() {
        return new AreaImpl.Path();
    }

    static @NotNull Area randomizer(Area area, double probability) {
        return new AreaImpl.Randomizer(area, probability);
    }

    interface Path {
        @NotNull Area.Path north(int factor);

        @NotNull Area.Path south(int factor);

        @NotNull Area.Path east(int factor);

        @NotNull Area.Path west(int factor);

        @NotNull Area.Path up(int factor);

        @NotNull Area.Path down(int factor);

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
