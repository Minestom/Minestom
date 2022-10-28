package net.minestom.server.instance;

import com.extollit.gaming.ai.path.model.IColumnarSpace;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
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
import net.minestom.server.event.EventHandler;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.batch.Batch;
import net.minestom.server.instance.batch.SectionBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.network.packet.server.play.BlockActionPacket;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
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
import java.util.stream.IntStream;

import static net.minestom.server.utils.chunk.ChunkUtils.isLoaded;

/**
 * Instances are what are called "worlds" in Minecraft, you can add an entity to one using {@link Entity#setInstance(Instance)}.
 * <p>
 * WARNING: when making your own implementation registering the chunk manually is required
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
     * Schedules a task to be run during the next chunk tick.
     *
     * @param runnable the task to execute during the next chunk tick
     */
    void scheduleNextTick(@NotNull Runnable runnable);

    @ApiStatus.Internal
    boolean placeBlock(@NotNull BlockHandler.Placement placement);

    /**
     * Does call {@link net.minestom.server.event.player.PlayerBlockBreakEvent}
     * and send particle packets
     *
     * @param player        the {@link Player} who is breaking the block
     * @param blockPosition the position of the broken block
     * @return true if the block has been broken, false if it has been cancelled
     */
    @ApiStatus.Internal
    boolean breakBlock(@NotNull Player player, @NotNull Point blockPosition);

    /**
     * Saves the current chunk tags.
     * <p>
     * Warning: only the global chunk data will be saved, not blocks.
     * You would need to call {@link #saveChunksToStorage()} too.
     *
     * @return the future called once the chunk data has been saved
     */
    @ApiStatus.Experimental
    @NotNull CompletableFuture<Void> saveInstance();

    /**
     * Saves multiple chunks to permanent storage.
     * @return future called when the chunks are done saving
     */
    @NotNull CompletableFuture<Void> saveChunksToStorage();

    /**
     * Changes the chunk {@link ChunkGenerator}.
     * @param chunkGenerator the new {@link ChunkGenerator} of the chunk
     * @deprecated Use {@link #setGenerator(Generator)}
     */
    @Deprecated(forRemoval = true)
    default void setChunkGenerator(@Nullable ChunkGenerator chunkGenerator) {
        setGenerator(chunkGenerator != null ? new ChunkGeneratorCompatibilityLayer(chunkGenerator, getDimensionType()) : null);
    }

    /**
     * Sets the chunk loader for this instance.
     * Note that this method is not a guarantee that this instance will use this chunk loader.
     * @param chunkLoader the chunk loader to use
     */
    void setChunkLoader(@Nullable IChunkLoader chunkLoader);

    /**
     * Gets the generator associated with the chunk.
     * @return the generator if any
     */
    @Nullable Generator generator();

    /**
     * Changes the generator of the chunk
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
     * Gets if the chunk should autoload chunks.
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
     * Gets if the chunk has been registered in {@link InstanceManager}.
     *
     * @return true if the chunk has been registered
     */
    boolean isRegistered();

    /**
     * Changes the registered field.
     * <p>
     * WARNING: should only be used by {@link InstanceManager}.
     *
     * @param registered true to mark the chunk as registered
     */
    @ApiStatus.Internal
    void setRegistered(boolean registered);

    /**
     * Gets the chunk {@link DimensionType}.
     *
     * @return the dimension of the chunk
     */
    DimensionType getDimensionType();

    /**
     * Gets the age of this chunk in tick.
     *
     * @return the age of this chunk in tick
     */
    long getWorldAge();

    /**
     * Gets the current time in the instance (sun/moon).
     *
     * @return the time in the instance
     */
    long getTime();

    /**
     * Changes the current time in the chunk, from 0 to 24000.
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
     * It does send the new time to all players in the chunk, unaffected by {@link #getTimeUpdate()}
     *
     * @param time the new time of the chunk
     */
    void setTime(long time);

    /**
     * Gets the rate of the time passing, it is 1 by default
     *
     * @return the time rate of the chunk
     */
    int getTimeRate();

    /**
     * Changes the time rate of the chunk
     * <p>
     * 1 is the default value and can be set to 0 to be completely disabled (constant time)
     *
     * @param timeRate the new time rate of the chunk
     * @throws IllegalStateException if {@code timeRate} is lower than 0
     */
    void setTimeRate(int timeRate);

    /**
     * Gets the rate at which the client is updated with the current chunk time
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
    default @NotNull TimeUpdatePacket createTimePacket() {
        long time = getTime();
        if (getTimeRate() == 0) {
            // Negative values stop the sun and moon from moving
            // 0 as a long cannot be negative
            time = time == 0 ? -24000L : -Math.abs(time);
        }
        return new TimeUpdatePacket(getWorldAge(), time);
    }

    /**
     * Gets the chunk {@link WorldBorder};
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
     * Gets the experience orbs in the chunk.
     *
     * @return an unmodifiable {@link Set} containing all the experience orbs in the chunk
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
    default @NotNull Set<@NotNull Entity> getChunkEntities(long chunk) {
        return getEntities().stream()
                .filter(entity -> {
                    Point pos = entity.getPosition();
                    long chunkIndex = ChunkUtils.getChunkIndex(pos.chunkX(), pos.chunkZ());
                    return chunkIndex == chunk;
                })
                .collect(Collectors.toSet());
    }

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
     * @return the chunk explosion supplier, null if none was provided
     */
    @Nullable ExplosionSupplier getExplosionSupplier();

    /**
     * Registers the {@link ExplosionSupplier} to use in this chunk.
     *
     * @param supplier the explosion supplier
     */
    void setExplosionSupplier(@Nullable ExplosionSupplier supplier);

    /**
     * Gets the chunk space.
     * <p>
     * Used by the pathfinder for entities.
     *
     * @return the chunk space
     */
    @ApiStatus.Internal
    @NotNull PFInstanceSpace getInstanceSpace();

    @Override
    @NotNull Pointers pointers();

    /**
     * This method is used to indicate whether the chunk is read only or not.
     * @return true if the chunk is read only, false otherwise
     */
    boolean isReadOnly();

    // Sections
    /**
     * Gets all the loaded sections in this chunk.
     * @return a collection of all the loaded sections
     */
    Long2ObjectMap<Section> getLoadedSections();

    /**
     * Checks whether this section is loaded.
     * @param chunkX the chunk X
     * @param sectionY the section Y
     * @param chunkZ the chunk Z
     * @return true if the section is loaded, false otherwise
     */
    default boolean isSectionLoaded(int chunkX, int sectionY, int chunkZ) {
        return getLoadedSections().containsKey(ChunkUtils.getSectionIndex(chunkX, sectionY, chunkZ));
    }

    /**
     * @see #isSectionLoaded(int, int, int)
     */
    default boolean isSectionLoaded(long sectionIndex) {
        return isSectionLoaded(ChunkUtils.getSectionCoordX(sectionIndex), ChunkUtils.getSectionCoordY(sectionIndex),
                ChunkUtils.getSectionCoordZ(sectionIndex));
    }

    /**
     * @see #isSectionLoaded(int, int, int)
     */
    default boolean isSectionLoaded(Point pos) {
        return isSectionLoaded(pos.chunkX(), pos.section(), pos.chunkZ());
    }

    /**
     * Gets the section at the given position.
     * @param chunkX the chunk X
     * @param sectionY the section Y
     * @param chunkZ the chunk Z
     * @return the section, or null if it is not loaded
     */
    default @Nullable Section getSection(int chunkX, int sectionY, int chunkZ) {
        long sectionIndex = ChunkUtils.getSectionIndex(chunkX, sectionY, chunkZ);
        return getLoadedSections().get(sectionIndex);
    }

    /**
     * @see #getSection(int, int, int)
     */
    default @Nullable Section getSection(long sectionIndex) {
        return getSection(ChunkUtils.getSectionCoordX(sectionIndex), ChunkUtils.getSectionCoordY(sectionIndex),
                ChunkUtils.getSectionCoordZ(sectionIndex));
    }

    /**
     * @see #getSection(int, int, int)
     */
    default @Nullable Section getSection(Point pos) {
        return getSection(pos.chunkX(), pos.section(), pos.chunkZ());
    }

    /**
     * Loads the section at the given coordinates, generating blocks for it if the section is not present.
     * @param chunkX the chunk X
     * @param sectionY the section Y
     * @param chunkZ the chunk Z
     * @return completable future that completes when the section is loaded, or exceptionally if the section cannot be
     *         loaded (e.g. if it is loaded already)
     */
    CompletableFuture<Section> loadSection(int chunkX, int sectionY, int chunkZ);

    /**
     * @see #loadSection(int, int, int)
     */
    default CompletableFuture<Section> loadSection(long sectionIndex) {
        return loadSection(ChunkUtils.getSectionCoordX(sectionIndex), ChunkUtils.getSectionCoordY(sectionIndex),
                ChunkUtils.getSectionCoordZ(sectionIndex));
    }

    /**
     * @see #loadSection(int, int, int)
     */
    default CompletableFuture<Section> loadSection(Point pos) {
        return loadSection(pos.chunkX(), pos.section(), pos.chunkZ());
    }

    /**
     * Unloads the section at the given coordinates.
     * @param chunkX the chunk X
     * @param sectionY the section Y
     * @param chunkZ the chunk Z
     * @return completable future that completes when the section is unloaded, or completes exceptionally if the
     *         section cannot be unloaded
     */
    CompletableFuture<Void> unloadSection(int chunkX, int sectionY, int chunkZ);

    /**
     * @see #unloadSection(int, int, int)
     */
    default CompletableFuture<Void> unloadSection(long sectionIndex) {
        return unloadSection(ChunkUtils.getSectionCoordX(sectionIndex), ChunkUtils.getSectionCoordY(sectionIndex),
                ChunkUtils.getSectionCoordZ(sectionIndex));
    }

    /**
     * @see #unloadSection(int, int, int)
     */
    default CompletableFuture<Void> unloadSection(Point pos) {
        return unloadSection(pos.chunkX(), pos.section(), pos.chunkZ());
    }

    /**
     * Loads the section at the given coordinates if {@link #hasEnabledAutoChunkLoad()} is enabled.
     * @param chunkX the chunk X
     * @param sectionY the section Y
     * @param chunkZ the chunk Z
     * @return completable future as seen in {@link #loadSection(int, int, int)}, or completed with null if
     *         auto chunk load is disabled
     */
    default CompletableFuture<Section> loadOptionalSection(int chunkX, int sectionY, int chunkZ) {
        if (hasEnabledAutoChunkLoad()) {
            return loadSection(chunkX, sectionY, chunkZ);
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * @see #loadOptionalSection(int, int, int)
     */
    default CompletableFuture<Section> loadOptionalSection(long sectionIndex) {
        return loadOptionalSection(ChunkUtils.getSectionCoordX(sectionIndex), ChunkUtils.getSectionCoordY(sectionIndex),
                ChunkUtils.getSectionCoordZ(sectionIndex));
    }

    /**
     * @see #loadOptionalSection(int, int, int)
     */
    default CompletableFuture<Section> loadOptionalSection(Point pos) {
        return loadOptionalSection(pos.chunkX(), pos.section(), pos.chunkZ());
    }

    // Chunks

    /**
     * Gets all the loaded chunks in this instance.
     * @return a collection of all the loaded chunks
     */
    @NotNull Long2ObjectMap<Chunk> getLoadedChunks();

    /**
     * Checks whether this chunk is loaded.
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     * @return true if the chunk is loaded, false otherwise
     */
    default boolean isChunkLoaded(int chunkX, int chunkZ) {
        return getLoadedChunks().containsKey(ChunkUtils.getChunkIndex(chunkX, chunkZ));
    }

    /**
     * @see #isChunkLoaded(int, int)
     */
    default boolean isChunkLoaded(long chunkIndex) {
        return isChunkLoaded(ChunkUtils.getChunkCoordX(chunkIndex), ChunkUtils.getChunkCoordZ(chunkIndex));
    }

    /**
     * @see #isChunkLoaded(int, int)
     */
    default boolean isChunkLoaded(Point pos) {
        return isChunkLoaded(pos.chunkX(), pos.chunkZ());
    }

    /**
     * Gets the chunk at the given position.
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     * @return the chunk, or null if it is not loaded
     */
    default @Nullable Chunk getChunk(int chunkX, int chunkZ) {
        long chunkIndex = ChunkUtils.getChunkIndex(chunkX, chunkZ);
        return getLoadedChunks().get(chunkIndex);
    }

    /**
     * @see #getChunk(int, int)
     */
    default @Nullable Chunk getChunk(long chunkIndex) {
        return getChunk(ChunkUtils.getChunkCoordX(chunkIndex), ChunkUtils.getChunkCoordZ(chunkIndex));
    }

    /**
     * @see #getChunk(int, int)
     */
    default @Nullable Chunk getChunk(Point pos) {
        return getChunk(pos.chunkX(), pos.chunkZ());
    }

    /**
     * Loads the chunk at the given coordinates, generating blocks for it if the chunk is not present.
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     * @return completable future that completes when the chunk is loaded, or exceptionally if the chunk cannot be
     *         loaded (e.g. if it is loaded already)
     */
    default CompletableFuture<Chunk> loadChunk(int chunkX, int chunkZ) {
        int minSection = getDimensionType().getMinY() / Section.SIZE_Y;
        int maxSection = getDimensionType().getMaxY() / Section.SIZE_Y;

        return CompletableFuture.allOf(IntStream.range(minSection, maxSection)
                .mapToObj(sectionY -> loadSection(chunkX, sectionY, chunkZ))
                .toArray(CompletableFuture[]::new))
                .thenApply(v -> Chunk.viewInto(this, chunkX, chunkZ));
    }

    /**
     * @see #loadChunk(int, int)
     */
    default CompletableFuture<Chunk> loadChunk(long chunkIndex) {
        return loadChunk(ChunkUtils.getChunkCoordX(chunkIndex), ChunkUtils.getChunkCoordZ(chunkIndex));
    }

    /**
     * @see #loadChunk(int, int)
     */
    default CompletableFuture<Chunk> loadChunk(Point pos) {
        return loadChunk(pos.chunkX(), pos.chunkZ());
    }

    /**
     * Unloads the chunk at the given coordinates.
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     * @return completable future that completes when the chunk is unloaded, or completes exceptionally if the
     *         chunk cannot be unloaded
     */
    default CompletableFuture<Void> unloadChunk(int chunkX, int chunkZ) {
        int minSection = getDimensionType().getMinY() / Section.SIZE_Y;
        int maxSection = getDimensionType().getMaxY() / Section.SIZE_Y;

        return CompletableFuture.allOf(IntStream.range(minSection, maxSection)
                .mapToObj(sectionY -> unloadSection(chunkX, sectionY, chunkZ))
                .toArray(CompletableFuture[]::new));
    }

    /**
     * @see #unloadChunk(int, int)
     */
    default CompletableFuture<Void> unloadChunk(long chunkIndex) {
        return unloadChunk(ChunkUtils.getChunkCoordX(chunkIndex), ChunkUtils.getChunkCoordZ(chunkIndex));
    }

    /**
     * @see #unloadChunk(int, int)
     */
    default CompletableFuture<Void> unloadChunk(Point pos) {
        return unloadChunk(pos.chunkX(), pos.chunkZ());
    }

    /**
     * Loads the chunk at the given coordinates if {@link #hasEnabledAutoChunkLoad()} is enabled.
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     * @return completable future as seen in {@link #loadChunk(int, int)}, or completed with null if
     *         auto chunk load is disabled
     */
    default CompletableFuture<Chunk> loadOptionalChunk(int chunkX, int chunkZ) {
        if (hasEnabledAutoChunkLoad()) {
            return loadChunk(chunkX, chunkZ);
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * @see #loadOptionalChunk(int, int)
     */
    default CompletableFuture<Chunk> loadOptionalChunk(long chunkIndex) {
        return loadOptionalChunk(ChunkUtils.getChunkCoordX(chunkIndex), ChunkUtils.getChunkCoordZ(chunkIndex));
    }

    /**
     * @see #loadOptionalChunk(int, int)
     */
    default CompletableFuture<Chunk> loadOptionalChunk(Point pos) {
        return loadOptionalChunk(pos.chunkX(), pos.chunkZ());
    }

    /**
     * Gets the chunk packet for the given chunk coordinates.
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     * @return the chunk packet, null if the chunk is not loaded
     */
    default @UnknownNullability ChunkDataPacket chunkPacket(int chunkX, int chunkZ) {
        return Chunk.viewInto(this, chunkX, chunkZ).chunkPacket(chunkX, chunkZ);
    }

    /**
     * @see #chunkPacket(int, int)
     */
    default @UnknownNullability ChunkDataPacket chunkPacket(long chunkIndex) {
        return chunkPacket(ChunkUtils.getChunkCoordX(chunkIndex), ChunkUtils.getChunkCoordZ(chunkIndex));
    }

    /**
     * @see #chunkPacket(int, int)
     */
    default @UnknownNullability ChunkDataPacket chunkPacket(Point pos) {
        return chunkPacket(pos.chunkX(), pos.chunkZ());
    }

    // Threading/ticks
    /**
     * Refreshes the chunk at the given coordinates.
     * @param tickable the tickable object being added to the chunk
     * @param newChunkX the new chunk X
     * @param newChunkZ the new chunk Z
     */
    @Deprecated
    void refreshCurrentChunk(Tickable tickable, int newChunkX, int newChunkZ);

    @ApiStatus.Internal
    void registerDispatcher(ThreadDispatcher<SectionCache> dispatcher);

    // Pathfinding
    IColumnarSpace createColumnarSpace(PFInstanceSpace instanceSpace, int cx, int cz);

    // Batch
    default CompletableFuture<Void> applyBatch(SectionBatch batch, int chunkX, int sectionY, int chunkZ) {
        return CompletableFuture.runAsync(() -> {
            int minX = chunkX * Chunk.SIZE_X;
            int minY = sectionY * Section.SIZE_Y;
            int minZ = chunkZ * Chunk.SIZE_Z;

            batch.blocks((x, y, z, block) -> setBlock(x + minX, y + minY, z + minZ, block));
        }, Batch.BLOCK_BATCH_POOL);
    }

    // Lighting

    /**
     * Gets the skylight at the given section.
     * @param chunkX the chunk X
     * @param sectionY the section Y
     * @param chunkZ the chunk Z
     * @return the skylight byte array, null if the section is not loaded
     */
    ByteList getSkyLight(int chunkX, int sectionY, int chunkZ);

    /**
     * Gets the blocklight at the given section.
     * @param chunkX the chunk X
     * @param sectionY the section Y
     * @param chunkZ the chunk Z
     * @return the blocklight byte array, null if the section is not loaded
     */
    ByteList getBlockLight(int chunkX, int sectionY, int chunkZ);

    /**
     * Sets the skylight at the given section.
     * @param chunkX the chunk X
     * @param sectionY the section Y
     * @param chunkZ the chunk Z
     * @param light the skylight byte array
     * @throws IllegalStateException if the section is not loaded
     */
    void setSkyLight(int chunkX, int sectionY, int chunkZ, ByteList light);

    /**
     * Sets the skylight at the given section.
     * @param chunkX the chunk X
     * @param sectionY the section Y
     * @param chunkZ the chunk Z
     * @param light the blocklight byte array
     */
    default void setSkyLight(int chunkX, int sectionY, int chunkZ, byte[] light) {
        setSkyLight(chunkX, sectionY, chunkZ, ByteList.of(light));
    }

    /**
     * Sets the blocklight at the given section.
     * @param chunkX the chunk X
     * @param sectionY the section Y
     * @param chunkZ the chunk Z
     * @param light the blocklight byte array
     * @throws IllegalStateException if the section is not loaded
     */
    void setBlockLight(int chunkX, int sectionY, int chunkZ, ByteList light);

    /**
     * Sets the blocklight at the given section.
     * @param chunkX the chunk X
     * @param sectionY the section Y
     * @param chunkZ the chunk Z
     * @param light the blocklight byte array
     */
    default void setBlockLight(int chunkX, int sectionY, int chunkZ, byte[] light) {
        setBlockLight(chunkX, sectionY, chunkZ, ByteList.of(light));
    }
}
