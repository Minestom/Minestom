package net.minestom.server.instance;

import com.extollit.gaming.ai.path.model.IColumnarSpace;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.pointer.Pointers;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerProcess;
import net.minestom.server.Viewable;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ExperienceOrb;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.pathfinding.PFColumnarSpace;
import net.minestom.server.entity.pathfinding.PFInstanceSpace;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.network.packet.server.play.EffectPacket;
import net.minestom.server.network.packet.server.play.TimeUpdatePacket;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.utils.*;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.time.Cooldown;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A base chunk with some default behaviour used to help with custom chunk implementations.
 */
public abstract class InstanceBase implements Instance {

    protected boolean registered;

    protected final DimensionType dimensionType;

    protected final WorldBorder worldBorder;

    // Tick since the creation of the chunk
    protected long worldAge;

    // The time of the chunk
    protected long time;
    protected int timeRate = 1;
    protected Duration timeUpdate = Duration.of(1, TimeUnit.SECOND);
    protected long lastTimeUpdate;

    // Field for tick events
    protected long lastTickAge = System.currentTimeMillis();
    protected final EntityTracker entityTracker = new EntityTrackerImpl();

    // the uuid of this chunk
    protected UUID uniqueId;

    // world loading
    protected Generator generator = null;
    protected IChunkLoader chunkLoader = null;

    protected boolean autoLoadChunks = true;

    // chunk custom data
    protected final TagHandler tagHandler = TagHandler.newHandler();
    protected final Scheduler scheduler = Scheduler.newScheduler();
    protected final EventNode<InstanceEvent> eventNode;

    // the explosion supplier
    protected ExplosionSupplier explosionSupplier;


    // Pathfinder
    protected final PFInstanceSpace instanceSpace = new PFInstanceSpace(this);

    // Adventure
    protected final Pointers pointers;

    protected long lastBlockChangeTime; // Time at which the last block change happened (#setBlock)
    protected boolean isReadOnly = true; // Whether the instance is read-only (no block changes allowed)

    /**
     * Creates a new chunk.
     *
     * @param uniqueId      the {@link UUID} of the chunk
     * @param dimensionType the {@link DimensionType} of the chunk
     */
    public InstanceBase(@NotNull UUID uniqueId, @NotNull DimensionType dimensionType) {
        Check.argCondition(!dimensionType.isRegistered(),
                "The dimension " + dimensionType.getName() + " is not registered! Please use DimensionTypeManager#addDimension");
        this.uniqueId = uniqueId;
        this.dimensionType = dimensionType;

        this.worldBorder = new WorldBorder(this);

        this.pointers = Pointers.builder()
                .withDynamic(Identity.UUID, this::getUniqueId)
                .build();

        final ServerProcess process = MinecraftServer.process();
        if (process != null) {
            this.eventNode = process.eventHandler().map(this, EventFilter.INSTANCE);
        } else {
            // Local nodes require a server process
            this.eventNode = null;
        }
    }

    @Override
    public void scheduleNextTick(@NotNull Runnable runnable) {
        scheduler.scheduleNextTick(runnable);
    }

    @Override
    public boolean placeBlock(@NotNull BlockHandler.Placement placement) {
        final Point blockPosition = placement.getBlockPosition();
        final Block block = placement.getBlock();
        setBlock(blockPosition, block);
        return true;
    }

    @Override
    public boolean breakBlock(@NotNull Player player, @NotNull Point blockPosition) {
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

    @Override
    public @Nullable Generator generator() {
        return generator;
    }

    @Override
    public void setGenerator(@Nullable Generator generator) {
        this.generator = generator;
    }

    @Override
    public void setChunkLoader(IChunkLoader chunkLoader) {
        this.chunkLoader = chunkLoader;
    }

    @Override
    public void enableAutoChunkLoad(boolean enable) {
        this.autoLoadChunks = enable;
    }

    @Override
    public boolean hasEnabledAutoChunkLoad() {
        return autoLoadChunks;
    }

    @Override
    public boolean isRegistered() {
        return registered;
    }

    @Override
    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    @Override
    public DimensionType getDimensionType() {
        return dimensionType;
    }

    @Override
    public long getWorldAge() {
        return worldAge;
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public int getTimeRate() {
        return timeRate;
    }

    @Override
    public void setTimeRate(int timeRate) {
        this.timeRate = timeRate;
    }

    @Override
    public @Nullable Duration getTimeUpdate() {
        return timeUpdate;
    }

    @Override
    public void setTimeUpdate(@Nullable Duration timeUpdate) {
        this.timeUpdate = timeUpdate;
    }

    public @NotNull TimeUpdatePacket createTimePacket() {
        long time = this.time;
        if (timeRate == 0) {
            //Negative values stop the sun and moon from moving
            //0 as a long cannot be negative
            time = time == 0 ? -24000L : -Math.abs(time);
        }
        return new TimeUpdatePacket(worldAge, time);
    }

    @Override
    public @NotNull WorldBorder getWorldBorder() {
        return worldBorder;
    }

    public @NotNull Set<@NotNull Entity> getEntities() {
        return entityTracker.entities();
    }

    public @NotNull Set<@NotNull Player> getPlayers() {
        return entityTracker.entities(EntityTracker.Target.PLAYERS);
    }

    public @NotNull Set<@NotNull EntityCreature> getCreatures() {
        return entityTracker.entities().stream()
                .filter(EntityCreature.class::isInstance)
                .map(entity -> (EntityCreature) entity)
                .collect(Collectors.toUnmodifiableSet());
    }

    public @NotNull Set<@NotNull ExperienceOrb> getExperienceOrbs() {
        return entityTracker.entities().stream()
                .filter(ExperienceOrb.class::isInstance)
                .map(entity -> (ExperienceOrb) entity)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public @NotNull Set<@NotNull Entity> getChunkEntities(long chunk) {
        int chunkX = ChunkUtils.getChunkCoordX(chunk);
        int chunkZ = ChunkUtils.getChunkCoordZ(chunk);
        //noinspection SimplifyStreamApiCallChains
        return entityTracker.chunkEntities(chunkX, chunkZ, EntityTracker.Target.ENTITIES)
                .stream()
                .collect(Collectors.toSet());
    }

    public @NotNull Collection<Entity> getNearbyEntities(@NotNull Point point, double range) {
        List<Entity> result = new ArrayList<>();
        this.entityTracker.nearbyEntities(point, range, EntityTracker.Target.ENTITIES, result::add);
        return result;
    }

    public @Nullable Block getBlock(int x, int y, int z, @NotNull Condition condition) {
        final Block block = retrieveBlock(x, y, z, condition);
        return switch (condition) {
            case TYPE -> block == null ? Block.AIR : block;
            case NONE -> block;
            case CACHED -> block == null ? Block.AIR : block.hasNbt() || block.handler() != null ? block : null;
        };
    }

    @Override
    public @NotNull Viewable getViewersAt(int x, int y, int z) {
        assert Chunk.SIZE_X == Chunk.SIZE_Z;
        int viewDistance = MinecraftServer.getEntityViewDistance() * Chunk.SIZE_X;
        Set<Player> players = getPlayers().stream()
                .filter(player -> player.getPosition().distanceSquared(x, y, z) <= viewDistance * viewDistance)
                .collect(Collectors.toUnmodifiableSet());
        return () -> players;
    }

    public EntityTracker getEntityTracker() {
        return entityTracker;
    }

    @Override
    public @NotNull UUID getUniqueId() {
        return uniqueId;
    }

    public void tick(long time) {
        // Scheduled tasks
        this.scheduler.processTick();
        // Time
        {
            this.worldAge++;
            this.time += timeRate;
            // time needs to be sent to players
            if (timeUpdate != null && !Cooldown.hasCooldown(time, lastTimeUpdate, timeUpdate)) {
                PacketUtils.sendGroupedPacket(getPlayers(), createTimePacket());
                this.lastTimeUpdate = time;
            }
        }
        // Tick event
        {
            // Process tick events
            EventDispatcher.call(new InstanceTickEvent(this, time, lastTickAge));
            // Set last tick age
            this.lastTickAge = time;
        }
        this.worldBorder.update();
    }

    public @NotNull TagHandler tagHandler() {
        return tagHandler;
    }

    public @NotNull Scheduler scheduler() {
        return scheduler;
    }

    public @NotNull EventNode<InstanceEvent> eventNode() {
        return eventNode;
    }

    public @Nullable ExplosionSupplier getExplosionSupplier() {
        return explosionSupplier;
    }

    public void setExplosionSupplier(@Nullable ExplosionSupplier supplier) {
        this.explosionSupplier = supplier;
    }

    public @NotNull PFInstanceSpace getInstanceSpace() {
        return instanceSpace;
    }

    public @NotNull Pointers pointers() {
        return this.pointers;
    }

    @Override
    public IColumnarSpace createColumnarSpace(PFInstanceSpace instanceSpace, int cx, int cz) {
        return new PFColumnarSpace(instanceSpace, InstanceBase.createChunk(this, cx, cz));
    }

    static @NotNull Chunk createChunk(Instance instance, int chunkX, int chunkZ) {
        Chunk chunk = Chunk.inMemory();

        int xMin = chunkX / Chunk.SIZE_X;
        int yMin = instance.getDimensionType().getMinY();
        int zMin = chunkZ / Chunk.SIZE_Z;

        int xMax = xMin + Chunk.SIZE_X;
        int yMax = instance.getDimensionType().getMaxY();
        int zMax = zMin + Chunk.SIZE_Z;

        for (int x = xMin; x < xMax; x++) {
            for (int y = yMin; y < yMax; y++) {
                for (int z = zMin; z < zMax; z++) {
                    Block block = instance.getBlock(x, y, z);
                    chunk.setBlock(x, y, z, block);
                }
            }
        }

        return chunk;
    }

    @Override
    public boolean isReadOnly() {
        return isReadOnly;
    }
}
