package fr.themode.minestom.entity;

import fr.themode.minestom.Main;
import fr.themode.minestom.event.PlayerLoginEvent;
import fr.themode.minestom.event.PlayerSpawnPacket;
import fr.themode.minestom.instance.Chunk;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.instance.InstanceManager;
import fr.themode.minestom.utils.GroupedCollections;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EntityManager {

    private static InstanceManager instanceManager = Main.getInstanceManager();

    private ExecutorService entitiesPool = Executors.newFixedThreadPool(Main.THREAD_COUNT_ENTITIES);
    private ExecutorService playersPool = Executors.newFixedThreadPool(Main.THREAD_COUNT_PLAYERS_ENTITIES);

    private ConcurrentLinkedQueue<Player> waitingPlayers = new ConcurrentLinkedQueue<>();

    public void update() {
        waitingPlayersTick();
        for (Instance instance : instanceManager.getInstances()) {
            // TODO optimize update engine for when there are too many entities on one chunk
            testTick1(instance);
        }

    }

    private void waitingPlayersTick() {
        Player waitingPlayer;
        while ((waitingPlayer = waitingPlayers.poll()) != null) {
            final Player playerCache = waitingPlayer;
            playersPool.execute(() -> {
                PlayerLoginEvent loginEvent = new PlayerLoginEvent();
                playerCache.callEvent(PlayerLoginEvent.class, loginEvent);
                Instance spawningInstance = loginEvent.getSpawningInstance() == null ? instanceManager.createInstance() : loginEvent.getSpawningInstance();
                // TODO load multiple chunks around player (based on view distance) instead of only one
                spawningInstance.loadChunk(playerCache.getPosition(), chunk -> {
                    playerCache.spawned = true;
                    playerCache.setInstance(spawningInstance);
                    PlayerSpawnPacket spawnPacket = new PlayerSpawnPacket();
                    playerCache.callEvent(PlayerSpawnPacket.class, spawnPacket);
                });
            });
        }
    }

    private void testTick2(Instance instance) {

        for (Chunk chunk : instance.getChunks()) {
            Set<ObjectEntity> objects = chunk.getObjectEntities();
            Set<EntityCreature> creatures = chunk.getCreatures();
            Set<Player> players = chunk.getPlayers();

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
    }

    private void testTick1(Instance instance) {
        GroupedCollections<ObjectEntity> objects = instance.getObjectEntities();
        GroupedCollections<EntityCreature> creatures = instance.getCreatures();
        GroupedCollections<Player> players = instance.getPlayers();

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
