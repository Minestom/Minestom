package net.minestom.server.instance;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.utils.chunk.ChunkSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * The {@link SharedInstance} is an instance that shares the same chunks as its linked {@link InstanceContainer},
 * entities are separated.
 */
public class SharedInstance extends Instance {
    private final InstanceContainer instanceContainer;

    public SharedInstance(@NotNull UUID uniqueId, @NotNull InstanceContainer instanceContainer) {
        super(uniqueId, instanceContainer.getDimensionType());
        this.instanceContainer = instanceContainer;
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block, boolean doBlockUpdates) {
        this.instanceContainer.setBlock(x, y, z, block, doBlockUpdates);
    }

    @Override
    public boolean placeBlock(@NotNull BlockHandler.Placement placement, boolean doBlockUpdates) {
        return instanceContainer.placeBlock(placement, doBlockUpdates);
    }

    @Override
    public boolean breakBlock(@NotNull Player player, @NotNull Point blockPosition, @NotNull BlockFace blockFace, boolean doBlockUpdates) {
        return instanceContainer.breakBlock(player, blockPosition, blockFace, doBlockUpdates);
    }

    @Override
    public @NotNull CompletableFuture<Chunk> loadChunk(int chunkX, int chunkZ) {
        return instanceContainer.loadChunk(chunkX, chunkZ);
    }

    @Override
    public @NotNull CompletableFuture<Chunk> loadOptionalChunk(int chunkX, int chunkZ) {
        return instanceContainer.loadOptionalChunk(chunkX, chunkZ);
    }

    @Override
    public void unloadChunk(@NotNull Chunk chunk) {
        instanceContainer.unloadChunk(chunk);
    }

    @Override
    public Chunk getChunk(int chunkX, int chunkZ) {
        return instanceContainer.getChunk(chunkX, chunkZ);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveInstance() {
        return instanceContainer.saveInstance();
    }

    @Override
    public @NotNull CompletableFuture<Void> saveChunkToStorage(@NotNull Chunk chunk) {
        return instanceContainer.saveChunkToStorage(chunk);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveChunksToStorage() {
        return instanceContainer.saveChunksToStorage();
    }

    @Override
    public void setChunkSupplier(@NotNull ChunkSupplier chunkSupplier) {
        instanceContainer.setChunkSupplier(chunkSupplier);
    }

    @Override
    public ChunkSupplier getChunkSupplier() {
        return instanceContainer.getChunkSupplier();
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
    @Override
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

    /**
     * Gets the {@link InstanceContainer} from where this instance takes its chunks from.
     *
     * @return the associated {@link InstanceContainer}
     */
    public @NotNull InstanceContainer getInstanceContainer() {
        return instanceContainer;
    }
}
