package net.minestom.server.instance.painter;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

record PainterImpl(List<Instruction> instructions) implements Painter {
    public PainterImpl {
        instructions = List.copyOf(instructions);
    }

    static PainterImpl paint(Consumer<World> consumer) {
        WorldImpl world = new WorldImpl();
        consumer.accept(world);
        return new PainterImpl(world.instructions);
    }

    @Override
    public Palette sectionAt(int x, int y, int z) {
        Palette palette = Palette.blocks();
        for (Instruction instruction : instructions) {
            // TODO
        }
        return palette;
    }

    sealed interface Instruction {
        record SetBlock(int x, int y, int z, Block block) implements Instruction {
        }
    }

    static final class WorldImpl implements World {
        private final List<Instruction> instructions = new ArrayList<>();

        @Override
        public @UnknownNullability Block getBlock(int x, int y, int z, @NotNull Condition condition) {
            return Block.AIR;
        }

        @Override
        public void setBlock(int x, int y, int z, @NotNull Block block) {
            append(new Instruction.SetBlock(x, y, z, block));
        }

        void append(Instruction instruction) {
            instructions.add(instruction);
        }
    }
}
