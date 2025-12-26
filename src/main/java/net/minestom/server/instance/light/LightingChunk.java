package net.minestom.server.instance.light;

import net.kyori.adventure.key.Key;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.heightmap.Heightmap;
import net.minestom.server.network.packet.server.play.UpdateLightPacket;
import net.minestom.server.network.packet.server.play.data.LightData;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;

public class LightingChunk extends DynamicChunk {
    private static final LightEngine LIGHT_ENGINE = LightEngine.getDefault();
    public static boolean announce = false;
    private final int minLightSection;
    private final int maxLightSection;
    private int highestBlock;
    private int @Nullable [] occlusionMap;
    private final List<LightSection> lightSections;
    private volatile boolean resendLight = false;
    private @Nullable WeakReference<@Nullable LightingChunk> north;
    private @Nullable WeakReference<@Nullable LightingChunk> east;
    private @Nullable WeakReference<@Nullable LightingChunk> south;
    private @Nullable WeakReference<@Nullable LightingChunk> west;
    private volatile boolean neighborUpdated = false;

    private static final Set<Key> DIFFUSE_SKY_LIGHT = Set.of(Block.COBWEB.key(), Block.ICE.key(), Block.HONEY_BLOCK.key(), Block.SLIME_BLOCK.key(), Block.WATER.key(), Block.ACACIA_LEAVES.key(), Block.AZALEA_LEAVES.key(), Block.BIRCH_LEAVES.key(), Block.DARK_OAK_LEAVES.key(), Block.FLOWERING_AZALEA_LEAVES.key(), Block.JUNGLE_LEAVES.key(), Block.CHERRY_LEAVES.key(), Block.OAK_LEAVES.key(), Block.SPRUCE_LEAVES.key(), Block.SPAWNER.key(), Block.BEACON.key(), Block.END_GATEWAY.key(), Block.CHORUS_PLANT.key(), Block.CHORUS_FLOWER.key(), Block.FROSTED_ICE.key(), Block.SEAGRASS.key(), Block.TALL_SEAGRASS.key(), Block.LAVA.key());

    private static boolean checkSkyOcclusion(@Nullable Block block) {
        if (block == Block.AIR || block == null) return false;
        if (DIFFUSE_SKY_LIGHT.contains(block.key())) return true;

        Shape shape = block.registry().occlusionShape();
        boolean occludesTop = Block.AIR.registry().occlusionShape().isOccluded(shape, BlockFace.TOP);
        boolean occludesBottom = Block.AIR.registry().occlusionShape().isOccluded(shape, BlockFace.BOTTOM);

        return occludesBottom || occludesTop;
    }

    protected LightingChunk(Instance instance, int chunkX, int chunkZ, List<Section> sections) {
        super(instance, chunkX, chunkZ, sections);
        this.minLightSection = minSection - 1;
        this.maxLightSection = maxSection + 1;
        this.lightSections = initSections();
    }

    public LightingChunk(Instance instance, int chunkX, int chunkZ) {
        super(instance, chunkX, chunkZ);
        this.minLightSection = minSection - 1;
        this.maxLightSection = maxSection + 1;
        this.lightSections = initSections();
    }

    public int getHighestBlock() {
        assert Thread.holdsLock(this);
        return highestBlock;
    }

    // Lazy compute occlusion map
    public int[] getOcclusionMap() {
        assert Thread.holdsLock(this);
        if (this.occlusionMap != null) return this.occlusionMap;
        var occlusionMap = new int[CHUNK_SIZE_X * CHUNK_SIZE_Z];

        int minY = instance.getCachedDimensionType().minY();
        highestBlock = minY - 1;

        synchronized (this) {
            int startY = Heightmap.getHighestBlockSection(this);

            for (int x = 0; x < CHUNK_SIZE_X; x++) {
                for (int z = 0; z < CHUNK_SIZE_Z; z++) {
                    int height = startY;
                    while (height >= minY) {
                        Block block = getBlock(x, height, z, Condition.TYPE);
                        if (block != Block.AIR) highestBlock = Math.max(highestBlock, height);
                        if (checkSkyOcclusion(block)) break;
                        height--;
                    }
                    occlusionMap[z << 4 | x] = (height + 1);
                }
            }
        }

        this.occlusionMap = occlusionMap;
        return occlusionMap;
    }

    /**
     * Gets a snapshot of all neighboring chunks. Chunks will be null if not found.
     * Chunks may even be found if they are not loaded (yet or anymore).
     * <p>
     * This will be called by lighting code quite a few times. Do not try to pass this as an
     * argument to the lighting code, this will create a race condition.
     * General rule is: All computation data must be fetched after a new version ID has been acquired.
     * Passing a NeighborSnapshot would imply fetching the snapshot before the version ID, which is illegal.
     */
    public NeighborSnapshot createNeighborSnapshot() {
        return new NeighborSnapshot(north == null ? null : north.get(), east == null ? null : east.get(), south == null ? null : south.get(), west == null ? null : west.get());
    }

    @Override
    public void setBlock(int x, int y, int z, Block block, BlockHandler.@Nullable Placement placement, BlockHandler.@Nullable Destroy destroy) {
        assert Thread.holdsLock(this);
        super.setBlock(x, y, z, block, placement, destroy);
        occlusionMap = null;
        var sectionY = CoordConversion.globalToSection(y);
        // TODO change this to a timer in case of many block changes
        announce = true;
        getLightSection(sectionY).blockChanged();
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        // We need to announce to our neighbors that we are loaded now.
        // This connects us with the neighbors, and invalidates their external lighting for us.
        // This connection stays up until chunks are unloaded.
        // onLoad is an ideal place because of its threading implications
        announceNeighborLoad(BlockFace.NORTH);
        announceNeighborLoad(BlockFace.EAST);
        announceNeighborLoad(BlockFace.SOUTH);
        announceNeighborLoad(BlockFace.WEST);
    }

    public void announceNeighborLoad(BlockFace directionFace) {
        var neighbor = this.instance.getChunkManager().getLoadedChunkManaged(chunkX + directionFace.toDirection().normalX(), chunkZ + directionFace.toDirection().normalZ());
        if (neighbor == null) return; // No neighbor
        if (neighbor instanceof LightingChunk lightingChunk) {
            lightingChunk.receiveNeighborLoad(this, directionFace.getOppositeFace());
            receiveNeighborLoad(lightingChunk, directionFace);
        }
    }

    private void receiveNeighborLoad(LightingChunk neighbor, BlockFace originDirection) {
        switch (originDirection) {
            case NORTH -> north = new WeakReference<>(neighbor);
            case EAST -> east = new WeakReference<>(neighbor);
            case SOUTH -> south = new WeakReference<>(neighbor);
            case WEST -> west = new WeakReference<>(neighbor);
            default -> {
                return;
            }
        }
        // Neighbor has updated. We now invalidate the external lighting
        // We want to keep work on chunk management thread to a minimum, so we delegate via flag
        neighborUpdated = true;
    }

    @Override
    protected LightData createLightData(boolean requiredFullChunk) {
        BitSet skyMask = new BitSet();
        BitSet blockMask = new BitSet();
        BitSet emptySkyMask = new BitSet();
        BitSet emptyBlockMask = new BitSet();
        List<byte[]> skyLights = new ArrayList<>();
        List<byte[]> blockLights = new ArrayList<>();

        for (var i = 0; i < lightSections.size(); i++) {
            var section = lightSections.get(i);
            // Add block and skylight to light data
            addLight(blockMask, emptyBlockMask, blockLights, i, section.getBlockLight().data());
            addLight(skyMask, emptySkyMask, skyLights, i, section.getSkyLight().data());
        }

        return new LightData(skyMask, blockMask, emptySkyMask, emptyBlockMask, skyLights, blockLights);
    }

    private void addLight(BitSet mask, BitSet emptyMask, List<byte[]> list, int index, byte[] data) {
        if (data == LightCompute.EMPTY_CONTENT) {
            emptyMask.set(index);
        } else {
            mask.set(index);
            list.add(data);
        }
    }

    @Override
    public void tick0(long time) {
        assert Thread.holdsLock(this);
        super.tick0(time);

        if (resendLight) {
            resendLight = false;
            sendLight();
        }
        if (neighborUpdated) {
            neighborUpdated = false;
            updateExternalLighting();
        }
    }

    private void updateExternalLighting() {
        for (var lightSection : lightSections) {
            lightSection.relightExternalBlockLightAsync();
        }
    }

    @Override
    public void onGenerate() {
        super.onGenerate();
        for (var lightSection : lightSections) {
            lightSection.generatorRelightBlockLightInternal();
        }
        for (var lightSection : lightSections) {
            lightSection.generatorRelightBlockLightExternal();
            lightSection.bakeBlockLight();
        }
    }

    public void scheduleResend() {
        resendLight = true;
    }

    public void sendLight() {
        sendPacketToViewers(new UpdateLightPacket(chunkX, chunkZ, createLightData(true)));
    }

    private List<LightSection> initSections() {
        var sectionsTemp = new LightSection[maxLightSection - minLightSection];
        for (var i = 0; i < sectionsTemp.length; i++) {
            var first = i == 0;
            var last = i == maxLightSection - minLightSection - 1;
            var section = first || last ? null : getSection(i + minLightSection);
            sectionsTemp[i] = new LightSection(LIGHT_ENGINE, this, section, i + minLightSection);
            if (!first) {
                sectionsTemp[i].down = sectionsTemp[i - 1];
                sectionsTemp[i - 1].up = sectionsTemp[i];
            }
        }
        return List.of(sectionsTemp);
    }

    public LightSection getLightSection(int sectionY) {
        return lightSections.get(sectionY - minLightSection);
    }

    public record NeighborSnapshot(@Nullable LightingChunk north, @Nullable LightingChunk east,
                                   @Nullable LightingChunk south, @Nullable LightingChunk west) {
    }
}
