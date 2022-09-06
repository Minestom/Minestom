package net.minestom.server;

import net.minestom.server.advancements.AdvancementManager;
import net.minestom.server.adventure.bossbar.BossBarManager;
import net.minestom.server.command.CommandManager;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.exception.ExceptionManager;
import net.minestom.server.extensions.ExtensionManager;
import net.minestom.server.gamedata.tags.TagManager;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.listener.manager.PacketListenerManager;
import net.minestom.server.monitoring.BenchmarkManager;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.PacketProcessor;
import net.minestom.server.network.packet.server.play.PluginMessagePacket;
import net.minestom.server.network.packet.server.play.ServerDifficultyPacket;
import net.minestom.server.network.socket.Server;
import net.minestom.server.recipe.RecipeManager;
import net.minestom.server.scoreboard.TeamManager;
import net.minestom.server.thread.TickSchedulerThread;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.Difficulty;
import net.minestom.server.world.DimensionTypeManager;
import net.minestom.server.world.biomes.BiomeManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * The main server class used to start the server and retrieve all the managers.
 * <p>
 * The server needs to be initialized with {@link #init()} and started with {@link #start(String, int)}.
 * You should register all of your dimensions, biomes, commands, events, etc... in-between.
 */
public final class MinecraftServer {

    public final static Logger LOGGER = LoggerFactory.getLogger(MinecraftServer.class);

    public static final String VERSION_NAME = "1.19.2";
    public static final int PROTOCOL_VERSION = 760;

    // Threads
    public static final String THREAD_NAME_BENCHMARK = "Ms-Benchmark";

    public static final String THREAD_NAME_TICK_SCHEDULER = "Ms-TickScheduler";
    public static final String THREAD_NAME_TICK = "Ms-Tick";

    // Config
    // Can be modified at performance cost when increased
    public static final int TICK_PER_SECOND = Integer.getInteger("minestom.tps", 20);
    public static final int TICK_MS = 1000 / TICK_PER_SECOND;

    // In-Game Manager
    private static volatile ServerProcess serverProcess;

    private static int chunkViewDistance = Integer.getInteger("minestom.chunk-view-distance", 8);
    private static int entityViewDistance = Integer.getInteger("minestom.entity-view-distance", 5);
    private static int compressionThreshold = 256;
    private static boolean terminalEnabled = System.getProperty("minestom.terminal.disabled") == null;
    private static String brandName = "Minestom";
    private static Difficulty difficulty = Difficulty.NORMAL;

    public static MinecraftServer init() {
        updateProcess();
        return new MinecraftServer();
    }

    @ApiStatus.Internal
    public static ServerProcess updateProcess() {
        ServerProcess process;
        try {
            process = new ServerProcessImpl();
            serverProcess = process;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return process;
    }

    /**
     * Gets the current server brand name.
     *
     * @return the server brand name
     */
    @NotNull
    public static String getBrandName() {
        return brandName;
    }

    /**
     * Changes the server brand name and send the change to all connected players.
     *
     * @param brandName the server brand name
     * @throws NullPointerException if {@code brandName} is null
     */
    public static void setBrandName(@NotNull String brandName) {
        MinecraftServer.brandName = brandName;
        PacketUtils.broadcastPacket(PluginMessagePacket.getBrandPacket());
    }

    /**
     * Gets the server difficulty showed in game option.
     *
     * @return the server difficulty
     */
    @NotNull
    public static Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Changes the server difficulty and send the appropriate packet to all connected clients.
     *
     * @param difficulty the new server difficulty
     */
    public static void setDifficulty(@NotNull Difficulty difficulty) {
        MinecraftServer.difficulty = difficulty;
        PacketUtils.broadcastPacket(new ServerDifficultyPacket(difficulty, true));
    }

    @ApiStatus.Experimental
    public static @UnknownNullability ServerProcess process() {
        return serverProcess;
    }

    public static @NotNull GlobalEventHandler getGlobalEventHandler() {
        return serverProcess.eventHandler();
    }

    public static @NotNull PacketListenerManager getPacketListenerManager() {
        return serverProcess.packetListener();
    }

    public static @NotNull InstanceManager getInstanceManager() {
        return serverProcess.instance();
    }

    public static @NotNull BlockManager getBlockManager() {
        return serverProcess.block();
    }

    public static @NotNull CommandManager getCommandManager() {
        return serverProcess.command();
    }

    public static @NotNull RecipeManager getRecipeManager() {
        return serverProcess.recipe();
    }

    public static @NotNull TeamManager getTeamManager() {
        return serverProcess.team();
    }

    public static @NotNull SchedulerManager getSchedulerManager() {
        return serverProcess.scheduler();
    }

    /**
     * Gets the manager handling server monitoring.
     *
     * @return the benchmark manager
     */
    public static @NotNull BenchmarkManager getBenchmarkManager() {
        return serverProcess.benchmark();
    }

    public static @NotNull ExceptionManager getExceptionManager() {
        return serverProcess.exception();
    }

    public static @NotNull ConnectionManager getConnectionManager() {
        return serverProcess.connection();
    }

    public static @NotNull BossBarManager getBossBarManager() {
        return serverProcess.bossBar();
    }

    public static @NotNull PacketProcessor getPacketProcessor() {
        return serverProcess.packetProcessor();
    }

    public static boolean isStarted() {
        return serverProcess.isAlive();
    }

    public static boolean isStopping() {
        return !isStarted();
    }

    /**
     * Gets the chunk view distance of the server.
     *
     * @return the chunk view distance
     */
    public static int getChunkViewDistance() {
        return chunkViewDistance;
    }

    /**
     * Changes the chunk view distance of the server.
     *
     * @param chunkViewDistance the new chunk view distance
     * @throws IllegalArgumentException if {@code chunkViewDistance} is not between 2 and 32
     * @deprecated should instead be defined with a java property
     */
    @Deprecated
    public static void setChunkViewDistance(int chunkViewDistance) {
        Check.stateCondition(serverProcess.isAlive(), "You cannot change the chunk view distance after the server has been started.");
        Check.argCondition(!MathUtils.isBetween(chunkViewDistance, 2, 32),
                "The chunk view distance must be between 2 and 32");
        MinecraftServer.chunkViewDistance = chunkViewDistance;
    }

    /**
     * Gets the entity view distance of the server.
     *
     * @return the entity view distance
     */
    public static int getEntityViewDistance() {
        return entityViewDistance;
    }

    /**
     * Changes the entity view distance of the server.
     *
     * @param entityViewDistance the new entity view distance
     * @throws IllegalArgumentException if {@code entityViewDistance} is not between 0 and 32
     * @deprecated should instead be defined with a java property
     */
    @Deprecated
    public static void setEntityViewDistance(int entityViewDistance) {
        Check.stateCondition(serverProcess.isAlive(), "You cannot change the entity view distance after the server has been started.");
        Check.argCondition(!MathUtils.isBetween(entityViewDistance, 0, 32),
                "The entity view distance must be between 0 and 32");
        MinecraftServer.entityViewDistance = entityViewDistance;
    }

    /**
     * Gets the compression threshold of the server.
     *
     * @return the compression threshold, 0 means that compression is disabled
     */
    public static int getCompressionThreshold() {
        return compressionThreshold;
    }

    /**
     * Changes the compression threshold of the server.
     * <p>
     * WARNING: this need to be called before {@link #start(SocketAddress)}.
     *
     * @param compressionThreshold the new compression threshold, 0 to disable compression
     * @throws IllegalStateException if this is called after the server started
     */
    public static void setCompressionThreshold(int compressionThreshold) {
        Check.stateCondition(serverProcess.isAlive(), "The compression threshold cannot be changed after the server has been started.");
        MinecraftServer.compressionThreshold = compressionThreshold;
    }

    /**
     * Gets if the built in Minestom terminal is enabled.
     *
     * @return true if the terminal is enabled
     */
    public static boolean isTerminalEnabled() {
        return terminalEnabled;
    }

    /**
     * Enabled/disables the built in Minestom terminal.
     *
     * @param enabled true to enable, false to disable
     */
    public static void setTerminalEnabled(boolean enabled) {
        Check.stateCondition(serverProcess.isAlive(), "Terminal settings may not be changed after starting the server.");
        MinecraftServer.terminalEnabled = enabled;
    }

    public static DimensionTypeManager getDimensionTypeManager() {
        return serverProcess.dimension();
    }

    public static BiomeManager getBiomeManager() {
        return serverProcess.biome();
    }

    public static AdvancementManager getAdvancementManager() {
        return serverProcess.advancement();
    }

    public static ExtensionManager getExtensionManager() {
        return serverProcess.extension();
    }

    public static TagManager getTagManager() {
        return serverProcess.tag();
    }

    public static Server getServer() {
        return serverProcess.server();
    }

    /**
     * Starts the server.
     * <p>
     * It should be called after {@link #init()} and probably your own initialization code.
     *
     * @param address the server address
     * @throws IllegalStateException if called before {@link #init()} or if the server is already running
     */
    public void start(@NotNull SocketAddress address) {
        serverProcess.start(address);
        new TickSchedulerThread(serverProcess).start();
    }

    public void start(@NotNull String address, int port) {
        start(new InetSocketAddress(address, port));
    }

    /**
     * Stops this server properly (saves if needed, kicking players, etc.)
     */
    public static void stopCleanly() {
        serverProcess.stop();
    }
}
