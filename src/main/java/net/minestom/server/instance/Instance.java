package net.minestom.server.instance;

import com.extollit.gaming.ai.path.model.IColumnarSpace;
import it.unimi.dsi.fastutil.bytes.ByteList;
import net.kyori.adventure.pointer.Pointers;
import net.minestom.server.Tickable;
import net.minestom.server.Viewable;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ExperienceOrb;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.pathfinding.PFInstanceSpace;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventHandler;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.batch.Batch;
import net.minestom.server.instance.batch.SectionBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.network.packet.server.play.BlockActionPacket;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.network.packet.server.play.EffectPacket;
import net.minestom.server.network.packet.server.play.TimeUpdatePacket;
import net.minestom.server.snapshot.*;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.tag.Taggable;
import net.minestom.server.thread.ThreadDispatcher;
import net.minestom.server.timer.Schedulable;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static net.minestom.server.utils.chunk.ChunkUtils.isLoaded;

/**
 * Instances are what are called "worlds" in Minecraft, you can add an entity to one using {@link Entity#setInstance(Instance)}.
 * <p>
 * WARNING: when making your own implementation registering the instance manually is required
 * with {@link InstanceManager#registerInstance(Instance)}, and
 * you need to be sure to signal the {@link ThreadDispatcher} of every partition/element changes.
 */
public interface Instance extends Block.Getter, Block.Setter, Biome.Setter, Biome.Getter,
        Tickable, Schedulable, Snapshotable, EventHandler<InstanceEvent>, Taggable, PacketGroupingAudience {

    @Deprecated(forRemoval = true)
    default void scheduleNextTick(@NotNull Consumer<Instance> callback) {
        scheduleNextTick(() -> callback.accept(this));
    }

    /**
     * Schedules a task to be run during the next instance tick.
     *
     * @param runnable the task to execute during the next instance tick
     */
    void scheduleNextTick(@NotNull Runnable runnable);

    @ApiStatus.Internal
    default boolean placeBlock(@NotNull BlockHandler.Placement placement) {
        final Point blockPosition = placement.getBlockPosition();
        final Block block = placement.getBlock();
        setBlock(blockPosition, block);
        return true;
    }

    /**
     * Does call {@link net.minestom.server.event.player.PlayerBlockBreakEvent}
     * and send particle packets
     *
     * @param player        the {@link Player} who is breaking the block
     * @param blockPosition the position of the broken block
     * @return true if the block has been broken, false if it has been cancelled
     */
    @ApiStatus.Internal
    default boolean breakBlock(@NotNull Player player, @NotNull Point blockPosition) {
        if (isReadOnly()) return false;

        final Block block = getBlock(blockPosition);
        if (block.isAir()) {
            // TODO: The player probably has a wrong version of this chunk section, send it
            return false;
        }

        PlayerBlockBreakEvent blockBreakEvent = new PlayerBlockBreakEvent(player, block, Block.AIR, blockPosition);
        EventDispatcher.call(blockBreakEvent);
        final boolean allowed = !blockBreakEvent.isCancelled();
        if (allowed) {
            // Break or change the broken block based on event result
            final Block resultBlock = blockBreakEvent.getResultBlock();
            setBlock(blockPosition, resultBlock);

            // Send the block break effect packet
            PacketUtils.sendGroupedPacket(getViewersAt(blockPosition).getViewers(),
                    new EffectPacket(2001 /*Block break + block break sound*/, blockPosition, block.stateId(), false),
                    // Prevent the block breaker to play the particles and sound two times
                    (viewer) -> !viewer.equals(player));
        }
        return allowed;
    }

    /**
     * Saves the current instance tags.
     * <p>
     * Warning: only the global instance data will be saved, not blocks.
     * You would need to call {@link #saveChunksToStorage()} too.
     *
     * @return the future called once the instance data has been saved
     */
    @ApiStatus.Experimental
    @NotNull CompletableFuture<Void> saveInstance();

    /**
     * Saves multiple chunks to permanent storage.
     *
     * @return future called when the chunks are done saving
     */
    @NotNull CompletableFuture<Void> saveChunksToStorage();

    /**
     * Changes the instance {@link ChunkGenerator}.
     *
     * @param chunkGenerator the new {@link ChunkGenerator} of the instance
     * @deprecated Use {@link #setGenerator(Generator)}
     */
    @Deprecated
    default void setChunkGenerator(@Nullable ChunkGenerator chunkGenerator) {
        setGenerator(chunkGenerator != null ? new ChunkGeneratorCompatibilityLayer(chunkGenerator, getDimensionType()) : null);
    }

    /**
     * Gets the generator associated with the instance
     *
     * @return the generator if any
     */
    @Nullable Generator generator();

    /**
     * Changes the generator of the instance
     *
     * @param generator the new generator, or null to disable generation
     */
    void setGenerator(@Nullable Generator generator);

    /**
     * When set to true, chunks will load automatically when requested.
     * Otherwise, you will need to manually load the chunks before players may even join.
     *
     * @param enable enable the auto chunk load
     */
    void enableAutoChunkLoad(boolean enable);

    /**
     * Gets if the instance should autoload chunks.
     *
     * @return true if auto chunk load is enabled, false otherwise
     */
    boolean hasEnabledAutoChunkLoad();

    /**
     * Determines whether a position in the void. If true, entities should take damage and die.
     * <p>
     * Always returning false allow entities to survive in the void.
     *
     * @param point the point in the world
     * @return true if the point is inside the void
     */
    default boolean isInVoid(@NotNull Point point) {
        return point.y() < getDimensionType().getMinY();
    }

    /**
     * Gets if the instance has been registered in {@link InstanceManager}.
     *
     * @return true if the instance has been registered
     */
    boolean isRegistered();

    /**
     * Changes the registered field.
     * <p>
     * WARNING: should only be used by {@link InstanceManager}.
     *
     * @param registered true to mark the instance as registered
     */
    @ApiStatus.Internal
    void setRegistered(boolean registered);

    /**
     * Gets the instance {@link DimensionType}.
     *
     * @return the dimension of the instance
     */
    DimensionType getDimensionType();

    /**
     * Gets the age of this instance in tick.
     *
     * @return the age of this instance in tick
     */
    long getWorldAge();

    /**
     * Gets the current time in the instance (sun/moon).
     *
     * @return the time in the instance
     */
    long getTime();

    /**
     * Changes the current time in the instance, from 0 to 24000.
     * <p>
     * If the time is negative, the vanilla client will not move the sun.
     * <p>
     * 0 = sunrise
     * 6000 = noon
     * 12000 = sunset
     * 18000 = midnight
     * <p>
     * This method is unaffected by {@link #getTimeRate()}
     * <p>
     * It does send the new time to all players in the instance, unaffected by {@link #getTimeUpdate()}
     *
     * @param time the new time of the instance
     */
    void setTime(long time);

    /**
     * Gets the rate of the time passing, it is 1 by default
     *
     * @return the time rate of the instance
     */
    int getTimeRate();

    /**
     * Changes the time rate of the instance
     * <p>
     * 1 is the default value and can be set to 0 to be completely disabled (constant time)
     *
     * @param timeRate the new time rate of the instance
     * @throws IllegalStateException if {@code timeRate} is lower than 0
     */
    void setTimeRate(int timeRate);

    /**
     * Gets the rate at which the client is updated with the current instance time
     *
     * @return the client update rate for time related packet
     */
    @Nullable Duration getTimeUpdate();

    /**
     * Changes the rate at which the client is updated about the time
     * <p>
     * Setting it to null means that the client will never know about time change
     * (but will still change server-side)
     *
     * @param timeUpdate the new update rate concerning time
     */
    void setTimeUpdate(@Nullable Duration timeUpdate);

    /**
     * Creates a {@link TimeUpdatePacket} with the current age and time of this instance
     *
     * @return the {@link TimeUpdatePacket} with this instance data
     */
    @ApiStatus.Internal
    @NotNull TimeUpdatePacket createTimePacket();

    /**
     * Gets the instance {@link WorldBorder};
     *
     * @return the {@link WorldBorder} linked to the instance
     */
    @NotNull WorldBorder getWorldBorder();

    /**
     * Gets the entities in the instance;
     *
     * @return an unmodifiable {@link Set} containing all the entities in the instance
     */
    @NotNull Set<@NotNull Entity> getEntities();

    /**
     * Gets the players in the instance;
     *
     * @return an unmodifiable {@link Set} containing all the players in the instance
     */
    @Override
    default @NotNull Set<@NotNull Player> getPlayers() {
        return getEntities().stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Gets the creatures in the instance;
     *
     * @return an unmodifiable {@link Set} containing all the creatures in the instance
     */
    @Deprecated
    default @NotNull Set<@NotNull EntityCreature> getCreatures() {
        return getEntities().stream()
                .filter(entity -> entity instanceof EntityCreature)
                .map(entity -> (EntityCreature) entity)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Gets the experience orbs in the instance.
     *
     * @return an unmodifiable {@link Set} containing all the experience orbs in the instance
     */
    @Deprecated
    @NotNull
    default Set<@NotNull ExperienceOrb> getExperienceOrbs() {
        return getEntities().stream()
                .filter(entity -> entity instanceof ExperienceOrb)
                .map(entity -> (ExperienceOrb) entity)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Gets the entities located in the chunk.
     *
     * @param chunk the chunk to get the entities from
     * @return an unmodifiable {@link Set} containing all the entities in a chunk,
     * if {@code chunk} is unloaded, return an empty {@link Set}
     */
    @NotNull Set<@NotNull Entity> getChunkEntities(long chunk);

    /**
     * Gets nearby entities to the given position.
     *
     * @param point position to look at
     * @param range max range from the given point to collect entities at
     * @return entities that are not further than the specified distance from the transmitted position.
     */
    default @NotNull Collection<Entity> getNearbyEntities(@NotNull Point point, double range) {
        return getEntities().stream()
                .filter(entity -> entity.getPosition().distanceSquared(point) <= range * range)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    default @Nullable Block getBlock(int x, int y, int z, @NotNull Condition condition) {
        return retrieveBlock(x, y, z, condition);
    }

    /**
     * Retrieves a {@link Block} from this {@link Instance}'s internal storage.
     * @param x the x coordinate of the block
     * @param y the y coordinate of the block
     * @param z the z coordinate of the block
     * @param condition the block condition
     * @return the block if it exists and matches the condition, otherwise null
     */
    @ApiStatus.Internal
    @Nullable Block retrieveBlock(int x, int y, int z, @NotNull Condition condition);

    @NotNull Viewable getViewersAt(int x, int y, int z);
    default @NotNull Viewable getViewersAt(Point blockPosition) {
        return getViewersAt(blockPosition.blockX(), blockPosition.blockY(), blockPosition.blockZ());
    }

    /**
     * Sends a {@link BlockActionPacket} for all the viewers of the specific position.
     *
     * @param blockPosition the block position
     * @param actionId      the action id, depends on the block
     * @param actionParam   the action parameter, depends on the block
     * @see <a href="https://wiki.vg/Protocol#Block_Action">BlockActionPacket</a> for the action id &amp; param
     */
    @Deprecated
    default void sendBlockAction(@NotNull Point blockPosition, byte actionId, byte actionParam) {
        final Block block = getBlock(blockPosition);
        final Viewable viewers = getViewersAt(blockPosition);
        PacketUtils.prepareViewablePacket(viewers, new BlockActionPacket(blockPosition, actionId, actionParam, block));
    }

    @ApiStatus.Experimental
    @Deprecated
    EntityTracker getEntityTracker();

    /**
     * Gets the instance unique id.
     *
     * @return the instance unique id
     */
    @NotNull UUID getUniqueId();

    /**
     * Performs a single tick in the instance, including scheduled tasks from {@link #scheduleNextTick(Consumer)}.
     * <p>
     * Warning: this does not update chunks and entities.
     *
     * @param time the tick time in milliseconds
     */
    @Override
    void tick(long time);

    @Override
    @NotNull TagHandler tagHandler();

    @Override
    @NotNull Scheduler scheduler();

    @Override
    @ApiStatus.Experimental
    @NotNull EventNode<InstanceEvent> eventNode();

    @Override
    @NotNull InstanceSnapshot updateSnapshot(@NotNull SnapshotUpdater updater);

    /**
     * Creates an explosion at the given position with the given strength.
     * The algorithm used to compute damages is provided by {@link #getExplosionSupplier()}.
     *
     * @param centerX  the center X
     * @param centerY  the center Y
     * @param centerZ  the center Z
     * @param strength the strength of the explosion
     * @throws IllegalStateException If no {@link ExplosionSupplier} was supplied
     */
    default void explode(float centerX, float centerY, float centerZ, float strength) {
        explode(centerX, centerY, centerZ, strength, null);
    }

    /**
     * Creates an explosion at the given position with the given strength.
     * The algorithm used to compute damages is provided by {@link #getExplosionSupplier()}.
     *
     * @param centerX        center X of the explosion
     * @param centerY        center Y of the explosion
     * @param centerZ        center Z of the explosion
     * @param strength       the strength of the explosion
     * @param additionalData data to pass to the explosion supplier
     * @throws IllegalStateException If no {@link ExplosionSupplier} was supplied
     */
    default void explode(float centerX, float centerY, float centerZ, float strength, @Nullable NBTCompound additionalData) {
        final ExplosionSupplier explosionSupplier = getExplosionSupplier();
        Check.stateCondition(explosionSupplier == null, "Tried to create an explosion with no explosion supplier");
        final Explosion explosion = explosionSupplier.createExplosion(centerX, centerY, centerZ, strength, additionalData);
        explosion.apply(this);
    }

    /**
     * Gets the registered {@link ExplosionSupplier}, or null if none was provided.
     *
     * @return the instance explosion supplier, null if none was provided
     */
    @Nullable ExplosionSupplier getExplosionSupplier();

    /**
     * Registers the {@link ExplosionSupplier} to use in this instance.
     *
     * @param supplier the explosion supplier
     */
    void setExplosionSupplier(@Nullable ExplosionSupplier supplier);

    /**
     * Gets the instance space.
     * <p>
     * Used by the pathfinder for entities.
     *
     * @return the instance space
     */
    @ApiStatus.Internal
    @NotNull PFInstanceSpace getInstanceSpace();

    @Override
    @NotNull Pointers pointers();

    /**
     * This method is used to indicate whether the instance is read only or not.
     * @return true if the instance is read only, false otherwise
     */
    boolean isReadOnly();

    @Deprecated
    @NotNull CompletableFuture<Void> loadChunk(int chunkX, int chunkZ);

    @Deprecated
    default @NotNull CompletableFuture<Void> loadChunk(Point blockPosition) {
        return loadChunk(blockPosition.chunkX(), blockPosition.chunkZ());
    }

    @Deprecated
    default @NotNull CompletableFuture<Void> loadOptionalChunk(int chunkX, int chunkZ) {
        if (hasEnabledAutoChunkLoad()) {
            return loadChunk(chunkX, chunkZ);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Deprecated
    default @NotNull CompletableFuture<Void> loadOptionalChunk(Point blockPosition) {
        return loadOptionalChunk(blockPosition.chunkX(), blockPosition.chunkZ());
    }

    @Deprecated
    boolean isChunkLoaded(long currentChunk);
    @Deprecated
    default boolean isChunkLoaded(int chunkX, int chunkZ) {
        return isChunkLoaded(ChunkUtils.getChunkIndex(chunkX, chunkZ));
    }
    @Deprecated
    default boolean isChunkLoaded(Point blockPosition) {
        return isChunkLoaded(ChunkUtils.getChunkIndex(blockPosition));
    }

    /**
     * Gets the chunk packet for the given chunk coordinates.
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     * @return the chunk packet, null if the chunk is not loaded
     */
    default @UnknownNullability ChunkDataPacket chunkPacket(int chunkX, int chunkZ) {
        return InstanceBase.createChunk(this, chunkX, chunkZ).chunkPacket();
    }

    /**
     * Refreshes the chunk at the given coordinates.
     * @param tickable the tickable object being added to the chunk
     * @param newChunkX the new chunk X
     * @param newChunkZ the new chunk Z
     */
    @Deprecated
    void refreshCurrentChunk(Tickable tickable, int newChunkX, int newChunkZ);

    @ApiStatus.Internal
    void registerDispatcher(ThreadDispatcher<Chunk> dispatcher);

    IColumnarSpace createColumnarSpace(PFInstanceSpace instanceSpace, int cx, int cz);

    default CompletableFuture<Void> applyBatch(SectionBatch batch, int chunkX, int sectionY, int chunkZ) {
        return CompletableFuture.runAsync(() -> {
            int minX = chunkX * Chunk.CHUNK_SIZE_X;
            int minY = sectionY * Chunk.CHUNK_SECTION_SIZE;
            int minZ = chunkZ * Chunk.CHUNK_SIZE_Z;

            batch.blocks((x, y, z, block) -> setBlock(x + minX, y + minY, z + minZ, block));
        }, Batch.BLOCK_BATCH_POOL);
    }

    // Lighting
    ByteList getSkyLight(int chunkX, int sectionY, int chunkZ);
    ByteList getBlockLight(int chunkX, int sectionY, int chunkZ);

    void setSkyLight(int chunkX, int sectionY, int chunkZ, ByteList light);
    void setBlockLight(int chunkX, int sectionY, int chunkZ, ByteList light);

    void clearSection(int chunkX, int sectionY, int chunkZ);

    boolean isSectionLoaded(int chunkX, int sectionY, int chunkZ);
}
