package net.minestom.server;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Contains server settings/flags to be set with system properties.
 *
 * <p>Some flags (labeled at the bottom) are experimental. They may be removed without notice, and may have issues.</p>
 */
public final class ServerFlag {

    // Server Behavior
    public static final Boolean SHUTDOWN_ON_SIGNAL = booleanProperty("minestom.shutdown-on-signal", true);
    public static final int SERVER_TICKS_PER_SECOND = intProperty("minestom.tps", 20);
    public static final int SERVER_MAX_TICK_CATCH_UP = intProperty("minestom.max-tick-catch-up", 5);
    public static final int CHUNK_VIEW_DISTANCE = intProperty("minestom.chunk-view-distance", 8);
    public static final int ENTITY_VIEW_DISTANCE = intProperty("minestom.entity-view-distance", 5);
    public static final int ENTITY_SYNCHRONIZATION_TICKS = intProperty("minestom.entity-synchronization-ticks", 20);
    public static final int DISPATCHER_THREADS = intProperty("minestom.dispatcher-threads", 1);
    public static final int SEND_LIGHT_AFTER_BLOCK_PLACEMENT_DELAY = intProperty("minestom.send-light-after-block-placement-delay", 100);
    public static final long LOGIN_PLUGIN_MESSAGE_TIMEOUT = longProperty("minestom.login-plugin-message-timeout", 5_000);

    // Network rate limiting
    public static final int PLAYER_PACKET_PER_TICK = intProperty("minestom.packet-per-tick", 50);
    public static final int PLAYER_PACKET_QUEUE_SIZE = intProperty("minestom.packet-queue-size", 1000);
    public static final long KEEP_ALIVE_DELAY = longProperty("minestom.keep-alive-delay", 10_000);
    public static final long KEEP_ALIVE_KICK = longProperty("minestom.keep-alive-kick", 15_000);

    // Network buffers
    public static final int MAX_PACKET_SIZE = intProperty("minestom.max-packet-size", 2_097_151); // 3 bytes var-int
    public static final int MAX_PACKET_SIZE_PRE_AUTH = intProperty("minestom.max-packet-size-pre-auth", 8_192);
    public static final int SOCKET_SEND_BUFFER_SIZE = intProperty("minestom.send-buffer-size", 262_143);
    public static final int SOCKET_RECEIVE_BUFFER_SIZE = intProperty("minestom.receive-buffer-size", 32_767);
    public static final boolean SOCKET_NO_DELAY = booleanProperty("minestom.tcp-no-delay", true);
    public static final int SOCKET_TIMEOUT = intProperty("minestom.socket-timeout", 15_000);
    public static final int POOLED_BUFFER_SIZE = intProperty("minestom.pooled-buffer-size", 16_383);

    // Chunk update
    public static final float MIN_CHUNKS_PER_TICK = floatProperty("minestom.chunk-queue.min-per-tick", 0.01f);
    public static final float MAX_CHUNKS_PER_TICK = floatProperty("minestom.chunk-queue.max-per-tick", 64.0f);
    public static final float CHUNKS_PER_TICK_MULTIPLIER = floatProperty("minestom.chunk-queue.multiplier", 1f);

    // Packet sending optimizations
    public static final boolean GROUPED_PACKET = booleanProperty("minestom.grouped-packet", true);
    public static final boolean CACHED_PACKET = booleanProperty("minestom.cached-packet", true);
    public static final boolean VIEWABLE_PACKET = booleanProperty("minestom.viewable-packet", true);

    // Tags
    public static final boolean TAG_HANDLER_CACHE_ENABLED = booleanProperty("minestom.tag-handler-cache", true);
    public static final boolean SERIALIZE_EMPTY_COMPOUND = booleanProperty("minestom.serialization.serialize-empty-nbt-compound", false);

    // Online Mode
    public static final @NotNull String AUTH_URL = stringProperty("minestom.auth.url", "https://sessionserver.mojang.com/session/minecraft/hasJoined");
    public static final boolean AUTH_PREVENT_PROXY_CONNECTIONS = booleanProperty("minestom.auth.prevent-proxy-connections", false);

    // World
    public static final int WORLD_BORDER_SIZE = intProperty("minestom.world-border-size", 29999984);

    // Maps
    public static final @NotNull String MAP_RGB_MAPPING = stringProperty("minestom.map.rgbmapping", "lazy");
    public static final int MAP_RGB_REDUCTION = intProperty("minestom.map.rgbreduction", -1); // Only used if rgb mapping is "approximate"

    // Entities
    public static final boolean ENFORCE_INTERACTION_LIMIT = booleanProperty("minestom.enforce-entity-interaction-range", true);

    // Experimental/Unstable
    public static final boolean REGISTRY_LATE_REGISTER = booleanProperty("minestom.registry.late-register");
    public static final boolean REGISTRY_UNSAFE_OPS = booleanProperty("minestom.registry.unsafe-ops");
    public static final boolean EVENT_NODE_ALLOW_MULTIPLE_PARENTS = booleanProperty("minestom.event.multiple-parents");

    public static boolean INSIDE_TEST = booleanProperty("minestom.inside-test", false);

    private ServerFlag() {
    }

    private static boolean booleanProperty(String name) {
        return Boolean.getBoolean(name);
    }

    private static boolean booleanProperty(String name, boolean defaultValue) {
        boolean result = defaultValue;
        try {
            final String value = System.getProperty(name);
            if (value != null) result = Boolean.parseBoolean(value);
        } catch (IllegalArgumentException | NullPointerException ignored) {
        }
        return result;
    }

    @Contract("_, null -> null; _, !null -> !null")
    private static String stringProperty(@NotNull String name, @Nullable String defaultValue) {
        return System.getProperty(name, defaultValue);
    }

    private static String stringProperty(@NotNull String name) {
        return System.getProperty(name);
    }

    private static int intProperty(String name, int defaultValue) {
        return Integer.getInteger(name, defaultValue);
    }

    private static long longProperty(String name, long defaultValue) {
        return Long.getLong(name, defaultValue);
    }

    private static Float floatProperty(String name, Float defaultValue) {
        Float result = defaultValue;
        try {
            final String value = System.getProperty(name);
            if (value != null) result = Float.parseFloat(value);
        } catch (IllegalArgumentException | NullPointerException ignored) {
        }
        return result;
    }
}
