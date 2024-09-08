package net.minestom.server.instance.painter;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
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

    static PainterImpl paint(Consumer<ReadableWorld> consumer) {
        WorldImpl world = new WorldImpl();
        consumer.accept(world);
        return new PainterImpl(world.instructions);
    }

    @Override
    public Palette sectionAt(int sectionX, int sectionY, int sectionZ) {
        return sectionAt(instructions, sectionX, sectionY, sectionZ);
    }

    record Bounds(Vec min, Vec max) {
    }

    sealed interface Instruction {

        record SetBlock(int x, int y, int z, Block block) implements Instruction {
        }

        record Operation2d(PosPredicate test, PreparedOperation operation) implements Instruction {
        }

        record Cuboid(Vec min, Vec max, BlockProvider blockProvider) implements Instruction {
        }
    }

    static final class WorldImpl implements ReadableWorld {
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

        @Override
        public void cuboid(Point min, Point max, BlockProvider blockProvider) {
            append(new Instruction.Cuboid(Vec.fromPoint(min), Vec.fromPoint(max), blockProvider));
        }

        @Override
        public void operation2d(PosPredicate noise, Operation operation) {
            append(new Instruction.Operation2d(noise, PreparedOperation.compile(operation)));
        }
    }

    static void applyInstruction(int sectionX, int sectionY, int sectionZ, Palette palette, Point offset, Instruction instruction) {
        if (!sectionRelevant(instruction, sectionX, sectionY, sectionZ, offset)) return;
        switch (instruction) {
            case Instruction.SetBlock setBlock -> {
                final int localX = ChunkUtils.toSectionRelativeCoordinate(setBlock.x() + offset.blockX());
                final int localY = ChunkUtils.toSectionRelativeCoordinate(setBlock.y() + offset.blockY());
                final int localZ = ChunkUtils.toSectionRelativeCoordinate(setBlock.z() + offset.blockZ());
                palette.set(localX, localY, localZ, setBlock.block().stateId());
            }
            case Instruction.Operation2d noise2D -> {

                // TODO: judge how much we need to generate based on the operation's bounds
                Bounds bounds = noise2D.operation().bounds();

                for (int x = 0; x < Chunk.CHUNK_SECTION_SIZE; x++) {
                    for (int z = 0; z < Chunk.CHUNK_SECTION_SIZE; z++) {
                        int absX = sectionX * Chunk.CHUNK_SECTION_SIZE + x + offset.blockX();
                        int absZ = sectionZ * Chunk.CHUNK_SECTION_SIZE + z + offset.blockZ();

                        if (noise2D.test().test(absX, 0, absZ)) {
                            Vec spreadOffset = new Vec(absX, offset.y(), absZ);
                            for (Instruction opInstruction : noise2D.operation().instructions()) {
                                applyInstruction(sectionX, sectionY, sectionZ, palette, spreadOffset, opInstruction);
                            }
                        }
                    }
                }
            }
            case Instruction.Cuboid cuboid -> {
                for (int x = cuboid.min().blockX(); x < cuboid.max().blockX(); x++) {
                    for (int y = cuboid.min().blockY(); y < cuboid.max().blockY(); y++) {
                        for (int z = cuboid.min().blockZ(); z < cuboid.max().blockZ(); z++) {
                            Block block = cuboid.blockProvider().get(x, y, z);

                            final int localX = ChunkUtils.toSectionRelativeCoordinate(x);
                            final int localY = ChunkUtils.toSectionRelativeCoordinate(y);
                            final int localZ = ChunkUtils.toSectionRelativeCoordinate(z);

                            palette.set(localX, localY, localZ, block.stateId());
                        }
                    }
                }
            }
        }
    }

    static Palette sectionAt(List<Instruction> instructions, int sectionX, int sectionY, int sectionZ) {
        Palette palette = Palette.blocks();
        for (Instruction instruction : instructions) {
            applyInstruction(sectionX, sectionY, sectionZ, palette, Vec.ZERO, instruction);
        }
        return palette;
    }

    static boolean sectionRelevant(Instruction instruction, int sectionX, int sectionY, int sectionZ, Point offset) {
        return switch (instruction) {
            case Instruction.SetBlock setBlock -> ChunkUtils.getChunkCoordinate(setBlock.x() + offset.blockX()) == sectionX &&
                    ChunkUtils.getChunkCoordinate(setBlock.y() + offset.blockY()) == sectionY &&
                    ChunkUtils.getChunkCoordinate(setBlock.z() + offset.blockZ()) == sectionZ;
            case Instruction.Cuboid cuboid -> {
                final Vec min = cuboid.min();
                final Vec max = cuboid.max();

                final int minX = ChunkUtils.getChunkCoordinate(min.blockX() + offset.blockX());
                final int minY = ChunkUtils.getChunkCoordinate(min.blockY() + offset.blockY());
                final int minZ = ChunkUtils.getChunkCoordinate(min.blockZ() + offset.blockZ());

                final int maxX = ChunkUtils.getChunkCoordinate(max.blockX() + offset.blockX());
                final int maxY = ChunkUtils.getChunkCoordinate(max.blockY() + offset.blockY());
                final int maxZ = ChunkUtils.getChunkCoordinate(max.blockZ() + offset.blockZ());

                yield sectionX >= minX && sectionX <= maxX &&
                        sectionY >= minY && sectionY <= maxY &&
                        sectionZ >= minZ && sectionZ <= maxZ;
            }
            case Instruction.Operation2d noise2D -> true;
        };
    }
}
