package net.minestom.server.monitoring;

import jdk.jfr.*;
import org.jetbrains.annotations.ApiStatus;

/**
 * JFR events for monitoring Minestom server activities.
 */
@ApiStatus.Internal
@SuppressWarnings("ALL")
public final class EventsJFR {
    public static final String SERVER_PING = "minestom.ServerPing";
    public static final String SERVER_TICK = "minestom.ServerTickTime";

    public static final String CHUNK_GENERATION = "minestom.ChunkGeneration";
    public static final String CHUNK_LOADING = "minestom.ChunkLoading";

    public static final String INSTANCE_JOIN = "minestom.InstanceJoin";
    public static final String INSTANCE_LEAVE = "minestom.InstanceLeave";

    public static final String INSTANCE_SET_BATCH = "minestom.InstanceSetBatch";
    public static final String INSTANCE_GET_BATCH = "minestom.InstanceGetBatch";

    public static final String PLAYER_JOIN = "minestom.PlayerJoin";
    public static final String PLAYER_LEAVE = "minestom.PlayerLeave";
    public static final String PLAYER_COMMAND = "minestom.PlayerCommand";
    public static final String PLAYER_CHAT = "minestom.PlayerChat";

    @Name(SERVER_PING)
    @Label("Server Ping")
    @Category({"Minestom", "Server"})
    @Description("A server ping (status query) was received")
    public static final class ServerPing extends Event {
        @Label("Remote Address")
        String remoteAddress;

        public ServerPing(String remoteAddress) {
            this.remoteAddress = remoteAddress;
        }
    }

    @Name(SERVER_TICK)
    @Label("Server Tick")
    @Category({"Minestom", "Server"})
    @Description("Time spent ticking the server once")
    public static final class ServerTick extends Event {
    }

    @Name(CHUNK_GENERATION)
    @Label("Chunk Generation")
    @Category({"Minestom", "World"})
    @Description("Chunk generation from instances' Generator")
    public static final class ChunkGeneration extends Event {
        @Label("Instance UUID")
        String instance;
        @Label("Chunk X")
        int chunkX;
        @Label("Chunk Z")
        int chunkZ;

        public ChunkGeneration(String instance, int chunkX, int chunkZ) {
            this.instance = instance;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }
    }

    @Name(CHUNK_LOADING)
    @Label("Chunk Loading")
    @Category({"Minestom", "World"})
    @Description("Chunk loading from the instances' IChunkLoader")
    public static final class ChunkLoading extends Event {
        @Label("Instance UUID")
        String instance;
        @Label("Loader Class")
        Class loader;
        @Label("Chunk X")
        int chunkX;
        @Label("Chunk Z")
        int chunkZ;

        public ChunkLoading(String instance, Class loader, int chunkX, int chunkZ) {
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
    public static final class InstanceJoin extends Event {
        @Label("Entity UUID")
        String entity;
        @Label("Instance UUID")
        String instance;

        public InstanceJoin(String entity, String instance) {
            this.entity = entity;
            this.instance = instance;
        }
    }

    @Name(INSTANCE_LEAVE)
    @Label("Instance Leave")
    @Category({"Minestom", "Instance"})
    @Description("An Entity has left an instance")
    public static final class InstanceLeave extends Event {
        @Label("Entity UUID")
        String entity;
        @Label("Instance UUID")
        String instance;

        public InstanceLeave(String entity, String instance) {
            this.entity = entity;
            this.instance = instance;
        }
    }

    @Name(INSTANCE_SET_BATCH)
    @Label("Instance Set Batch")
    @Category({"Minestom", "Instance"})
    @Description("A batch has been placed into an instance")
    public static final class InstanceSetBatch extends Event {
        String instance;
        String origin;
        long batchFlags;
        int batchCount;

        public InstanceSetBatch(String instance, String origin, long batchFlags, int batchCount) {
            this.instance = instance;
            this.origin = origin;
            this.batchFlags = batchFlags;
            this.batchCount = batchCount;
        }
    }

    @Name(INSTANCE_GET_BATCH)
    @Label("Instance Get Batch")
    @Category({"Minestom", "Instance"})
    @Description("A batch has been retrieved from an instance")
    public static final class InstanceGetBatch extends Event {
        String instance;
        String origin;
        long batchFlags;
        public int batchCount;

        public InstanceGetBatch(String instance, String origin, long batchFlags, int batchCount) {
            this.instance = instance;
            this.origin = origin;
            this.batchFlags = batchFlags;
            this.batchCount = batchCount;
        }
    }

    @Name(PLAYER_JOIN)
    @Label("Player Join")
    @Category({"Minestom", "Player"})
    @Description("A player joined the server")
    public static final class PlayerJoin extends Event {
        @Label("Player UUID")
        String player;

        public PlayerJoin(String player) {
            this.player = player;
        }
    }

    @Name(PLAYER_LEAVE)
    @Label("Player Leave")
    @Category({"Minestom", "Player"})
    @Description("A player left the server")
    public static final class PlayerLeave extends Event {
        @Label("Player UUID")
        String player;

        public PlayerLeave(String player) {
            this.player = player;
        }
    }

    @Name(PLAYER_COMMAND)
    @Label("Player Command")
    @Category({"Minestom", "Player"})
    @Description("A player executed a command")
    public static final class PlayerCommand extends Event {
        @Label("Player UUID")
        String player;
        @Label("Command")
        String command;

        public PlayerCommand(String player, String command) {
            this.player = player;
            this.command = command;
        }
    }

    @Name(PLAYER_CHAT)
    @Label("Player Chat")
    @Category({"Minestom", "Player"})
    @Description("A player sent a chat message")
    public static final class PlayerChat extends Event {
        @Label("Player UUID")
        String player;
        @Label("Message")
        String message;

        public PlayerChat(String player, String message) {
            this.player = player;
            this.message = message;
        }
    }
}
