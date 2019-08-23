package fr.themode.minestom.entity;

import fr.themode.minestom.Main;
import fr.themode.minestom.instance.Chunk;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.instance.InstanceManager;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EntityManager {

    private static InstanceManager instanceManager = Main.getInstanceManager();

    private ExecutorService objectsPool = Executors.newFixedThreadPool(Main.THREAD_COUNT_OBJECTS_ENTITIES);
    private ExecutorService creaturesPool = Executors.newFixedThreadPool(Main.THREAD_COUNT_CREATURES_ENTITIES);
    private ExecutorService playersPool = Executors.newFixedThreadPool(Main.THREAD_COUNT_PLAYERS_ENTITIES);

    // TODO API for custom thread division (
    public void update() {
        for (Instance instance : instanceManager.getInstances()) {
            testTick2(instance); // TODO optimize update engine for when there are too many entities on one chunk
        }

    }

    private void testTick2(Instance instance) {
        for (Chunk chunk : instance.getChunks()) {
            Set<ObjectEntity> objects = chunk.getObjectEntities();
            Set<EntityCreature> creatures = chunk.getCreatures();
            Set<Player> players = chunk.getPlayers();

            if (!objects.isEmpty()) {
                objectsPool.submit(() -> {
                    for (ObjectEntity objectEntity : objects) {
                        boolean shouldRemove = objectEntity.shouldRemove();
                        if (!shouldRemove) {
                            objectEntity.tick();
                        }

                        if (objectEntity.shouldRemove()) {
                            instance.removeEntity(objectEntity);
                        }
                    }
                });
            }

            if (!creatures.isEmpty()) {
                creaturesPool.submit(() -> {
                    for (EntityCreature creature : creatures) {
                        boolean shouldRemove = creature.shouldRemove();
                        if (!shouldRemove) {
                            creature.tick();
                        }

                        if (creature.shouldRemove()) {
                            instance.removeEntity(creature);
                        }
                    }
                });
            }

            if (!players.isEmpty()) {
                playersPool.submit(() -> {
                    for (Player player : players) {
                        boolean shouldRemove = player.shouldRemove();
                        if (!shouldRemove) {
                            player.tick();
                        }

                        if (player.shouldRemove()) {
                            instance.removeEntity(player);
                        }
                    }
                });
            }
        }
    }

    private void testTick(Instance instance) {
        // Creatures
        for (EntityCreature creature : instance.getCreatures()) {
            creaturesPool.submit(() -> {
                boolean shouldRemove = creature.shouldRemove();
                if (!shouldRemove) {
                    creature.tick();
                }

                if (creature.shouldRemove()) {
                    instance.removeEntity(creature);
                }
            });
        }

        // Players
        for (Player player : instance.getPlayers()) {
            playersPool.submit(() -> {
                boolean shouldRemove = player.shouldRemove();
                if (!shouldRemove) {
                    player.tick();
                }

                if (player.shouldRemove()) {
                    instance.removeEntity(player);
                }
            });
        }
    }

}
