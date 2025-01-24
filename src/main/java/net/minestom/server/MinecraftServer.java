package net.minestom.server;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.advancements.AdvancementManager;
import net.minestom.server.adventure.bossbar.BossBarManager;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.animal.tameable.WolfMeta;
import net.minestom.server.entity.metadata.other.PaintingMeta;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.exception.ExceptionManager;
import net.minestom.server.gamedata.tags.TagManager;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.banner.BannerPattern;
import net.minestom.server.instance.block.jukebox.JukeboxSong;
import net.minestom.server.item.armor.TrimMaterial;
import net.minestom.server.item.armor.TrimPattern;
import net.minestom.server.item.enchant.*;
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
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.Difficulty;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biome.Biome;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
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
        PacketSendingUtils.broadcastPlayPacket(PluginMessagePacket.brandPacket(brandName));
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
        PacketSendingUtils.broadcastPlayPacket(new ServerDifficultyPacket(difficulty, true));
    }

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

    public static @NotNull PacketParser<ClientPacket> getPacketParser() {
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

    public static TagManager getTagManager() {
        return serverProcess.tag();
    }

    public static @NotNull DynamicRegistry<ChatType> getChatTypeRegistry() {
        return serverProcess.chatType();
    }

    public static @NotNull DynamicRegistry<DimensionType> getDimensionTypeRegistry() {
        return serverProcess.dimensionType();
    }

    public static @NotNull DynamicRegistry<Biome> getBiomeRegistry() {
        return serverProcess.biome();
    }

    public static @NotNull DynamicRegistry<DamageType> getDamageTypeRegistry() {
        return serverProcess.damageType();
    }

    public static @NotNull DynamicRegistry<TrimMaterial> getTrimMaterialRegistry() {
        return serverProcess.trimMaterial();
    }

    public static @NotNull DynamicRegistry<TrimPattern> getTrimPatternRegistry() {
        return serverProcess.trimPattern();
    }

    public static @NotNull DynamicRegistry<BannerPattern> getBannerPatternRegistry() {
        return serverProcess.bannerPattern();
    }

    public static @NotNull DynamicRegistry<WolfMeta.Variant> getWolfVariantRegistry() {
        return serverProcess.wolfVariant();
    }

    public static @NotNull DynamicRegistry<Enchantment> getEnchantmentRegistry() {
        return serverProcess.enchantment();
    }

    public static @NotNull DynamicRegistry<PaintingMeta.Variant> getPaintingVariantRegistry() {
        return serverProcess.paintingVariant();
    }

    public static @NotNull DynamicRegistry<JukeboxSong> getJukeboxSongRegistry() {
        return serverProcess.jukeboxSong();
    }

    public static @NotNull DynamicRegistry<BinaryTagSerializer<? extends LevelBasedValue>> enchantmentLevelBasedValues() {
        return process().enchantmentLevelBasedValues();
    }

    public static @NotNull DynamicRegistry<BinaryTagSerializer<? extends ValueEffect>> enchantmentValueEffects() {
        return process().enchantmentValueEffects();
    }

    public static @NotNull DynamicRegistry<BinaryTagSerializer<? extends EntityEffect>> enchantmentEntityEffects() {
        return process().enchantmentEntityEffects();
    }

    public static @NotNull DynamicRegistry<BinaryTagSerializer<? extends LocationEffect>> enchantmentLocationEffects() {
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
