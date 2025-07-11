package net.minestom.server.monitoring;

import jdk.jfr.*;
import org.jetbrains.annotations.ApiStatus;

/**
 * JFR events for monitoring Minestom server activities.
 */
@ApiStatus.Internal
@SuppressWarnings("ALL")
public final class EventsJFR {
    public static final String SERVER_TICK = "minestom.ServerTickTime";

    public static final String CHUNK_GENERATION = "minestom.ChunkGeneration";
    public static final String CHUNK_LOADING = "minestom.ChunkLoading";

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

    @Name("minestom.InstanceJoin")
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

    @Name("minestom.InstanceLeave")
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
}
