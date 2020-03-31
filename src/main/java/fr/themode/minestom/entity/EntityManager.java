package fr.themode.minestom.entity;

import fr.themode.minestom.MinecraftServer;
import fr.themode.minestom.event.PlayerLoginEvent;
import fr.themode.minestom.instance.Chunk;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.instance.InstanceManager;
import fr.themode.minestom.utils.thread.MinestomThread;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

public class EntityManager {

    private static InstanceManager instanceManager = MinecraftServer.getInstanceManager();

    private UpdateType updateType = UpdateType.PER_INSTANCE;
    private Set<Instance> instances = instanceManager.getInstances();

    private ExecutorService entitiesPool = new MinestomThread(MinecraftServer.THREAD_COUNT_ENTITIES, "Ms-EntitiesPool");
    private ExecutorService playersPool = new MinestomThread(MinecraftServer.THREAD_COUNT_PLAYERS_ENTITIES, "Ms-PlayersPool");

    private ConcurrentLinkedQueue<Player> waitingPlayers = new ConcurrentLinkedQueue<>();

    public void update() {
        final long time = System.currentTimeMillis();

        // Connect waiting players
        waitingPlayersTick();

        // Update entities
        switch (updateType) {
            case PER_CHUNK:
                chunkUpdate(instances, time);
                break;
            case PER_ENTITY_TYPE:
                entityTypeUpdate(instances, time);
                break;
            case PER_INSTANCE:
                instanceUpdate(instances, time);
                break;
            case SINGLE_THREADED:
                singleThreaded(instances, time);
                break;
        }

    }

    /**
     * Update is chunk based
     *
     * @param time
     */
    private void chunkUpdate(Set<Instance> instances, long time) {
        // TODO optimize for when there are too many entities on one chunk
        for (Instance instance : instanceManager.getInstances()) {
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
    private void entityTypeUpdate(Set<Instance> instances, long time) {
        for (Instance instance : instanceManager.getInstances()) {
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
     * @param instances
     * @param time
     */
    private void instanceUpdate(Set<Instance> instances, long time) {
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
     * @param instances
     * @param time
     */
    private void singleThreaded(Set<Instance> instances, long time) {
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
                Instance spawningInstance = loginEvent.getSpawningInstance() == null ? instanceManager.createInstanceContainer() : loginEvent.getSpawningInstance();

                playerCache.setInstance(spawningInstance);
            });
        }
    }

    public UpdateType getUpdateType() {
        return updateType;
    }

    public void addWaitingPlayer(Player player) {
        this.waitingPlayers.add(player);
    }

    public void setUpdateType(UpdateType updateType) {
        this.updateType = updateType;
    }

    public enum UpdateType {
        PER_CHUNK,
        PER_ENTITY_TYPE,
        PER_INSTANCE,
        SINGLE_THREADED;
    }
}
