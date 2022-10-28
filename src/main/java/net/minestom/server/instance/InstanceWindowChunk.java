package net.minestom.server.instance;

import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.network.packet.server.play.UpdateLightPacket;
import net.minestom.server.network.packet.server.play.data.ChunkData;
import net.minestom.server.network.packet.server.play.data.LightData;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.ObjectPool;
import net.minestom.server.utils.Utils;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

public class InstanceWindowChunk extends ChunkBase {
    public InstanceWindowChunk(Instance instance, int chunkX, int chunkZ) {
        super(instance, chunkX, chunkZ, false);
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        x = ChunkUtils.getChunkCoordinate(x);
        z = ChunkUtils.getChunkCoordinate(z);
        x += chunkX * Chunk.SIZE_X;
        z += chunkZ * Chunk.SIZE_Z;
        instance.setBlock(x, y, z, block);
    }

    @Override
    public @Nullable Section getSection(int section) {
        return instance.getSection(chunkX, section, chunkZ);
    }

    @Override
    public CompletableFuture<Void> unload() {
        return instance.unloadChunk(chunkX, chunkZ);
    }

    @Override
    public ByteList getSkyLight(int sectionY) {
        Section section = getSection(sectionY);
        if (section == null) throw new IllegalArgumentException("Section " + sectionY + " is not loaded");
        return section.getSkyLight();
    }

    @Override
    public ByteList getBlockLight(int sectionY) {
        Section section = getSection(sectionY);
        if (section == null) throw new IllegalArgumentException("Section " + sectionY + " is not loaded");
        return section.getBlockLight();
    }

    @Override
    public void setSkyLight(int sectionY, ByteList light) {
        Section section = getSection(sectionY);
        if (section == null) throw new IllegalArgumentException("Section " + sectionY + " is not loaded");
        section.setSkyLight(light);
    }

    @Override
    public void setBlockLight(int sectionY, ByteList light) {
        Section section = getSection(sectionY);
        if (section == null) throw new IllegalArgumentException("Section " + sectionY + " is not loaded");
        section.setBlockLight(light);
    }

    @Override
    public void tick(long time) {
    }

    @Override
    public void reset() {
        IntStream.range(getMinSection(), getMaxSection())
                .mapToObj(this::getSection)
                .filter(Objects::nonNull)
                .forEach(Section::clear);
    }

    @Override
    public @UnknownNullability Block getBlock(int x, int y, int z, @NotNull Condition condition) {
        Section section = getSectionAt(y);
        if (section == null) return null;
        return section.getBlock(x, y, z, condition);
    }

    @Override
    public void setBiome(int x, int y, int z, @NotNull Biome biome) {
        Section section = getSectionAt(y);
        if (section == null) throw new IllegalStateException("Cannot set biome of unloaded section");
        section.setBiome(x, y, z, biome);
    }

    @Override
    public @NotNull Biome getBiome(int x, int y, int z) {
        Section section = getSectionAt(y);
        if (section == null) throw new IllegalStateException("Cannot get biome of unloaded section");
        return section.getBiome(x, y, z);
    }

    private synchronized @NotNull ChunkDataPacket createChunkPacket() {
        instance.loadChunk(chunkX, chunkZ).join();
        final NBTCompound heightmapsNBT;
        // TODO: don't hardcode heightmaps
        // Heightmap
        {
            int dimensionHeight = getInstance().getDimensionType().getHeight();
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
                    .map(Objects::requireNonNull)
                    .map(PaletteSectionData::new)
                    .forEach(writer::write);
            return writer.toByteArray();
        });

        // Block entities
        Int2ObjectMap<Block> entries = new Int2ObjectOpenHashMap<>();
        for (int sectionY = getMinSection(); sectionY < getMaxSection(); sectionY++) {
            final Section section = getSection(sectionY);
            Objects.requireNonNull(section);
            int yOffset = sectionY * Section.SIZE_Y;
            section.forEachBlock((x, y, z, block) -> {
                int chunkBlockIndex = ChunkUtils.getBlockIndex(x, y + yOffset, z);
                entries.put(chunkBlockIndex, block);
            });
        }
        return new ChunkDataPacket(chunkX, chunkZ,
                new ChunkData(heightmapsNBT, data, entries),
                createLightData());
    }

    private synchronized @NotNull UpdateLightPacket createLightPacket() {
        return new UpdateLightPacket(chunkX, chunkZ, createLightData());
    }

    private LightData createLightData() {
        instance.loadChunk(chunkX, chunkZ).join();
        BitSet skyMask = new BitSet();
        BitSet blockMask = new BitSet();
        BitSet emptySkyMask = new BitSet();
        BitSet emptyBlockMask = new BitSet();
        List<byte[]> skyLights = new ArrayList<>();
        List<byte[]> blockLights = new ArrayList<>();
        int index = 0;
        for (int sectionY = getMinSection(); sectionY < getMaxSection(); sectionY++) {
            final Section section = getSection(sectionY);
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
