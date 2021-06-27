package net.minestom.server.area;

import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.UnaryOperator;

class AreaImpl {

    static class Fill implements Area {

        private final BlockPosition min, max;

        protected Fill(BlockPosition pos1, BlockPosition pos2) {
            this.min = new BlockPosition(Math.min(pos1.getX(), pos2.getX()),
                    Math.min(pos1.getY(), pos2.getY()),
                    Math.min(pos1.getZ(), pos2.getZ()));
            this.max = new BlockPosition(Math.max(pos1.getX(), pos2.getX()),
                    Math.max(pos1.getY(), pos2.getY()),
                    Math.max(pos1.getZ(), pos2.getZ()));
        }

        @NotNull
        @Override
        public Iterator<BlockPosition> iterator() {
            // TODO implement
            return new Iterator<>() {

                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public BlockPosition next() {
                    return null;
                }
            };
        }
    }

    static class Path implements Area.Path {

        private final List<BlockPosition> positions = new ArrayList<>();
        private BlockPosition currentPosition;

        @Override
        public Area.@NotNull Path north(int factor) {
            return with(blockPosition -> blockPosition.add(0, 0, -factor));
        }

        @Override
        public Area.@NotNull Path south(int factor) {
            return with(blockPosition -> blockPosition.add(0, 0, factor));
        }

        @Override
        public Area.@NotNull Path east(int factor) {
            return with(blockPosition -> blockPosition.add(factor, 0, 0));
        }

        @Override
        public Area.@NotNull Path west(int factor) {
            return with(blockPosition -> blockPosition.add(-factor, 0, 0));
        }

        @Override
        public Area.@NotNull Path up(int factor) {
            return with(blockPosition -> blockPosition.add(0, factor, 0));
        }

        @Override
        public Area.@NotNull Path down(int factor) {
            return with(blockPosition -> blockPosition.add(0, -factor, 0));
        }

        @Override
        public @NotNull Area end() {
            return positions::iterator;
        }

        private Area.Path with(UnaryOperator<BlockPosition> operator) {
            this.currentPosition = operator.apply(currentPosition.clone());
            positions.add(currentPosition);
            return this;
        }
    }

    static class Randomizer implements Area {
        private final List<BlockPosition> positions = new ArrayList<>();

        protected Randomizer(Area area, double probability) {
            var random = ThreadLocalRandom.current();
            area.forEach(blockPosition -> {
                final double value = random.nextDouble();
                if (value <= probability) {
                    positions.add(blockPosition);
                }
            });
        }

        @NotNull
        @Override
        public Iterator<BlockPosition> iterator() {
            return positions.iterator();
        }
    }
}
