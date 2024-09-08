package net.minestom.server.instance.painter;

import java.util.ArrayList;
import java.util.List;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.painter.PainterImpl.Bounds;
import net.minestom.server.instance.painter.PainterImpl.Instruction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record PreparedOperation(Bounds bounds, List<Instruction> instructions) {

    static @Nullable PreparedOperation compile(Painter.Operation operation) {
        StagingRelWorld stagingRelWorld = new StagingRelWorld();
        operation.apply(stagingRelWorld);
        if (stagingRelWorld.relInstructions.isEmpty()) return null;
        return new PreparedOperation(new Bounds(stagingRelWorld.min, stagingRelWorld.max.add(1, 1, 1)), stagingRelWorld.relInstructions);
    }

    static final class StagingRelWorld implements Painter.World {

        private final List<Instruction> relInstructions = new ArrayList<>();

        private Vec min = new Vec(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        private Vec max = new Vec(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE);

        private void adjustBounds(int x, int y, int z) {
            if (x < min.x()) min = min.withX(x);
            if (y < min.y()) min = min.withY(y);
            if (z < min.z()) min = min.withZ(z);

            if (x > max.x()) max = max.withX(x);
            if (y > max.y()) max = max.withY(y);
            if (z > max.z()) max = max.withZ(z);
        }

        @Override
        public void cuboid(Point min, Point max, Block block) {
            relInstructions.add(new Instruction.Cuboid(Vec.fromPoint(min), Vec.fromPoint(max), block));

            // adjust bounds to include the cuboid
            adjustBounds(min.blockX(), min.blockY(), min.blockZ());
            adjustBounds(max.blockX(), max.blockY(), max.blockZ());
        }

        @Override
        public void setBlock(int x, int y, int z, @NotNull Block block) {
            relInstructions.add(new Instruction.SetBlock(x, y, z, block));
            adjustBounds(x, y, z);
        }

        @Override
        public void operation2d(Painter.PosPredicate noise, Painter.Operation operation) {
            throw new UnsupportedOperationException("Noise operations are not supported in prepared operations");
        }
    }
}
