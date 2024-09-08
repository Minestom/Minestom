package net.minestom.server.instance.painter;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.chunk.ChunkUtils;
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
    public Palette sectionAt(int sectionX, int sectionY, int sectionZ) {
        return sectionAt(instructions, sectionX, sectionY, sectionZ);
    }

    sealed interface Instruction {
        record SetBlock(int x, int y, int z, Block block) implements Instruction {
        }
    }

    static final class WorldImpl implements World {
        private final List<Instruction> instructions = new ArrayList<>();

        @Override
        public @UnknownNullability Block getBlock(int x, int y, int z, @NotNull Condition condition) {
            final int sectionX = ChunkUtils.getChunkCoordinate(x);
            final int sectionY = ChunkUtils.getChunkCoordinate(y);
            final int sectionZ = ChunkUtils.getChunkCoordinate(z);
            final Palette palette = sectionAt(instructions, sectionX, sectionY, sectionZ);
            final int localX = ChunkUtils.toSectionRelativeCoordinate(x);
            final int localY = ChunkUtils.toSectionRelativeCoordinate(y);
            final int localZ = ChunkUtils.toSectionRelativeCoordinate(z);
            final int stateId = palette.get(localX, localY, localZ);
            return Block.fromStateId(stateId);
        }

        @Override
        public void setBlock(int x, int y, int z, @NotNull Block block) {
            append(new Instruction.SetBlock(x, y, z, block));
        }

        void append(Instruction instruction) {
            instructions.add(instruction);
        }
    }

    static Palette sectionAt(List<Instruction> instructions, int x, int y, int z) {
        Palette palette = Palette.blocks();
        for (Instruction instruction : instructions) {
            if (!sectionRelevant(instruction, x, y, z)) continue;
            switch (instruction) {
                case Instruction.SetBlock setBlock -> {
                    final int localX = ChunkUtils.toSectionRelativeCoordinate(setBlock.x());
                    final int localY = ChunkUtils.toSectionRelativeCoordinate(setBlock.y());
                    final int localZ = ChunkUtils.toSectionRelativeCoordinate(setBlock.z());
                    palette.set(localX, localY, localZ, setBlock.block().stateId());
                }
            }
        }
        return palette;
    }

    static boolean sectionRelevant(Instruction instruction, int sectionX, int sectionY, int sectionZ) {
        return switch (instruction) {
            case Instruction.SetBlock setBlock -> ChunkUtils.getChunkCoordinate(setBlock.x()) == sectionX &&
                    ChunkUtils.getChunkCoordinate(setBlock.y()) == sectionY &&
                    ChunkUtils.getChunkCoordinate(setBlock.z()) == sectionZ;
        };
    }
}
