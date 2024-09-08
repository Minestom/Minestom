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

        record Operation2d(PosPredicate predicate, PreparedOperation operation) implements Instruction {
        }

        record Heightmap(HeightProvider heightProvider, PreparedOperation operation) implements Instruction {
        }

        record Cuboid(Vec min, Vec max, Block block) implements Instruction {
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
        public void cuboid(Point min, Point max, Block block) {
            append(new Instruction.Cuboid(Vec.fromPoint(min), Vec.fromPoint(max), block));
        }

        @Override
        public void operation2d(PosPredicate noise, Operation operation) {
            PreparedOperation prepared = PreparedOperation.compile(operation);
            if (prepared == null) return;
            append(new Instruction.Operation2d(noise, prepared));
        }

        @Override
        public void heightmap(HeightProvider heightProvider, Operation operation) {
            PreparedOperation prepared = PreparedOperation.compile(operation);
            if (prepared == null) return;
            append(new Instruction.Heightmap(heightProvider, prepared));
        }
    }

    static void applyInstruction(int sectionX, int sectionY, int sectionZ, Palette palette, Point offset, Instruction instruction) {
        if (!sectionRelevant(instruction, sectionX, sectionY, sectionZ, offset)) return;

        int minSectionX = sectionX * Chunk.CHUNK_SECTION_SIZE;
        int maxSectionX = minSectionX + Chunk.CHUNK_SECTION_SIZE;
        int minSectionY = sectionY * Chunk.CHUNK_SECTION_SIZE;
        int maxSectionY = minSectionY + Chunk.CHUNK_SECTION_SIZE;
        int minSectionZ = sectionZ * Chunk.CHUNK_SECTION_SIZE;
        int maxSectionZ = minSectionZ + Chunk.CHUNK_SECTION_SIZE;

        switch (instruction) {
            case Instruction.SetBlock setBlock -> {
                final int absX = setBlock.x() + offset.blockX();
                final int absY = setBlock.y() + offset.blockY();
                final int absZ = setBlock.z() + offset.blockZ();
                if (absX < minSectionX || absX >= maxSectionX ||
                        absY < minSectionY || absY >= maxSectionY ||
                        absZ < minSectionZ || absZ >= maxSectionZ) return;
                final int localX = ChunkUtils.toSectionRelativeCoordinate(absX);
                final int localY = ChunkUtils.toSectionRelativeCoordinate(absY);
                final int localZ = ChunkUtils.toSectionRelativeCoordinate(absZ);
                palette.set(localX, localY, localZ, setBlock.block().stateId());
            }
            case Instruction.Cuboid cuboid -> {
                for (int x = cuboid.min().blockX(); x < cuboid.max().blockX(); x++) {
                    for (int y = cuboid.min().blockY(); y < cuboid.max().blockY(); y++) {
                        for (int z = cuboid.min().blockZ(); z < cuboid.max().blockZ(); z++) {
                            Block block = cuboid.block();

                            if (x < minSectionX || x >= maxSectionX ||
                                    y < minSectionY || y >= maxSectionY ||
                                    z < minSectionZ || z >= maxSectionZ) continue;

                            final int localX = ChunkUtils.toSectionRelativeCoordinate(x);
                            final int localY = ChunkUtils.toSectionRelativeCoordinate(y);
                            final int localZ = ChunkUtils.toSectionRelativeCoordinate(z);

                            palette.set(localX, localY, localZ, block.stateId());
                        }
                    }
                }
            }
            case Instruction.Operation2d noise2D -> {
                Bounds bounds = noise2D.operation().bounds();

                int maxX = bounds.max().blockX() + Chunk.CHUNK_SECTION_SIZE;
                int minX = bounds.min().blockX() - Chunk.CHUNK_SECTION_SIZE;

                int maxZ = bounds.max().blockZ() + Chunk.CHUNK_SECTION_SIZE;
                int minZ = bounds.min().blockZ() - Chunk.CHUNK_SECTION_SIZE;

                // directions to start/end the loop
                int negX = -Math.max(maxX, 0);
                int negZ = -Math.max(maxZ, 0);
                int posX = Math.max(0, -minX);
                int posZ = Math.max(0, -minZ);

                for (int x = negX; x < posX; x++) {
                    for (int z = negZ; z < posZ; z++) {
                        int absX = x + Chunk.CHUNK_SECTION_SIZE * sectionX + offset.blockX();
                        int absZ = z + Chunk.CHUNK_SECTION_SIZE * sectionZ + offset.blockZ();

                        if (!noise2D.predicate().test(absX, 0, absZ)) continue;

                        Vec spreadOffset = new Vec(absX, offset.y(), absZ);
                        for (Instruction opInstruction : noise2D.operation().instructions()) {
                            applyInstruction(sectionX, sectionY, sectionZ, palette, spreadOffset, opInstruction);
                        }
                    }
                }
            }
            case Instruction.Heightmap heightmap -> {
                Bounds bounds = heightmap.operation().bounds();

                int maxX = bounds.max().blockX() + Chunk.CHUNK_SECTION_SIZE;
                int minX = bounds.min().blockX() - Chunk.CHUNK_SECTION_SIZE;

                int maxZ = bounds.max().blockZ() + Chunk.CHUNK_SECTION_SIZE;
                int minZ = bounds.min().blockZ() - Chunk.CHUNK_SECTION_SIZE;

                // directions to start/end the loop
                int negX = -Math.max(maxX, 0);
                int negZ = -Math.max(maxZ, 0);
                int posX = Math.max(0, -minX);
                int posZ = Math.max(0, -minZ);

                for (int x = negX; x < posX; x++) {
                    for (int z = negZ; z < posZ; z++) {
                        int absX = x + Chunk.CHUNK_SECTION_SIZE * sectionX + offset.blockX();
                        int absZ = z + Chunk.CHUNK_SECTION_SIZE * sectionZ + offset.blockZ();

                        Integer height = heightmap.heightProvider().test(absX, absZ);
                        if (height == null) continue;

                        Vec spreadOffset = new Vec(absX, height + offset.y(), absZ);
                        for (Instruction opInstruction : heightmap.operation().instructions()) {
                            applyInstruction(sectionX, sectionY, sectionZ, palette, spreadOffset, opInstruction);
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
            case Instruction.Heightmap heightmap -> true;
        };
    }
}
