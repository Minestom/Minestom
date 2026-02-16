package net.minestom.server;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.advancements.AdvancementManager;
import net.minestom.server.adventure.ClickCallbackManager;
import net.minestom.server.adventure.bossbar.BossBarManager;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.command.CommandManager;
import net.minestom.server.dialog.Dialog;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.animal.ZombieNautilusVariant;
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
import net.minestom.server.world.timeline.Timeline;
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
    private static final boolean IMMUTABLE_SERVER_PROCESS = // Dont use for tests, or when explicitly disabled
            !ServerFlag.ALLOW_MULTIPLE_INITIALIZATIONS && !ServerFlag.INSIDE_TEST;

    // In-Game Manager
    private static volatile @UnknownNullability ServerProcess serverProcess; // Mutable holder, {@see ImmutableServerProcessHolder}

    private static int compressionThreshold = 256;
    private static String brandName = "Minestom";
    private static Difficulty difficulty = Difficulty.NORMAL;

    public static MinecraftServer init(Auth auth) {
        updateProcess(auth);
        return new MinecraftServer();
    }

    public static MinecraftServer init() {
        return init(new Auth.Offline());
    }

    @ApiStatus.Internal
    public static ServerProcess updateProcess(Auth auth) {
        if (serverProcess != null && IMMUTABLE_SERVER_PROCESS) {
            // It's likely that the server process is already initialized, and we are in immutable mode.
            LOGGER.warn("""
                    The server process is likely already initialized, but you are trying to initialize it again.
                    This is not allowed in immutable mode. If you want to change the server process,
                    you will need to restart the JVM and or set ServerFlag.ALLOW_MULTIPLE_INITIALIZATIONS to true.
                    """);
            return serverProcess;
        }
        serverProcess = new ServerProcessImpl(auth);
        return process();
    }

    @ApiStatus.Internal
    public static ServerProcess updateProcess() {
        return updateProcess(new Auth.Offline());
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
        if (serverProcess == null) return; // Use mutable holder here
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
        if (serverProcess == null) return; // Use mutable holder here
        PacketSendingUtils.broadcastPlayPacket(new ServerDifficultyPacket(difficulty, true));
    }

    public static @UnknownNullability ServerProcess process() {
        if (IMMUTABLE_SERVER_PROCESS) {
            // The first caller will lock the process to be immutable.
            return ImmutableServerProcessHolder.SERVER_PROCESS;
        } else {
            return serverProcess;
        }
    }

    public static GlobalEventHandler getGlobalEventHandler() {
        return process().eventHandler();
    }

    public static PacketListenerManager getPacketListenerManager() {
        return process().packetListener();
    }

    public static InstanceManager getInstanceManager() {
        return process().instance();
    }

    public static BlockManager getBlockManager() {
        return process().block();
    }

    public static CommandManager getCommandManager() {
        return process().command();
    }

    public static RecipeManager getRecipeManager() {
        return process().recipe();
    }

    public static TeamManager getTeamManager() {
        return process().team();
    }

    public static SchedulerManager getSchedulerManager() {
        return process().scheduler();
    }

    /**
     * Gets the manager handling server monitoring.
     *
     * @return the benchmark manager
     */
    public static BenchmarkManager getBenchmarkManager() {
        return process().benchmark();
    }

    public static ExceptionManager getExceptionManager() {
        return process().exception();
    }

    public static ConnectionManager getConnectionManager() {
        return process().connection();
    }

    public static BossBarManager getBossBarManager() {
        return process().bossBar();
    }

    public static PacketParser<ClientPacket> getPacketParser() {
        return process().packetParser();
    }

    public static boolean isStarted() {
        return process().isAlive();
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
        // This method could be called before init; Use the mutable holder to not accidentally initialize the immutable holder.
        Check.stateCondition(serverProcess != null && serverProcess.isAlive(), "The compression threshold cannot be changed after the server has been started.");
        MinecraftServer.compressionThreshold = compressionThreshold;
    }

    public static AdvancementManager getAdvancementManager() {
        return process().advancement();
    }

    public static ClickCallbackManager getClickCallbackManager() {
        return process().clickCallbackManager();
    }

    public static DynamicRegistry<ChatType> getChatTypeRegistry() {
        return process().chatType();
    }

    public static DynamicRegistry<Dialog> getDialogRegistry() {
        return process().dialog();
    }

    public static DynamicRegistry<DimensionType> getDimensionTypeRegistry() {
        return process().dimensionType();
    }

    public static DynamicRegistry<Biome> getBiomeRegistry() {
        return process().biome();
    }

    public static DynamicRegistry<DamageType> getDamageTypeRegistry() {
        return process().damageType();
    }

    public static DynamicRegistry<TrimMaterial> getTrimMaterialRegistry() {
        return process().trimMaterial();
    }

    public static DynamicRegistry<TrimPattern> getTrimPatternRegistry() {
        return process().trimPattern();
    }

    public static DynamicRegistry<BannerPattern> getBannerPatternRegistry() {
        return process().bannerPattern();
    }

    public static DynamicRegistry<WolfVariant> getWolfVariantRegistry() {
        return process().wolfVariant();
    }

    public static DynamicRegistry<ZombieNautilusVariant> getZombieNautilusVariantRegistry() {
        return process().zombieNautilusVariant();
    }

    public static DynamicRegistry<Enchantment> getEnchantmentRegistry() {
        return process().enchantment();
    }

    public static DynamicRegistry<PaintingVariant> getPaintingVariantRegistry() {
        return process().paintingVariant();
    }

    public static DynamicRegistry<JukeboxSong> getJukeboxSongRegistry() {
        return process().jukeboxSong();
    }

    public static DynamicRegistry<Instrument> getInstrumentRegistry() {
        return process().instrument();
    }

    public static DynamicRegistry<Timeline> getTimelineRegistry() {
        return process().timeline();
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
        return process().server();
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
        process().start(address);
        process().dispatcher().start();
        new TickSchedulerThread(process()).start();
    }

    public void start(String address, int port) {
        start(new InetSocketAddress(address, port));
    }

    /**
     * Stops this server properly (saves if needed, kicking players, etc.)
     */
    public static void stopCleanly() {
        process().stop();
    }

    /**
     * Allows Minestom to get constant folding for the server process;
     * This has the side effect of not allowing the server process to be mutable,
     * So using an immutable process will require a full JVM restart;
     */
    private static final class ImmutableServerProcessHolder {
        static {
            Check.stateCondition(!IMMUTABLE_SERVER_PROCESS, "ServerProcessHolder.Immutable should only be initialized when the server process is immutable.");
            Check.notNull(serverProcess, "The server is not initialized yet; Did you forget to use MinecraftServer.init()?");
        }

        private static final ServerProcess SERVER_PROCESS = serverProcess;
    }
}