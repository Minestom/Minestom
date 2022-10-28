package net.minestom.server.instance;

import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.entity.pathfinding.PFColumnarSpace;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.network.packet.server.play.data.ChunkData;
import net.minestom.server.network.packet.server.play.data.LightData;
import net.minestom.server.snapshot.Snapshotable;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.ObjectPool;
import net.minestom.server.utils.Utils;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

// TODO light data & API

/**
 * A chunk is a view into an {@link Instance}, limited by a size of 16xNx16 blocks and subdivided in N sections of 16 blocks height.
 * Should contain all the blocks located at those positions and manage their tick updates.
 * Be aware that implementations do not need to be thread-safe, all chunks are guarded by their own chunk ('this').
 * <p>
 * You generally want to avoid storing references of this object as this could lead to a huge memory leak,
 * you should store the chunk coordinates instead.
 */
public interface Chunk extends Block.Getter, Block.Setter, Biome.Getter, Biome.Setter, Snapshotable {
    int SIZE_X = 16;
    int SIZE_Z = 16;

    @Deprecated int CHUNK_SIZE_X = SIZE_X;
    @Deprecated int CHUNK_SIZE_Z = SIZE_Z;
    @Deprecated int CHUNK_SECTION_SIZE = Section.SIZE_Y;

    static @NotNull Chunk inMemory() {
        return new InMemoryChunk();
    }

    static @NotNull Chunk viewInto(Instance instance, int chunkX, int chunkZ) {
        return new InstanceWindowChunk(instance, chunkX, chunkZ);
    }

    /**
     * Sets a block at a position.
     * <p>
     * This is used when the previous block has to be destroyed/replaced, meaning that it clears the previous data and update method.
     * <p>
     * WARNING: this method is not thread-safe (in order to bring performance improvement with {@link net.minestom.server.instance.batch.Batch batches})
     * The thread-safe version is {@link Instance#setBlock(int, int, int, Block)} (or any similar chunk methods)
     * Otherwise, you can simply do not forget to have this chunk synchronized when this is called.
     *
     * @param x     the block X
     * @param y     the block Y
     * @param z     the block Z
     * @param block the block to place
     */
    @Override
    void setBlock(int x, int y, int z, @NotNull Block block);

    @Nullable Section getSection(int section);

    default @Nullable Section getSectionAt(int blockY) {
        return getSection(ChunkUtils.getChunkCoordinate(blockY));
    }

    /**
     * Resets the chunk, this means clearing all the data making it empty.
     */
    void reset();

    /**
     * Gets the unique identifier of this chunk.
     * <p>
     * WARNING: this UUID is not persistent but randomized once the object is instantiated.
     *
     * @return the chunk identifier
     */
    default @NotNull UUID getIdentifier() {
        // use object hashcode as UUID
        return new UUID(0, hashCode());
    }

    /**
     * Gets the lowest (inclusive) section Y available in this chunk
     *
     * @return the lowest (inclusive) section Y available in this chunk
     */
    int getMinSection();

    /**
     * Gets the highest (exclusive) section Y available in this chunk
     *
     * @return the highest (exclusive) section Y available in this chunk
     */
    int getMaxSection();

    /**
     * Gets if this chunk will or had been loaded with a {@link ChunkGenerator}.
     * <p>
     * If false, the chunk will be entirely empty when loaded.
     *
     * @return true if this chunk is affected by a {@link ChunkGenerator}
     */
    default boolean shouldGenerate() {
        return true;
    }

    /**
     * Gets if this chunk is read-only.
     * <p>
     * Being read-only should prevent block placing/breaking and setting block from an {@link Instance}.
     * It does not affect {@link IChunkLoader} and {@link ChunkGenerator}.
     *
     * @return true if the chunk is read-only
     */
    boolean isReadOnly();

    /**
     * Changes the read state of the chunk.
     * <p>
     * Being read-only should prevent block placing/breaking and setting block from an {@link Instance}.
     * It does not affect {@link IChunkLoader} and {@link ChunkGenerator}.
     *
     * @param readOnly true to make the chunk read-only, false otherwise
     */
    void setReadOnly(boolean readOnly);

    /**
     * Changes this chunk columnar space.
     *
     * @param columnarSpace the new columnar space
     */
    void setColumnarSpace(PFColumnarSpace columnarSpace);

    /**
     * Used to verify if the chunk should still be kept in memory.
     *
     * @return true if the chunk is loaded
     */
    boolean isLoaded();

    /**
     * Sets the chunk as "unloaded".
     */
    @ApiStatus.Internal
    CompletableFuture<Void> unload();

    default ChunkDataPacket chunkPacket(int chunkX, int chunkZ) {
        return createChunkDataPacket(chunkX, chunkZ);
    }
    
    // lighting
    /**
     * Gets the skylight at the given section.
     * @param sectionY the section Y
     * @return the skylight byte array, null if the section is not loaded
     */
    ByteList getSkyLight(int sectionY);

    /**
     * Gets the blocklight at the given section.
     * @param sectionY the section Y
     * @return the blocklight byte array, null if the section is not loaded
     */
    ByteList getBlockLight(int sectionY);

    /**
     * Sets the skylight at the given section.
     * @param sectionY the section Y
     * @param light the skylight byte array
     * @throws IllegalStateException if the section is not loaded
     */
    void setSkyLight(int sectionY, ByteList light);

    /**
     * Sets the skylight at the given section.
     * @param sectionY the section Y
     * @param light the blocklight byte array
     */
    default void setSkyLight(int sectionY, byte[] light) {
        setSkyLight(sectionY, ByteList.of(light));
    }

    /**
     * Sets the blocklight at the given section.
     * @param sectionY the section Y
     * @param light the blocklight byte array
     * @throws IllegalStateException if the section is not loaded
     */
    void setBlockLight(int sectionY, ByteList light);

    /**
     * Sets the blocklight at the given section.
     * @param sectionY the section Y
     * @param light the blocklight byte array
     */
    default void setBlockLight(int sectionY, byte[] light) {
        setBlockLight(sectionY, ByteList.of(light));
    }

    private ChunkDataPacket createChunkDataPacket(int chunkX, int chunkZ) {
        final NBTCompound heightmapsNBT;
        // TODO: don't hardcode heightmaps
        // Heightmap
        {
            int dimensionHeight = (getMaxSection() - getMinSection()) * Section.SIZE_Y;
            int[] motionBlocking = new int[16 * 16];
            int[] worldSurface = new int[16 * 16];
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    motionBlocking[x + z * 16] = 0;
                    worldSurface[x + z * 16] = dimensionHeight - 1;
                }
            }
            final int bitsForHeight = MathUtils.bitsToRepresent(dimensionHeight);
            heightmapsNBT = NBT.Compound(Map.of(
                    "MOTION_BLOCKING", NBT.LongArray(Utils.encodeBlocks(motionBlocking, bitsForHeight)),
                    "WORLD_SURFACE", NBT.LongArray(Utils.encodeBlocks(worldSurface, bitsForHeight))));
        }
        // Data
        final byte[] data = ObjectPool.PACKET_POOL.use(buffer -> {
            final BinaryWriter writer = new BinaryWriter(buffer);
            IntStream.range(getMinSection(), getMaxSection())
                    .mapToObj(this::getSection)
                    .peek(Objects::requireNonNull)
                    .map(PaletteSectionData::new)
                    .forEach(sectionData -> sectionData.write(writer));
            return writer.toByteArray();
        });
        // Block entities
        Int2ObjectOpenHashMap<Block> entries = new Int2ObjectOpenHashMap<>();
        for (int sectionY = getMinSection(); sectionY < getMaxSection(); sectionY++) {
            int yOffset = sectionY * Section.SIZE_Y;
            Section section = getSection(sectionY);
            if (section == null) throw new IllegalStateException("Section " + sectionY + " is not loaded");
            section.forEachBlock((x, y, z, block) -> {
                var handler = block.handler();
                if (handler != null || block.hasNbt() || block.registry().isBlockEntity()) {
                    entries.put(ChunkUtils.getBlockIndex(x, y + yOffset, z), block);
                }
            });
        }
        return new ChunkDataPacket(chunkX, chunkZ,
                new ChunkData(heightmapsNBT, data, entries),
                createLightData());
    }

    private LightData createLightData() {
        BitSet skyMask = new BitSet();
        BitSet blockMask = new BitSet();
        BitSet emptySkyMask = new BitSet();
        BitSet emptyBlockMask = new BitSet();
        List<byte[]> skyLights = new ArrayList<>();
        List<byte[]> blockLights = new ArrayList<>();

        int index = 0;
        for (int sectionY = getMinSection(); sectionY < getMaxSection(); sectionY++) {
            Section section = getSection(sectionY);
            Objects.requireNonNull(section);
            index++;
            final byte[] skyLight = section.getSkyLight().toByteArray();
            final byte[] blockLight = section.getBlockLight().toByteArray();
            if (skyLight.length != 0) {
                skyLights.add(skyLight);
                skyMask.set(index);
            } else {
                emptySkyMask.set(index);
            }
            if (blockLight.length != 0) {
                blockLights.add(blockLight);
                blockMask.set(index);
            } else {
                emptyBlockMask.set(index);
            }
        }
        return new LightData(true,
                skyMask, blockMask,
                emptySkyMask, emptyBlockMask,
                skyLights, blockLights);
    }
}