package net.minestom.server.monitoring;

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Label;
import jdk.jfr.Name;
import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;

/**
 * JFR events for monitoring Minestom server activities.
 */
@ApiStatus.Internal
@SuppressWarnings("ALL")
public final class EventsJFR {
    public static final boolean JFR_AVAILABLE = jfrAvailable();

    private static boolean jfrAvailable() {
        try {
            Class<?> vmClass = Class.forName("org.graalvm.nativeimage.VMRuntime");
            return false;
        } catch (ClassNotFoundException e) {
        }
        try {
            Class.forName("jdk.jfr.Event");
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static final String SERVER_PING = "minestom.ServerPing";
    public static final String SERVER_TICK = "minestom.ServerTickTime";
    public static final String SERVER_IMMUTABLE = "minestom.ServerImmutable";

    public static final String CHUNK_GENERATION = "minestom.ChunkGeneration";
    public static final String CHUNK_LOADING = "minestom.ChunkLoading";

    public static final String INSTANCE_JOIN = "minestom.InstanceJoin";
    public static final String INSTANCE_LEAVE = "minestom.InstanceLeave";

    public static final String PLAYER_JOIN = "minestom.PlayerJoin";
    public static final String PLAYER_LEAVE = "minestom.PlayerLeave";
    public static final String PLAYER_COMMAND = "minestom.PlayerCommand";
    public static final String PLAYER_CHAT = "minestom.PlayerChat";

    public static EventMarker newServerPing(String remoteAddress) {
        return JFR_AVAILABLE ? new ServerPing(remoteAddress) : NO_OP;
    }

    public static EventMarker newServerTick() {
        return JFR_AVAILABLE ? new ServerTick() : NO_OP;
    }

    public static EventMarker newServerImmutable() {
        return JFR_AVAILABLE ? new ServerImmutable() : NO_OP;
    }

    public static EventMarker newChunkGeneration(UUID instance, int chunkX, int chunkZ) {
        return JFR_AVAILABLE ? new ChunkGeneration(instance.toString(), chunkX, chunkZ) : NO_OP;
    }

    public static EventMarker newChunkLoading(UUID instance, Class loader, int chunkX, int chunkZ) {
        return JFR_AVAILABLE ? new ChunkLoading(instance.toString(), loader, chunkX, chunkZ) : NO_OP;
    }

    public static EventMarker newInstanceJoin(UUID entity, UUID instance) {
        return JFR_AVAILABLE ? new InstanceJoin(entity.toString(), instance.toString()) : NO_OP;
    }

    public static EventMarker newInstanceLeave(UUID entity, UUID instance) {
        return JFR_AVAILABLE ? new InstanceLeave(entity.toString(), instance.toString()) : NO_OP;
    }

    public static EventMarker newPlayerJoin(UUID player) {
        return JFR_AVAILABLE ? new PlayerJoin(player.toString()) : NO_OP;
    }

    public static EventMarker newPlayerLeave(UUID player) {
        return JFR_AVAILABLE ? new PlayerLeave(player.toString()) : NO_OP;
    }

    public static EventMarker newPlayerCommand(UUID player, String command) {
        return JFR_AVAILABLE ? new PlayerCommand(player.toString(), command) : NO_OP;
    }

    public static EventMarker newPlayerChat(UUID player, String message) {
        return JFR_AVAILABLE ? new PlayerChat(player.toString(), message) : NO_OP;
    }

    @Name(SERVER_PING)
    @Label("Server Ping")
    @Category({"Minestom", "Server"})
    @Description("A server ping (status query) was received")
    private static final class ServerPing extends JFREventWrapper {
        @Label("Remote Address")
        String remoteAddress;

        private ServerPing(String remoteAddress) {
            this.remoteAddress = remoteAddress;
        }
    }

    @Name(SERVER_TICK)
    @Label("Server Tick")
    @Category({"Minestom", "Server"})
    @Description("Time spent ticking the server once")
    private static final class ServerTick extends JFREventWrapper {
    }

    @Name(SERVER_IMMUTABLE)
    @Label("Server Immutable")
    @Category({"Minestom", "Server"})
    @Description("Called when the server process is frozen")
    private static final class ServerImmutable extends JFREventWrapper {
    }

    @Name(CHUNK_GENERATION)
    @Label("Chunk Generation")
    @Category({"Minestom", "World"})
    @Description("Chunk generation from instances' Generator")
    private static final class ChunkGeneration extends JFREventWrapper {
        @Label("Instance UUID")
        String instance;
        @Label("Chunk X")
        int chunkX;
        @Label("Chunk Z")
        int chunkZ;

        private ChunkGeneration(String instance, int chunkX, int chunkZ) {
            this.instance = instance;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }
    }

    @Name(CHUNK_LOADING)
    @Label("Chunk Loading")
    @Category({"Minestom", "World"})
    @Description("Chunk loading from the instances' IChunkLoader")
    private static final class ChunkLoading extends JFREventWrapper {
        @Label("Instance UUID")
        String instance;
        @Label("Loader Class")
        Class loader;
        @Label("Chunk X")
        int chunkX;
        @Label("Chunk Z")
        int chunkZ;

        private ChunkLoading(String instance, Class loader, int chunkX, int chunkZ) {
            this.instance = instance;
            this.loader = loader;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }
    }

    @Name(INSTANCE_JOIN)
    @Label("Instance Join")
    @Category({"Minestom", "Instance"})
    @Description("An Entity has joined an instance")
    private static final class InstanceJoin extends JFREventWrapper {
        @Label("Entity UUID")
        String entity;
        @Label("Instance UUID")
        String instance;

        private InstanceJoin(String entity, String instance) {
            this.entity = entity;
            this.instance = instance;
        }
    }

    @Name(INSTANCE_LEAVE)
    @Label("Instance Leave")
    @Category({"Minestom", "Instance"})
    @Description("An Entity has left an instance")
    private static final class InstanceLeave extends JFREventWrapper {
        @Label("Entity UUID")
        String entity;
        @Label("Instance UUID")
        String instance;

        private InstanceLeave(String entity, String instance) {
            this.entity = entity;
            this.instance = instance;
        }
    }

    @Name(PLAYER_JOIN)
    @Label("Player Join")
    @Category({"Minestom", "Player"})
    @Description("A player joined the server")
    private static final class PlayerJoin extends JFREventWrapper {
        @Label("Player UUID")
        String player;

        private PlayerJoin(String player) {
            this.player = player;
        }
    }

    @Name(PLAYER_LEAVE)
    @Label("Player Leave")
    @Category({"Minestom", "Player"})
    @Description("A player left the server")
    private static final class PlayerLeave extends JFREventWrapper {
        @Label("Player UUID")
        String player;

        private PlayerLeave(String player) {
            this.player = player;
        }
    }

    @Name(PLAYER_COMMAND)
    @Label("Player Command")
    @Category({"Minestom", "Player"})
    @Description("A player executed a command")
    private static final class PlayerCommand extends JFREventWrapper {
        @Label("Player UUID")
        String player;
        @Label("Command")
        String command;

        private PlayerCommand(String player, String command) {
            this.player = player;
            this.command = command;
        }
    }

    @Name(PLAYER_CHAT)
    @Label("Player Chat")
    @Category({"Minestom", "Player"})
    @Description("A player sent a chat message")
    private static final class PlayerChat extends JFREventWrapper {
        @Label("Player UUID")
        String player;
        @Label("Message")
        String message;

        private PlayerChat(String player, String message) {
            this.player = player;
            this.message = message;
        }
    }

    public interface EventMarker {
        default void begin() {
        }

        default void end() {
        }

        default void commit() {
        }
    }

    private static class JFREventWrapper extends jdk.jfr.Event implements EventMarker {
    }

    private static final EventMarker NO_OP = new NoOpEvent();

    private static class NoOpEvent implements EventMarker {
        @Override
        public void commit() {
            // do nothing
        }
    }
}
