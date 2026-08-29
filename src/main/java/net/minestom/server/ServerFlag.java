package net.minestom.server;

import net.minestom.server.property.ServerProperties;

/**
 * Contains server settings/flags to be set with system properties.
 *
 * <p>Some flags (labeled at the bottom) are experimental. They may be removed without notice, and may have issues.</p>
 */
public final class ServerFlag {

    // Server Behavior
    public static final boolean SHUTDOWN_ON_SIGNAL = ServerProperties.SHUTDOWN_ON_SIGNAL.get();
    public static final int SERVER_TICKS_PER_SECOND = ServerProperties.SERVER_TICKS_PER_SECOND.get();
    public static final int SERVER_MAX_TICK_CATCH_UP = ServerProperties.SERVER_MAX_TICK_CATCH_UP.get();
    public static final int CHUNK_VIEW_DISTANCE = ServerProperties.CHUNK_VIEW_DISTANCE.get();
    public static final int ENTITY_VIEW_DISTANCE = ServerProperties.ENTITY_VIEW_DISTANCE.get();
    public static final int ENTITY_SYNCHRONIZATION_TICKS = ServerProperties.ENTITY_SYNCHRONIZATION_TICKS.get();
    public static final int DISPATCHER_THREADS = ServerProperties.DISPATCHER_THREADS.get();
    public static final int SEND_LIGHT_AFTER_BLOCK_PLACEMENT_DELAY = ServerProperties.SEND_LIGHT_AFTER_BLOCK_PLACEMENT_DELAY.get();
    public static final long LOGIN_PLUGIN_MESSAGE_TIMEOUT = ServerProperties.LOGIN_PLUGIN_MESSAGE_TIMEOUT.get();
    public static final long KNOWN_PACKS_RESPONSE_TIMEOUT = ServerProperties.KNOWN_PACKS_RESPONSE_TIMEOUT.get();
    public static final boolean ACCEPT_TRANSFERS = ServerProperties.ACCEPT_TRANSFERS.get();
    public static final boolean AUTOMATIC_COMPONENT_TRANSLATION = ServerProperties.AUTOMATIC_COMPONENT_TRANSLATION.get();

    // Network rate limiting
    public static final int PLAYER_PACKET_PER_TICK = ServerProperties.PLAYER_PACKET_PER_TICK.get();
    public static final int PLAYER_PACKET_QUEUE_SIZE = ServerProperties.PLAYER_PACKET_QUEUE_SIZE.get();
    public static final long KEEP_ALIVE_DELAY = ServerProperties.KEEP_ALIVE_DELAY.get();
    public static final long KEEP_ALIVE_KICK = ServerProperties.KEEP_ALIVE_KICK.get();
    public static final int PLAYER_CHUNK_UPDATE_LIMITER_HISTORY_SIZE = ServerProperties.PLAYER_CHUNK_UPDATE_LIMITER_HISTORY_SIZE.get();

    // Network buffers
    public static final int MAX_PACKET_SIZE = ServerProperties.MAX_PACKET_SIZE.get();
    public static final int MAX_PACKET_SIZE_PRE_AUTH = ServerProperties.MAX_PACKET_SIZE_PRE_AUTH.get();
    public static final int SOCKET_SEND_BUFFER_SIZE = ServerProperties.SOCKET_SEND_BUFFER_SIZE.get();
    public static final int SOCKET_RECEIVE_BUFFER_SIZE = ServerProperties.SOCKET_RECEIVE_BUFFER_SIZE.get();
    public static final boolean SOCKET_NO_DELAY = ServerProperties.SOCKET_NO_DELAY.get();
    public static final int SOCKET_TIMEOUT = ServerProperties.SOCKET_TIMEOUT.get();
    public static final int POOLED_BUFFER_SIZE = ServerProperties.POOLED_BUFFER_SIZE.get();

    // Chunk update
    public static final float MIN_CHUNKS_PER_TICK = ServerProperties.MIN_CHUNKS_PER_TICK.get();
    public static final float MAX_CHUNKS_PER_TICK = ServerProperties.MAX_CHUNKS_PER_TICK.get();
    public static final float CHUNKS_PER_TICK_MULTIPLIER = ServerProperties.CHUNKS_PER_TICK_MULTIPLIER.get();

    // Packet sending optimizations
    public static final boolean GROUPED_PACKET = ServerProperties.GROUPED_PACKET.get();
    public static final boolean CACHED_PACKET = ServerProperties.CACHED_PACKET.get();
    public static final boolean VIEWABLE_PACKET = ServerProperties.VIEWABLE_PACKET.get();

    // Tags
    public static final boolean TAG_HANDLER_CACHE_ENABLED = ServerProperties.TAG_HANDLER_CACHE_ENABLED.get();
    public static final boolean SERIALIZE_EMPTY_COMPOUND = ServerProperties.SERIALIZE_EMPTY_COMPOUND.get();

    // Online Mode
    public static final String AUTH_URL = ServerProperties.AUTH_URL.get();
    public static final boolean AUTH_PREVENT_PROXY_CONNECTIONS = ServerProperties.AUTH_PREVENT_PROXY_CONNECTIONS.get();

    // World
    public static final int WORLD_BORDER_SIZE = ServerProperties.WORLD_BORDER_SIZE.get();

    // Maps
    public static final String MAP_RGB_MAPPING = ServerProperties.MAP_RGB_MAPPING.get();
    public static final int MAP_RGB_REDUCTION = ServerProperties.MAP_RGB_REDUCTION.get();

    // Entities
    public static final boolean ENFORCE_INTERACTION_LIMIT = ServerProperties.ENFORCE_INTERACTION_LIMIT.get();

    // Experimental/Unstable
    public static final boolean REGISTRY_UNSAFE_OPS = ServerProperties.REGISTRY_UNSAFE_OPS.get();
    public static final boolean EVENT_NODE_ALLOW_MULTIPLE_PARENTS = ServerProperties.EVENT_NODE_ALLOW_MULTIPLE_PARENTS.get();
    public static final boolean FASTER_SOCKET_WRITES = ServerProperties.FASTER_SOCKET_WRITES.get();
    public static final boolean ACQUIRABLE_STRICT = ServerProperties.ACQUIRABLE_STRICT.get();
    public static final boolean UNSAFE_COLLECTIONS = ServerProperties.UNSAFE_COLLECTIONS.get();
    public static final boolean TEMPLATE_COMPILER = ServerProperties.TEMPLATE_COMPILER.get();
    public static final boolean PROXY_PROTOCOL = ServerProperties.PROXY_PROTOCOL.get();
    public static final boolean PROXY_PROTOCOL_REQUIRED = ServerProperties.PROXY_PROTOCOL_REQUIRED.get();
    public static final int NBT_MAX_DEPTH = ServerProperties.NBT_MAX_DEPTH.get();

    @SuppressWarnings("ConstantField") // kept not final for binary compatibility until the next breaking release
    public static boolean INSIDE_TEST = ServerProperties.INSIDE_TEST.get();

    private ServerFlag() {}
}
