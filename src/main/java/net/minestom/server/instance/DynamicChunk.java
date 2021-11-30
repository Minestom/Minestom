package net.minestom.server.instance;

import com.extollit.gaming.ai.path.model.ColumnarOcclusionFieldList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.pathfinding.PFBlock;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.network.packet.server.play.UpdateLightPacket;
import net.minestom.server.network.packet.server.play.data.ChunkData;
import net.minestom.server.network.packet.server.play.data.LightData;
import net.minestom.server.utils.ArrayUtils;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.Utils;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.*;

/**
 * Represents a {@link Chunk} which store each individual block in memory.
 * <p>
 * WARNING: not thread-safe.
 */
public class DynamicChunk extends Chunk {

    private final int minSection, maxSection;
    private final Section[] sections;

    // Key = ChunkUtils#getBlockIndex
    protected final Int2ObjectOpenHashMap<Block> entries = new Int2ObjectOpenHashMap<>(0);
    protected final Int2ObjectOpenHashMap<Block> tickableMap = new Int2ObjectOpenHashMap<>(0);

    private long lastChange;
    private final CachedPacket chunkCache = new CachedPacket(this::createChunkPacket);
    private final CachedPacket lightCache = new CachedPacket(this::createLightPacket);

    public DynamicChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
        super(instance, chunkX, chunkZ, true);
        this.minSection = instance.getDimensionType().getMinY() / CHUNK_SECTION_SIZE;
        this.maxSection = (instance.getDimensionType().getMinY() + instance.getDimensionType().getHeight()) / CHUNK_SECTION_SIZE;
        this.sections = new Section[maxSection - minSection];
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        this.lastChange = System.currentTimeMillis();
        this.chunkCache.invalidate();
        this.lightCache.invalidate();
        // Update pathfinder
        if (columnarSpace != null) {
            final ColumnarOcclusionFieldList columnarOcclusionFieldList = columnarSpace.occlusionFields();
            final var blockDescription = PFBlock.get(block);
            columnarOcclusionFieldList.onBlockChanged(x, y, z, blockDescription, 0);
        }
        Section section = getSectionAt(y);
        section.blockPalette()
                .set(toChunkRelativeCoordinate(x), y, toChunkRelativeCoordinate(z), block.stateId());

        final int index = ChunkUtils.getBlockIndex(x, y, z);
        // Handler
        final BlockHandler handler = block.handler();
        if (handler != null || block.hasNbt() || block.registry().isBlockEntity()) {
            this.entries.put(index, block);
        } else {
            this.entries.remove(index);
        }
        // Block tick
        if (handler != null && handler.isTickable()) {
            this.tickableMap.put(index, block);
        } else {
            this.tickableMap.remove(index);
        }
    }

    @Override
    public void setBiome(int x, int y, int z, @NotNull Biome biome) {
        this.chunkCache.invalidate();
        Section section = getSectionAt(y);
        section.biomePalette()
                .set(toChunkRelativeCoordinate(x) / 4, y / 4, toChunkRelativeCoordinate(z) / 4, biome.id());
    }

    @Override
    public @NotNull Section getSection(int section) {
        final int index = section - minSection;
        Section result = sections[index];
        if (result == null) sections[index] = result = new Section();
        return result;
    }

    @Override
    public void tick(long time) {
        if (tickableMap.isEmpty()) return;
        tickableMap.int2ObjectEntrySet().fastForEach(entry -> {
            final int index = entry.getIntKey();
            final Block block = entry.getValue();
            final BlockHandler handler = block.handler();
            if (handler == null) return;
            final Point blockPosition = ChunkUtils.getBlockPosition(index, chunkX, chunkZ);
            handler.tick(new BlockHandler.Tick(block, instance, blockPosition));
        });
    }

    @Override
    public @Nullable Block getBlock(int x, int y, int z, @NotNull Condition condition) {
        // Verify if the block object is present
        if (condition != Condition.TYPE) {
            final Block entry = !entries.isEmpty() ?
                    entries.get(ChunkUtils.getBlockIndex(x, y, z)) : null;
            if (entry != null || condition == Condition.CACHED) {
                return entry;
            }
        }
        // Retrieve the block from state id
        final Section section = sections[ChunkUtils.getSectionAt(y) + minSection];
        if (section == null) return Block.AIR; // Section is unloaded
        final int blockStateId = section.blockPalette()
                .get(toChunkRelativeCoordinate(x), y, toChunkRelativeCoordinate(z));
        if (blockStateId == -1) return Block.AIR; // Section is empty
        return Objects.requireNonNullElse(Block.fromStateId((short) blockStateId), Block.AIR);
    }

    @Override
    public @NotNull Biome getBiome(int x, int y, int z) {
        final Section section = sections[ChunkUtils.getSectionAt(y) + minSection];
        if (section == null) return Biome.PLAINS; // Section is unloaded
        final int id = section.biomePalette()
                .get(toChunkRelativeCoordinate(x) / 4, y / 4, toChunkRelativeCoordinate(z) / 4);
        return MinecraftServer.getBiomeManager().getById(id);
    }

    @Override
    public long getLastChangeTime() {
        return lastChange;
    }

    @Override
    public void sendChunk(@NotNull Player player) {
        if (!isLoaded()) return;
        player.sendPacket(chunkCache);
    }

    @Override
    public void sendChunk() {
        if (!isLoaded()) return;
        if (getViewers().isEmpty()) return;
        sendPacketToViewers(chunkCache);
    }

    @Override
    public @NotNull Chunk copy(@NotNull Instance instance, int chunkX, int chunkZ) {
        DynamicChunk dynamicChunk = new DynamicChunk(instance, chunkX, chunkZ);
        Arrays.setAll(dynamicChunk.sections, value -> {
            final Section s = sections[value];
            return s != null ? s.clone() : null;
        });
        dynamicChunk.entries.putAll(entries);
        return dynamicChunk;
    }

    @Override
    public void reset() {
        for (Section section : sections) {
            if (section != null) section.clear();
        }
        this.entries.clear();
    }

    private synchronized @NotNull ChunkDataPacket createChunkPacket() {
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
            heightmapsNBT = NBT.Compound(nbt -> {
                    nbt.setLongArray("MOTION_BLOCKING", Utils.encodeBlocks(motionBlocking, bitsForHeight));
                    nbt.setLongArray("WORLD_SURFACE", Utils.encodeBlocks(worldSurface, bitsForHeight));
            });
        }
        // Data
        final BinaryWriter writer = new BinaryWriter();
        for (Section section : sections) {
            if (section != null) {
                final Palette blockPalette = section.blockPalette();
                writer.writeShort((short) blockPalette.size());
                blockPalette.write(writer); // Blocks
                section.biomePalette().write(writer); // Biomes
            } else {
                // Hardcode empty section
                writer.writeShort((short) 0); // Block count
                writer.writeByte((byte) 15); // Block bpe
                writer.writeVarInt(0); // Block data length
                writer.writeByte((byte) 15); // Biome bpe
                writer.writeVarInt(0); // Biome data length
            }
        }
        return new ChunkDataPacket(chunkX, chunkZ,
                new ChunkData(heightmapsNBT, writer.toByteArray(), entries),
                createLightData());
    }

    private synchronized @NotNull UpdateLightPacket createLightPacket() {
        return new UpdateLightPacket(chunkX, chunkZ, createLightData());
    }

    private LightData createLightData() {
        BitSet skyMask = new BitSet();
        BitSet blockMask = new BitSet();
        BitSet emptySkyMask = new BitSet();
        BitSet emptyBlockMask = new BitSet();
        List<byte[]> skyLights = new ArrayList<>();
        List<byte[]> blockLights = new ArrayList<>();

        int index = 0;
        for (Section section : sections) {
            index++;
            if (section != null) {
                final byte[] skyLight = section.getSkyLight();
                final byte[] blockLight = section.getBlockLight();
                if (!ArrayUtils.empty(skyLight)) {
                    skyLights.add(skyLight);
                    skyMask.set(index);
                }
                if (!ArrayUtils.empty(blockLight)) {
                    blockLights.add(blockLight);
                    blockMask.set(index);
                }
            } else {
                emptyBlockMask.set(index);
                emptySkyMask.set(index);
            }
        }
        return new LightData(true,
                skyMask, blockMask,
                emptySkyMask, emptyBlockMask,
                skyLights, blockLights);
    }

    private static int toChunkRelativeCoordinate(int xz) {
        xz %= 16;
        if (xz < 0) {
            xz += Chunk.CHUNK_SECTION_SIZE;
        }
        return xz;
    }
}
