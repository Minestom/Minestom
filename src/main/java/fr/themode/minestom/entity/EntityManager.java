package fr.themode.minestom.entity;

import fr.themode.minestom.Main;
import fr.themode.minestom.event.PlayerLoginEvent;
import fr.themode.minestom.event.PlayerSpawnEvent;
import fr.themode.minestom.instance.Chunk;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.instance.InstanceManager;
import fr.themode.minestom.utils.ChunkUtils;
import fr.themode.minestom.utils.Position;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class EntityManager {

    private static InstanceManager instanceManager = Main.getInstanceManager();

    private ExecutorService entitiesPool = Executors.newFixedThreadPool(Main.THREAD_COUNT_ENTITIES);
    private ExecutorService playersPool = Executors.newFixedThreadPool(Main.THREAD_COUNT_PLAYERS_ENTITIES);

    private ConcurrentLinkedQueue<Player> waitingPlayers = new ConcurrentLinkedQueue<>();

    public void update() {
        waitingPlayersTick();
        for (Instance instance : instanceManager.getInstances()) {
            testTick2(instance);
        }

    }

    private void waitingPlayersTick() {
        Player waitingPlayer;
        while ((waitingPlayer = waitingPlayers.poll()) != null) {
            final Player playerCache = waitingPlayer;
            playersPool.execute(() -> {
                PlayerLoginEvent loginEvent = new PlayerLoginEvent();
                playerCache.callEvent(PlayerLoginEvent.class, loginEvent);
                Instance spawningInstance = loginEvent.getSpawningInstance() == null ? instanceManager.createInstanceContainer() : loginEvent.getSpawningInstance();
                Position position = playerCache.getPosition();

                long[] visibleChunks = ChunkUtils.getVisibleChunks(position);
                for (int i = 0; i < visibleChunks.length; i++) {
                    int[] chunkPos = ChunkUtils.getChunkCoord(visibleChunks[i]);
                    int chunkX = chunkPos[0];
                    int chunkZ = chunkPos[1];
                    boolean isLast = i == visibleChunks.length - 1;
                    Consumer<Chunk> callback = isLast ? chunk -> {
                        System.out.println("END CHUNK LOADING");
                        playerCache.spawned = true;
                        playerCache.setInstance(spawningInstance);
                        PlayerSpawnEvent spawnEvent = new PlayerSpawnEvent();
                        playerCache.callEvent(PlayerSpawnEvent.class, spawnEvent);
                    } : null;
                    spawningInstance.loadChunk(chunkX, chunkZ, callback); // TODO loadOptionalChunk for not loading chunks when autoload is false
                }

            });
        }
    }

    // TODO optimize for when there are too many entities on one chunk
    private void testTick2(Instance instance) {

        for (Chunk chunk : instance.getChunks()) {
            Set<Entity> entities = instance.getChunkEntities(chunk);

            if (!entities.isEmpty()) {
                entitiesPool.execute(() -> {
                    for (Entity entity : entities) {
                        entity.tick();
                    }
                });
            }
        }
    }

    private void testTick1(Instance instance) {
        Set<ObjectEntity> objects = instance.getObjectEntities();
        Set<EntityCreature> creatures = instance.getCreatures();
        Set<Player> players = instance.getPlayers();

        if (!creatures.isEmpty() || !objects.isEmpty()) {
            entitiesPool.execute(() -> {
                for (EntityCreature creature : creatures) {
                    creature.tick();
                }
                for (ObjectEntity objectEntity : objects) {
                    objectEntity.tick();
                }
            });
        }

        if (!players.isEmpty()) {
            playersPool.execute(() -> {
                for (Player player : players) {
                    player.tick();
                }
            });
        }
    }

    public void addWaitingPlayer(Player player) {
        this.waitingPlayers.add(player);
    }

}
