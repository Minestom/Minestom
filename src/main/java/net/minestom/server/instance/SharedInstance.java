package net.minestom.server.instance;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.utils.chunk.ChunkSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * The {@link SharedInstance} is an instance that shares the same chunks as its linked {@link InstanceContainer},
 * entities are separated.
 */
public class SharedInstance extends Instance {
    private final InstanceContainer instanceContainer;
    private boolean sharesEntities = false;

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

    @Override
    public @NotNull Set<@NotNull Player> getPlayers() {
        Set<Player> allPlayers = super.getPlayers();
        if (!sharesEntities) return allPlayers;
        Set<Player> playersHere = new HashSet<>();
        for (Player p : allPlayers) {
            if (p.getInstance().equals(this)) playersHere.add(p);
        }
        return Collections.unmodifiableSet(playersHere);
    }

    /**
     * Changes whether this {@link SharedInstance} should share entities from the underlying {@link InstanceContainer}.
     * If this is changed from true to false, the entities from the underlying {@link InstanceContainer} will no longer
     * show in this {@link SharedInstance} and vice-versa.
     *
     * @param sharesEntities whether this {@link SharedInstance} should share the entities from the underlying {@link InstanceContainer}
     */
    public void setSharesEntities(boolean sharesEntities) {
        if (sharesEntities == this.sharesEntities) return;
        this.sharesEntities = sharesEntities;
        EntityTracker entityTracker = getEntityTracker();
        Set<Entity> containerEntities = instanceContainer.getEntities();
        if (sharesEntities) {
            // register entities already inside the instanceContainer
            for (Entity e : containerEntities) {
                entityTracker.register(e, e.getPosition(), e.getTrackingTarget(), e.getTrackingUpdate());
            }
        } else {
            // unregister entities already inside the instanceContainer
            for (Entity e : containerEntities) {
                entityTracker.unregister(e,  e.getTrackingTarget(), e.getTrackingUpdate());
            }
        }
    }

    /**
     * Gets if this {@link SharedInstance} is sharing entities from the underlying {@link InstanceContainer}
     *
     * @return true if this {@link SharedInstance} is sharing entities from the underlying {@link InstanceContainer}
     */
    public boolean sharesEntities() {
        return sharesEntities;
    }

    /**
     * Gets if two instances share the same chunks.
     *
     * @param instance1 the first instance
     * @param instance2 the second instance
     * @return true if the two instances share the same chunks
     */
    public static boolean areLinked(Instance instance1, Instance instance2) {
        // SharedInstance check
        if (instance1 instanceof InstanceContainer && instance2 instanceof SharedInstance) {
            return ((SharedInstance) instance2).getInstanceContainer().equals(instance1);
        } else if (instance2 instanceof InstanceContainer && instance1 instanceof SharedInstance) {
            return ((SharedInstance) instance1).getInstanceContainer().equals(instance2);
        } else if (instance1 instanceof SharedInstance && instance2 instanceof SharedInstance) {
            final InstanceContainer container1 = ((SharedInstance) instance1).getInstanceContainer();
            final InstanceContainer container2 = ((SharedInstance) instance2).getInstanceContainer();
            return container1.equals(container2);
        }

        // InstanceContainer check (copied from)
        if (instance1 instanceof InstanceContainer container1 && instance2 instanceof InstanceContainer container2) {
            if (container1.getSrcInstance() != null) {
                return container1.getSrcInstance().equals(container2)
                        && container1.getLastBlockChangeTime() == container2.getLastBlockChangeTime();
            } else if (container2.getSrcInstance() != null) {
                return container2.getSrcInstance().equals(container1)
                        && container2.getLastBlockChangeTime() == container1.getLastBlockChangeTime();
            }
        }
        return false;
    }
}
