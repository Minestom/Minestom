package net.minestom.server;

import net.minestom.server.utils.PropertyUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Contains server settings/flags to be set with system properties.
 *
 * <p>Some flags (labeled at the bottom) are experimental. They may be removed without notice, and may have issues.</p>
 */
public final class ServerFlag {

    // Server Behavior
    public static final int SERVER_TICKS_PER_SECOND = Integer.getInteger("minestom.tps", 20);
    public static final int SERVER_MAX_TICK_CATCH_UP = Integer.getInteger("minestom.max-tick-catch-up", 5);
    public static final int CHUNK_VIEW_DISTANCE = Integer.getInteger("minestom.chunk-view-distance", 8);
    public static final int ENTITY_VIEW_DISTANCE = Integer.getInteger("minestom.entity-view-distance", 5);
    public static final int ENTITY_SYNCHRONIZATION_TICKS = Integer.getInteger("minestom.entity-synchronization-ticks", 20);
    public static final int WORKER_COUNT = Integer.getInteger("minestom.workers", Runtime.getRuntime().availableProcessors());
    public static final int MAX_PACKET_SIZE = Integer.getInteger("minestom.max-packet-size", 2_097_151); // 3 bytes var-int
    public static final int SOCKET_SEND_BUFFER_SIZE = Integer.getInteger("minestom.send-buffer-size", 262_143);
    public static final int SOCKET_RECEIVE_BUFFER_SIZE = Integer.getInteger("minestom.receive-buffer-size", 32_767);
    public static final int POOLED_BUFFER_SIZE = Integer.getInteger("minestom.pooled-buffer-size", 262_143);
    public static final int PLAYER_PACKET_PER_TICK = Integer.getInteger("minestom.packet-per-tick", 20);
    public static final int PLAYER_PACKET_QUEUE_SIZE = Integer.getInteger("minestom.packet-queue-size", 1000);
    public static final int SEND_LIGHT_AFTER_BLOCK_PLACEMENT_DELAY = Integer.getInteger("minestom.send-light-after-block-placement-delay", 100);
    public static final long KEEP_ALIVE_DELAY = Long.getLong("minestom.keep-alive-delay", 10_000);
    public static final long KEEP_ALIVE_KICK = Long.getLong("minestom.keep-alive-kick", 30_000);
    public static final long LOGIN_PLUGIN_MESSAGE_TIMEOUT = Long.getLong("minestom.login-plugin-message-timeout", 5_000);

    // Packet sending optimizations
    public static final boolean GROUPED_PACKET = PropertyUtils.getBoolean("minestom.grouped-packet", true);
    public static final boolean CACHED_PACKET = PropertyUtils.getBoolean("minestom.cached-packet", true);
    public static final boolean VIEWABLE_PACKET = PropertyUtils.getBoolean("minestom.viewable-packet", true);

    // Tags
    public static final boolean TAG_HANDLER_CACHE_ENABLED = PropertyUtils.getBoolean("minestom.tag-handler-cache", true);
    public static final boolean SERIALIZE_EMPTY_COMPOUND = PropertyUtils.getBoolean("minestom.serialization.serialize-empty-nbt-compound", false);

    // Online Mode
    public static final @NotNull String AUTH_URL = System.getProperty("minestom.auth.url", "https://sessionserver.mojang.com/session/minecraft/hasJoined");

    // World
    public static final int WORLD_BORDER_SIZE = Integer.getInteger("minestom.world-border-size", 29999984);

    // Maps
    public static final @NotNull String MAP_RGB_MAPPING = System.getProperty("minestom.map.rgbmapping", "lazy");
    public static final @Nullable String MAP_RGB_REDUCTION = System.getProperty("minestom.map.rgbreduction"); // Only used if rgb mapping is "approximate"

    // Experimental/Unstable
    public static final boolean REGISTRY_LATE_REGISTER = Boolean.getBoolean("minestom.registry.late-register");
    public static final boolean REGISTRY_UNSAFE_OPS = Boolean.getBoolean("minestom.registry.unsafe-ops");
    public static final boolean EVENT_NODE_ALLOW_MULTIPLE_PARENTS = Boolean.getBoolean("minestom.event.multiple-parents");

    private ServerFlag() {}

}
