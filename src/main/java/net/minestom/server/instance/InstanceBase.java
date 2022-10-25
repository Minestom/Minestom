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
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.Generator;
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
 * A base instance with some default behaviour used to help with custom instance implementations.
 */
public abstract class InstanceBase implements Instance {

    private boolean registered;

    private final DimensionType dimensionType;

    private final WorldBorder worldBorder;

    // Tick since the creation of the instance
    private long worldAge;

    // The time of the instance
    private long time;
    private int timeRate = 1;
    private Duration timeUpdate = Duration.of(1, TimeUnit.SECOND);
    private long lastTimeUpdate;

    // Field for tick events
    private long lastTickAge = System.currentTimeMillis();
    protected final EntityTracker entityTracker = new EntityTrackerImpl();

    // the uuid of this instance
    protected UUID uniqueId;

    // world loading
    protected Generator generator = null;

    protected boolean autoLoadChunks = true;

    // instance custom data
    private final TagHandler tagHandler = TagHandler.newHandler();
    private final Scheduler scheduler = Scheduler.newScheduler();
    private final EventNode<InstanceEvent> eventNode;

    // the explosion supplier
    private ExplosionSupplier explosionSupplier;


    // Pathfinder
    private final PFInstanceSpace instanceSpace = new PFInstanceSpace(this);

    // Adventure
    private final Pointers pointers;

    protected long lastBlockChangeTime; // Time at which the last block change happened (#setBlock)

    /**
     * Creates a new instance.
     *
     * @param uniqueId      the {@link UUID} of the instance
     * @param dimensionType the {@link DimensionType} of the instance
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
    public @Nullable Generator generator() {
        return generator;
    }

    @Override
    public void setGenerator(@Nullable Generator generator) {
        this.generator = generator;
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
        if (block == null) throw new NullPointerException("Unloaded chunk at " + x + "," + y + "," + z);
        return block;
    }

    @Override
    public @NotNull Viewable getViewersAt(int x, int y, int z) {
        assert Chunk.CHUNK_SIZE_X == Chunk.CHUNK_SIZE_Z;
        int viewDistance = MinecraftServer.getEntityViewDistance() * Chunk.CHUNK_SIZE_X;
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
        Chunk chunk = new DynamicChunk(instance, chunkX, chunkZ);

        int xMin = chunkX / Chunk.CHUNK_SIZE_X;
        int yMin = instance.getDimensionType().getMinY();
        int zMin = chunkZ / Chunk.CHUNK_SIZE_Z;

        int xMax = xMin + Chunk.CHUNK_SIZE_X;
        int yMax = instance.getDimensionType().getMaxY();
        int zMax = zMin + Chunk.CHUNK_SIZE_Z;

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
}
