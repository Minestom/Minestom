package net.minestom.server.instance;

import com.extollit.gaming.ai.path.model.IColumnarSpace;
import it.unimi.dsi.fastutil.bytes.ByteList;
import net.minestom.server.MinecraftServer;
import net.minestom.server.Tickable;
import net.minestom.server.Viewable;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.pathfinding.PFInstanceSpace;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.snapshot.ChunkSnapshot;
import net.minestom.server.snapshot.InstanceSnapshot;
import net.minestom.server.snapshot.SnapshotImpl;
import net.minestom.server.snapshot.SnapshotUpdater;
import net.minestom.server.thread.ThreadDispatcher;
import net.minestom.server.utils.ArrayUtils;
import net.minestom.server.utils.chunk.ChunkCallback;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * The {@link SharedInstance} is an instance that shares the same chunks as its linked {@link InstanceContainer},
 * entities are separated.
 */
public class SharedInstance extends InstanceBase {
    private final InstanceContainer instanceContainer;

    public SharedInstance(@NotNull UUID uniqueId, @NotNull InstanceContainer instanceContainer) {
        super(uniqueId, instanceContainer.getDimensionType());
        this.instanceContainer = instanceContainer;
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        this.instanceContainer.setBlock(x, y, z, block);
    }

    @Override
    public boolean placeBlock(@NotNull BlockHandler.Placement placement) {
        return instanceContainer.placeBlock(placement);
    }

    @Override
    public boolean breakBlock(@NotNull Player player, @NotNull Point blockPosition) {
        return instanceContainer.breakBlock(player, blockPosition);
    }

    public @NotNull CompletableFuture<Void> loadChunk(int chunkX, int chunkZ) {
        return instanceContainer.loadChunk(chunkX, chunkZ);
    }

    public void sendChunk(Player player, int chunkX, int chunkZ) {
        player.sendPacket(instanceContainer.chunkPacket(chunkX, chunkZ));
    }

    @Override
    public void refreshCurrentChunk(Tickable tickable, int newChunkX, int newChunkZ) {
        instanceContainer.refreshCurrentChunk(tickable, newChunkX, newChunkZ);
    }

    @Override
    public boolean isChunkLoaded(long currentChunk) {
        return instanceContainer.isChunkLoaded(currentChunk);
    }

    @Override
    public boolean isChunkLoaded(Point blockPosition) {
        return instanceContainer.isChunkLoaded(blockPosition);
    }

    @Override
    public void registerDispatcher(ThreadDispatcher<Chunk> dispatcher) {
        instanceContainer.registerDispatcher(dispatcher);
    }

    @Override
    public IColumnarSpace createColumnarSpace(PFInstanceSpace instanceSpace, int cx, int cz) {
        return instanceContainer.createColumnarSpace(instanceSpace, cx, cz);
    }

    @Override
    public ByteList getSkyLight(int chunkX, int sectionY, int chunkZ) {
        return instanceContainer.getSkyLight(chunkX, sectionY, chunkZ);
    }

    @Override
    public ByteList getBlockLight(int chunkX, int sectionY, int chunkZ) {
        return instanceContainer.getBlockLight(chunkX, sectionY, chunkZ);
    }

    @Override
    public void setSkyLight(int chunkX, int sectionY, int chunkZ, ByteList light) {
        instanceContainer.setSkyLight(chunkX, sectionY, chunkZ, light);
    }

    @Override
    public void setBlockLight(int chunkX, int sectionY, int chunkZ, ByteList light) {
        instanceContainer.setBlockLight(chunkX, sectionY, chunkZ, light);
    }

    @Override
    public void clearSection(int chunkX, int sectionY, int chunkZ) {
        instanceContainer.clearSection(chunkX, sectionY, chunkZ);
    }

    @Override
    public boolean isSectionLoaded(int chunkX, int sectionY, int chunkZ) {
        return instanceContainer.isSectionLoaded(chunkX, sectionY, chunkZ);
    }

    public void unloadChunk(@NotNull Chunk chunk) {
        instanceContainer.unloadChunk(chunk);
    }

    public Chunk getChunk(int chunkX, int chunkZ) {
        return instanceContainer.getChunk(chunkX, chunkZ);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveInstance() {
        return instanceContainer.saveInstance();
    }

    public @NotNull CompletableFuture<Void> saveChunkToStorage(@NotNull Chunk chunk) {
        return instanceContainer.saveChunkToStorage(chunk);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveChunksToStorage() {
        return instanceContainer.saveChunksToStorage();
    }

    @Override
    public @Nullable Generator generator() {
        return instanceContainer.generator();
    }

    @Override
    public void setGenerator(@Nullable Generator generator) {
        instanceContainer.setGenerator(generator);
    }

    @NotNull
    public Collection<Chunk> getChunks() {
        return instanceContainer.getChunks();
    }

    @Override
    public void enableAutoChunkLoad(boolean enable) {
        instanceContainer.enableAutoChunkLoad(enable);
    }

    @Override
    public boolean hasEnabledAutoChunkLoad() {
        return instanceContainer.hasEnabledAutoChunkLoad();
    }

    @Override
    public boolean isInVoid(@NotNull Point point) {
        return instanceContainer.isInVoid(point);
    }

    @Override
    public @NotNull Set<@NotNull Entity> getChunkEntities(long chunk) {
        int chunkX = ChunkUtils.getChunkCoordX(chunk);
        int chunkZ = ChunkUtils.getChunkCoordZ(chunk);
        return Set.copyOf(getEntityTracker().chunkEntities(chunkX, chunkZ, EntityTracker.Target.ENTITIES));
    }

    @Override
    public @Nullable Block retrieveBlock(int x, int y, int z, @NotNull Condition condition) {
        return instanceContainer.retrieveBlock(x, y, z, condition);
    }

    @Override
    public @NotNull Viewable getViewersAt(int x, int y, int z) {
        // TODO: Find a better solution for this
        double viewDistance = Math.pow(MinecraftServer.getEntityViewDistance() * Chunk.CHUNK_SIZE_X, 2.0);
        return new Viewable() {

            @Override
            public boolean addViewer(@NotNull Player player) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean removeViewer(@NotNull Player player) {
                throw new UnsupportedOperationException();
            }

            @Override
            public @NotNull Set<@NotNull Player> getViewers() {
                return getEntityTracker()
                        .entities(EntityTracker.Target.PLAYERS)
                        .stream()
                        .filter(entity -> entity.getPosition().distanceSquared(x, y, z) < viewDistance)
                        .collect(Collectors.toSet());
            }
        };
    }

    @Override
    public @NotNull InstanceSnapshot updateSnapshot(@NotNull SnapshotUpdater updater) {
        final Map<Long, AtomicReference<ChunkSnapshot>> chunksMap = updater.referencesMapLong(getChunks(), ChunkUtils::getChunkIndex);
        final int[] entities = ArrayUtils.mapToIntArray(getEntityTracker().entities(), Entity::getEntityId);
        return new SnapshotImpl.Instance(updater.reference(MinecraftServer.process()),
                getDimensionType(), getWorldAge(), getTime(), chunksMap, entities,
                tagHandler().readableCopy());
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    /**
     * Gets the {@link InstanceContainer} from where this instance takes its chunks from.
     *
     * @return the associated {@link InstanceContainer}
     */
    public @NotNull InstanceContainer getInstanceContainer() {
        return instanceContainer;
    }

    @Override
    public void setBiome(int x, int y, int z, @NotNull Biome biome) {
        instanceContainer.setBiome(x, y, z, biome);
    }

    @Override
    public @NotNull Biome getBiome(int x, int y, int z) {
        return instanceContainer.getBiome(x, y, z);
    }
}
