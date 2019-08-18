package fr.themode.minestom.entity;

import fr.themode.minestom.Main;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.instance.InstanceManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EntityManager {

    private static InstanceManager instanceManager = Main.getInstanceManager();

    private ExecutorService creaturesPool = Executors.newFixedThreadPool(2);
    private ExecutorService playersPool = Executors.newFixedThreadPool(2);

    public void update() {
        for (Instance instance : instanceManager.getInstances()) {
            // TODO loop chunks and entities on it instead of individual (to have more non-blocking operation)

            // Creatures
            for (EntityCreature creature : instance.getCreatures()) {
                creaturesPool.submit(() -> {
                    boolean shouldRemove = creature.shouldRemove();
                    if (!shouldRemove) {
                        creature.update();
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
                        player.update();
                    }

                    if (player.shouldRemove()) {
                        instance.removeEntity(player);
                    }
                });
            }

        }

    }

}
