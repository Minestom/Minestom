package net.minestom.server;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.advancements.AdvancementManager;
import net.minestom.server.adventure.bossbar.BossBarManager;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.animal.tameable.WolfVariant;
import net.minestom.server.entity.metadata.other.PaintingVariant;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.exception.ExceptionManager;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.banner.BannerPattern;
import net.minestom.server.instance.block.jukebox.JukeboxSong;
import net.minestom.server.item.armor.TrimMaterial;
import net.minestom.server.item.armor.TrimPattern;
import net.minestom.server.item.enchant.*;
import net.minestom.server.item.instrument.Instrument;
import net.minestom.server.listener.manager.PacketListenerManager;
import net.minestom.server.message.ChatType;
import net.minestom.server.monitoring.BenchmarkManager;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.PacketParser;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import net.minestom.server.network.packet.server.play.ServerDifficultyPacket;
import net.minestom.server.network.socket.Server;
import net.minestom.server.recipe.RecipeManager;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.scoreboard.TeamManager;
import net.minestom.server.thread.TickSchedulerThread;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.utils.PacketSendingUtils;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.Difficulty;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biome.Biome;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * The main server class used to start the server and retrieve all the managers.
 * <p>
 * The server needs to be initialized with {@link #init()} and started with {@link #start(String, int)}.
 * You should register all of your dimensions, biomes, commands, events, etc... in-between.
 */
public final class MinecraftServer implements MinecraftConstants {

    public static final ComponentLogger LOGGER = ComponentLogger.logger(MinecraftServer.class);

    // Threads
    public static final String THREAD_NAME_BENCHMARK = "Ms-Benchmark";

    public static final String THREAD_NAME_TICK_SCHEDULER = "Ms-TickScheduler";
    public static final String THREAD_NAME_TICK = "Ms-Tick";

    // Config
    // Can be modified at performance cost when increased
    @Deprecated
    public static final int TICK_PER_SECOND = ServerFlag.SERVER_TICKS_PER_SECOND;
    public static final int TICK_MS = 1000 / TICK_PER_SECOND;

    // In-Game Manager
    private static volatile ServerProcess serverProcess;

    private static int compressionThreshold = 256;
    private static String brandName = "Minestom";
    private static Difficulty difficulty = Difficulty.NORMAL;

    public static MinecraftServer init() {
        updateProcess();
        return new MinecraftServer();
    }

    @ApiStatus.Internal
    public static ServerProcess updateProcess() {
        ServerProcess process = new ServerProcessImpl();
        serverProcess = process;
        return process;
    }

    /**
     * Gets the current server brand name.
     *
     * @return the server brand name
     */
    public static String getBrandName() {
        return brandName;
    }

    /**
     * Changes the server brand name and send the change to all connected players.
     *
     * @param brandName the server brand name
     * @throws NullPointerException if {@code brandName} is null
     */
    public static void setBrandName(String brandName) {
        MinecraftServer.brandName = brandName;
        PacketSendingUtils.broadcastPlayPacket(PluginMessagePacket.brandPacket(brandName));
    }

    /**
     * Gets the server difficulty showed in game option.
     *
     * @return the server difficulty
     */
    public static Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Changes the server difficulty and send the appropriate packet to all connected clients.
     *
     * @param difficulty the new server difficulty
     */
    public static void setDifficulty(Difficulty difficulty) {
        MinecraftServer.difficulty = difficulty;
        PacketSendingUtils.broadcastPlayPacket(new ServerDifficultyPacket(difficulty, true));
    }

    public static @UnknownNullability ServerProcess process() {
        return serverProcess;
    }

    public static GlobalEventHandler getGlobalEventHandler() {
        return serverProcess.eventHandler();
    }

    public static PacketListenerManager getPacketListenerManager() {
        return serverProcess.packetListener();
    }

    public static InstanceManager getInstanceManager() {
        return serverProcess.instance();
    }

    public static BlockManager getBlockManager() {
        return serverProcess.block();
    }

    public static CommandManager getCommandManager() {
        return serverProcess.command();
    }

    public static RecipeManager getRecipeManager() {
        return serverProcess.recipe();
    }

    public static TeamManager getTeamManager() {
        return serverProcess.team();
    }

    public static SchedulerManager getSchedulerManager() {
        return serverProcess.scheduler();
    }

    /**
     * Gets the manager handling server monitoring.
     *
     * @return the benchmark manager
     */
    public static BenchmarkManager getBenchmarkManager() {
        return serverProcess.benchmark();
    }

    public static ExceptionManager getExceptionManager() {
        return serverProcess.exception();
    }

    public static ConnectionManager getConnectionManager() {
        return serverProcess.connection();
    }

    public static BossBarManager getBossBarManager() {
        return serverProcess.bossBar();
    }

    public static PacketParser<ClientPacket> getPacketParser() {
        return serverProcess.packetParser();
    }

    public static boolean isStarted() {
        return serverProcess.isAlive();
    }

    public static boolean isStopping() {
        return !isStarted();
    }

    /**
     * Gets the chunk view distance of the server.
     * <p>
     * Deprecated in favor of {@link ServerFlag#CHUNK_VIEW_DISTANCE}
     *
     * @return the chunk view distance
     */
    @Deprecated
    public static int getChunkViewDistance() {
        return ServerFlag.CHUNK_VIEW_DISTANCE;
    }

    /**
     * Gets the entity view distance of the server.
     * <p>
     * Deprecated in favor of {@link ServerFlag#ENTITY_VIEW_DISTANCE}
     *
     * @return the entity view distance
     */
    @Deprecated
    public static int getEntityViewDistance() {
        return ServerFlag.ENTITY_VIEW_DISTANCE;
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
        Check.stateCondition(serverProcess != null && serverProcess.isAlive(), "The compression threshold cannot be changed after the server has been started.");
        MinecraftServer.compressionThreshold = compressionThreshold;
    }

    public static AdvancementManager getAdvancementManager() {
        return serverProcess.advancement();
    }

    public static DynamicRegistry<ChatType> getChatTypeRegistry() {
        return serverProcess.chatType();
    }

    public static DynamicRegistry<DimensionType> getDimensionTypeRegistry() {
        return serverProcess.dimensionType();
    }

    public static DynamicRegistry<Biome> getBiomeRegistry() {
        return serverProcess.biome();
    }

    public static DynamicRegistry<DamageType> getDamageTypeRegistry() {
        return serverProcess.damageType();
    }

    public static DynamicRegistry<TrimMaterial> getTrimMaterialRegistry() {
        return serverProcess.trimMaterial();
    }

    public static DynamicRegistry<TrimPattern> getTrimPatternRegistry() {
        return serverProcess.trimPattern();
    }

    public static DynamicRegistry<BannerPattern> getBannerPatternRegistry() {
        return serverProcess.bannerPattern();
    }

    public static DynamicRegistry<WolfVariant> getWolfVariantRegistry() {
        return serverProcess.wolfVariant();
    }

    public static DynamicRegistry<Enchantment> getEnchantmentRegistry() {
        return serverProcess.enchantment();
    }

    public static DynamicRegistry<PaintingVariant> getPaintingVariantRegistry() {
        return serverProcess.paintingVariant();
    }

    public static DynamicRegistry<JukeboxSong> getJukeboxSongRegistry() {
        return serverProcess.jukeboxSong();
    }

    public static DynamicRegistry<Instrument> getInstrumentRegistry() {
        return serverProcess.instrument();
    }

    public static DynamicRegistry<StructCodec<? extends LevelBasedValue>> enchantmentLevelBasedValues() {
        return process().enchantmentLevelBasedValues();
    }

    public static DynamicRegistry<StructCodec<? extends ValueEffect>> enchantmentValueEffects() {
        return process().enchantmentValueEffects();
    }

    public static DynamicRegistry<StructCodec<? extends EntityEffect>> enchantmentEntityEffects() {
        return process().enchantmentEntityEffects();
    }

    public static DynamicRegistry<StructCodec<? extends LocationEffect>> enchantmentLocationEffects() {
        return process().enchantmentLocationEffects();
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
    public void start(SocketAddress address) {
        serverProcess.start(address);
        serverProcess.dispatcher().start();
        new TickSchedulerThread(serverProcess).start();
    }

    public void start(String address, int port) {
        start(new InetSocketAddress(address, port));
    }

    /**
     * Stops this server properly (saves if needed, kicking players, etc.)
     */
    public static void stopCleanly() {
        serverProcess.stop();
    }
}
