package net.minestom.server.entity;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.PlayerLoginEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.utils.thread.MinestomThread;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

public class EntityManager {

    private static InstanceManager instanceManager = MinecraftServer.getInstanceManager();

    private UpdateType updateType = UpdateType.PER_INSTANCE;
    private Set<Instance> instances = instanceManager.getInstances();

    private ExecutorService entitiesPool = new MinestomThread(MinecraftServer.THREAD_COUNT_ENTITIES, MinecraftServer.THREAD_NAME_ENTITIES);
    private ExecutorService playersPool = new MinestomThread(MinecraftServer.THREAD_COUNT_PLAYERS_ENTITIES, MinecraftServer.THREAD_NAME_PLAYERS_ENTITIES);

    private ConcurrentLinkedQueue<Player> waitingPlayers = new ConcurrentLinkedQueue<>();

    public void update() {
        final long time = System.currentTimeMillis();

        // Connect waiting players
        waitingPlayersTick();

        // Update entities
        switch (updateType) {
            case PER_CHUNK:
                chunkUpdate(time);
                break;
            case PER_ENTITY_TYPE:
                entityTypeUpdate(time);
                break;
            case PER_INSTANCE:
                instanceUpdate(time);
                break;
            case SINGLE_THREADED:
                singleThreaded(time);
                break;
        }

    }

    /**
     * Update is chunk based
     *
     * @param time
     */
    private void chunkUpdate(long time) {
        for (Instance instance : instances) {
            for (Chunk chunk : instance.getChunks()) {
                Set<Entity> entities = instance.getChunkEntities(chunk);

                if (!entities.isEmpty()) {
                    entitiesPool.execute(() -> {
                        for (Entity entity : entities) {
                            entity.tick(time);
                        }
                    });
                }
            }
        }

    }

    /**
     * Update each entity type separately independently of their location
     *
     * @param time
     */
    private void entityTypeUpdate(long time) {
        for (Instance instance : instances) {
            Set<Player> players = instance.getPlayers();
            Set<EntityCreature> creatures = instance.getCreatures();
            Set<ObjectEntity> objects = instance.getObjectEntities();

            if (!players.isEmpty()) {
                playersPool.execute(() -> {
                    for (Player player : players) {
                        player.tick(time);
                    }
                });
            }

            if (!creatures.isEmpty() || !objects.isEmpty()) {
                entitiesPool.execute(() -> {
                    for (EntityCreature creature : creatures) {
                        creature.tick(time);
                    }
                    for (ObjectEntity objectEntity : objects) {
                        objectEntity.tick(time);
                    }
                });
            }
        }
    }

    /**
     * Each instance get its pool, should suppress most of the problems related to thread-safety
     *
     * @param time
     */
    private void instanceUpdate(long time) {
        for (Instance instance : instances) {
            Set<Player> players = instance.getPlayers();
            Set<EntityCreature> creatures = instance.getCreatures();
            Set<ObjectEntity> objects = instance.getObjectEntities();

            if (!players.isEmpty() || !creatures.isEmpty() || !objects.isEmpty()) {
                entitiesPool.execute(() -> {
                    for (Player player : players) {
                        player.tick(time);
                    }
                    for (EntityCreature creature : creatures) {
                        creature.tick(time);
                    }
                    for (ObjectEntity objectEntity : objects) {
                        objectEntity.tick(time);
                    }
                });
            }
        }
    }

    /**
     * Single threaded update (like the notchian server)
     *
     * @param time
     */
    private void singleThreaded(long time) {
        for (Instance instance : instances) {
            Set<Player> players = instance.getPlayers();
            Set<EntityCreature> creatures = instance.getCreatures();
            Set<ObjectEntity> objects = instance.getObjectEntities();

            if (!players.isEmpty() || !creatures.isEmpty() || !objects.isEmpty()) {
                for (Player player : players) {
                    player.tick(time);
                }
                for (EntityCreature creature : creatures) {
                    creature.tick(time);
                }
                for (ObjectEntity objectEntity : objects) {
                    objectEntity.tick(time);
                }
            }
        }
    }

    // Add connected clients after the handshake (used to free the networking threads)
    private void waitingPlayersTick() {
        Player waitingPlayer;
        while ((waitingPlayer = waitingPlayers.poll()) != null) {
            final Player playerCache = waitingPlayer;
            playersPool.execute(() -> {
                PlayerLoginEvent loginEvent = new PlayerLoginEvent();
                playerCache.callEvent(PlayerLoginEvent.class, loginEvent);
                Instance spawningInstance = loginEvent.getSpawningInstance();
                if (spawningInstance == null)
                    throw new NullPointerException("You need to specify a spawning instance in the PlayerLoginEvent");

                playerCache.setInstance(spawningInstance);
            });
        }
    }

    public void addWaitingPlayer(Player player) {
        this.waitingPlayers.add(player);
    }

    public UpdateType getUpdateType() {
        return updateType;
    }

    public void setUpdateType(UpdateType updateType) {
        this.updateType = updateType;
    }

    public enum UpdateType {
        PER_CHUNK,
        PER_ENTITY_TYPE,
        PER_INSTANCE,
        SINGLE_THREADED
    }
}
