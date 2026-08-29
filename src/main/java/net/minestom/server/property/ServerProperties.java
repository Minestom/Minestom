package net.minestom.server.property;

import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The server settings, each initialized from the system property it is named after.
 *
 * <p>Read a setting with {@link ServerProperty#get()}. Each one is immutable by default, so its
 * value is fixed when this class is initialized and reads fold away to a constant. Configure a
 * server through the system properties below, or make a single setting writable at runtime with
 * {@code <name>.mutable=true}.</p>
 *
 * <p>{@code minestom.properties.mutable} moves that default for every setting at once and by default is {@code false}.</p>
 */
public final class ServerProperties {

    // Server Behavior
    public static final ServerProperty<Boolean> SHUTDOWN_ON_SIGNAL = Boolean("minestom.shutdown-on-signal", true);
    public static final ServerProperty<Integer> SERVER_TICKS_PER_SECOND = Integer("minestom.tps", 20);
    public static final ServerProperty<Integer> SERVER_MAX_TICK_CATCH_UP = Integer("minestom.max-tick-catch-up", 5);
    public static final ServerProperty<Integer> CHUNK_VIEW_DISTANCE = Integer("minestom.chunk-view-distance", 8); // Base chunk view distance of instances and client settings
    public static final ServerProperty<Integer> ENTITY_VIEW_DISTANCE = Integer("minestom.entity-view-distance", 5);
    public static final ServerProperty<Integer> ENTITY_SYNCHRONIZATION_TICKS = Integer("minestom.entity-synchronization-ticks", 20);
    public static final ServerProperty<Integer> DISPATCHER_THREADS = Integer("minestom.dispatcher-threads", 1);
    public static final ServerProperty<Integer> SEND_LIGHT_AFTER_BLOCK_PLACEMENT_DELAY = Integer("minestom.send-light-after-block-placement-delay", 100);
    public static final ServerProperty<Long> LOGIN_PLUGIN_MESSAGE_TIMEOUT = Long("minestom.login-plugin-message-timeout", 5_000); // 5s
    public static final ServerProperty<Long> KNOWN_PACKS_RESPONSE_TIMEOUT = Long("minestom.known-packs-response-timeout", 5 * 60_000); // 5m
    public static final ServerProperty<Boolean> ACCEPT_TRANSFERS = Boolean("minestom.accept-transfers", false);
    public static final ServerProperty<Boolean> AUTOMATIC_COMPONENT_TRANSLATION = Boolean("minestom.automatic-component-translation", false);

    // Network rate limiting
    public static final ServerProperty<Integer> PLAYER_PACKET_PER_TICK = Integer("minestom.packet-per-tick", 50);
    public static final ServerProperty<Integer> PLAYER_PACKET_QUEUE_SIZE = Integer("minestom.packet-queue-size", 1000);
    public static final ServerProperty<Long> KEEP_ALIVE_DELAY = Long("minestom.keep-alive-delay", 10_000);
    public static final ServerProperty<Long> KEEP_ALIVE_KICK = Long("minestom.keep-alive-kick", 15_000);
    public static final ServerProperty<Integer> PLAYER_CHUNK_UPDATE_LIMITER_HISTORY_SIZE = Integer("minestom.player.chunk-update-limiter-history-size", 5, 0, Integer.MAX_VALUE);

    // Network buffers
    public static final ServerProperty<Integer> MAX_PACKET_SIZE = Integer("minestom.max-packet-size", 2_097_151); // 3 bytes var-int
    public static final ServerProperty<Integer> MAX_PACKET_SIZE_PRE_AUTH = Integer("minestom.max-packet-size-pre-auth", 8_192);
    public static final ServerProperty<Integer> SOCKET_SEND_BUFFER_SIZE = Integer("minestom.send-buffer-size", 262_143);
    public static final ServerProperty<Integer> SOCKET_RECEIVE_BUFFER_SIZE = Integer("minestom.receive-buffer-size", 32_767);
    public static final ServerProperty<Boolean> SOCKET_NO_DELAY = Boolean("minestom.tcp-no-delay", true);
    public static final ServerProperty<Integer> SOCKET_TIMEOUT = Integer("minestom.socket-timeout", 15_000);
    public static final ServerProperty<Integer> POOLED_BUFFER_SIZE = Integer("minestom.pooled-buffer-size", 16_383);

    // Chunk update
    public static final ServerProperty<Float> MIN_CHUNKS_PER_TICK = Float("minestom.chunk-queue.min-per-tick", 0.01f);
    public static final ServerProperty<Float> MAX_CHUNKS_PER_TICK = Float("minestom.chunk-queue.max-per-tick", 64.0f);
    public static final ServerProperty<Float> CHUNKS_PER_TICK_MULTIPLIER = Float("minestom.chunk-queue.multiplier", 1f);

    // Packet sending optimizations
    public static final ServerProperty<Boolean> GROUPED_PACKET = Boolean("minestom.grouped-packet", true);
    public static final ServerProperty<Boolean> CACHED_PACKET = Boolean("minestom.cached-packet", true);
    public static final ServerProperty<Boolean> VIEWABLE_PACKET = Boolean("minestom.viewable-packet", true);

    // Tags
    public static final ServerProperty<Boolean> TAG_HANDLER_CACHE_ENABLED = Boolean("minestom.tag-handler-cache", true);
    public static final ServerProperty<Boolean> SERIALIZE_EMPTY_COMPOUND = Boolean("minestom.serialization.serialize-empty-nbt-compound", false);

    // Online Mode
    public static final ServerProperty<String> AUTH_URL = String("minestom.auth.url", "https://sessionserver.mojang.com/session/minecraft/hasJoined");
    public static final ServerProperty<Boolean> AUTH_PREVENT_PROXY_CONNECTIONS = Boolean("minestom.auth.prevent-proxy-connections", false);

    // World
    public static final ServerProperty<Integer> WORLD_BORDER_SIZE = Integer("minestom.world-border-size", 29999984);

    // Maps
    public static final ServerProperty<String> MAP_RGB_MAPPING = String("minestom.map.rgbmapping", "lazy");
    public static final ServerProperty<Integer> MAP_RGB_REDUCTION = Integer("minestom.map.rgbreduction", -1); // Only used if rgb mapping is "approximate"

    // Entities
    public static final ServerProperty<Boolean> ENFORCE_INTERACTION_LIMIT = Boolean("minestom.enforce-entity-interaction-range", true);

    // Testing
    public static final ServerProperty<Boolean> INSIDE_TEST = Boolean("minestom.inside-test", false);

    // Experimental/Unstable
    @ApiStatus.Experimental
    public static final ServerProperty<Boolean> REGISTRY_UNSAFE_OPS = Boolean("minestom.registry.unsafe-ops", false);
    @ApiStatus.Experimental
    public static final ServerProperty<Boolean> EVENT_NODE_ALLOW_MULTIPLE_PARENTS = Boolean("minestom.event.multiple-parents", false);
    @ApiStatus.Experimental
    public static final ServerProperty<Boolean> FASTER_SOCKET_WRITES = Boolean("minestom.new-socket-write-lock", false); // TODO: promote to default
    @ApiStatus.Experimental
    public static final ServerProperty<Boolean> ACQUIRABLE_STRICT = Boolean("minestom.acquirable-strict", false);
    @ApiStatus.Experimental
    public static final ServerProperty<Boolean> UNSAFE_COLLECTIONS = Boolean("minestom.unsafe-collections", false); // Likely to be removed in the future
    @ApiStatus.Experimental
    public static final ServerProperty<Boolean> TEMPLATE_COMPILER = Boolean("minestom.template-compiler", false);
    @ApiStatus.Experimental
    public static final ServerProperty<Boolean> PROXY_PROTOCOL = Boolean("minestom.proxy-protocol", false);
    @ApiStatus.Experimental
    public static final ServerProperty<Boolean> PROXY_PROTOCOL_REQUIRED = Boolean("minestom.proxy-protocol.required", false);
    @ApiStatus.Experimental
    public static final ServerProperty<Integer> NBT_MAX_DEPTH = Integer("minestom.nbt.max-depth", 512, 1, Integer.MAX_VALUE); // Binary tags are read and written recursively, so raising this can overflow the java stack

    private ServerProperties() {}

    static ServerProperty<Boolean> Boolean(String name, boolean defaultValue) {
        return ServerPropertyImpl.create(name, defaultValue, Boolean::parseBoolean);
    }

    static ServerProperty<Byte> Byte(String name, byte defaultValue) {
        return ServerPropertyImpl.create(name, defaultValue, Byte::parseByte);
    }

    static ServerProperty<Byte> Byte(String name, byte defaultValue, byte minValue, byte maxValue) {
        return ServerPropertyImpl.create(name, defaultValue, Byte::parseByte, range(name, minValue, maxValue));
    }

    static ServerProperty<Short> Short(String name, short defaultValue) {
        return ServerPropertyImpl.create(name, defaultValue, Short::parseShort);
    }

    static ServerProperty<Short> Short(String name, short defaultValue, short minValue, short maxValue) {
        return ServerPropertyImpl.create(name, defaultValue, Short::parseShort, range(name, minValue, maxValue));
    }

    static ServerProperty<Integer> Integer(String name, int defaultValue) {
        return ServerPropertyImpl.create(name, defaultValue, Integer::parseInt);
    }

    static ServerProperty<Integer> Integer(String name, int defaultValue, int minValue, int maxValue) {
        return ServerPropertyImpl.create(name, defaultValue, Integer::parseInt, range(name, minValue, maxValue));
    }

    static ServerProperty<Long> Long(String name, long defaultValue) {
        return ServerPropertyImpl.create(name, defaultValue, Long::parseLong);
    }

    static ServerProperty<Long> Long(String name, long defaultValue, long minValue, long maxValue) {
        return ServerPropertyImpl.create(name, defaultValue, Long::parseLong, range(name, minValue, maxValue));
    }

    static ServerProperty<Float> Float(String name, float defaultValue) {
        return ServerPropertyImpl.create(name, defaultValue, Float::parseFloat);
    }

    static ServerProperty<Float> Float(String name, float defaultValue, float minValue, float maxValue) {
        return ServerPropertyImpl.create(name, defaultValue, Float::parseFloat, range(name, minValue, maxValue));
    }

    static ServerProperty<Double> Double(String name, double defaultValue) {
        return ServerPropertyImpl.create(name, defaultValue, Double::parseDouble);
    }

    static ServerProperty<Double> Double(String name, double defaultValue, double minValue, double maxValue) {
        return ServerPropertyImpl.create(name, defaultValue, Double::parseDouble, range(name, minValue, maxValue));
    }

    static ServerProperty<String> String(String name, String defaultValue) {
        return ServerPropertyImpl.create(name, defaultValue, Function.identity());
    }

    private static <T extends Comparable<T>> Consumer<T> range(String name, T minValue, T maxValue) {
        return value -> {
            if (value.compareTo(minValue) < 0 || value.compareTo(maxValue) > 0) {
                throw new IllegalArgumentException(String.format(
                        "Property '%s' value must be in range [%s..%s] but was %s",
                        name, minValue, maxValue, value
                ));
            }
        };
    }
}
