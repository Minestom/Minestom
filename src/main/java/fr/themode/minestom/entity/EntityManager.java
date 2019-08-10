package fr.themode.minestom.entity;

import fr.themode.minestom.Main;
import fr.themode.minestom.net.ConnectionManager;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EntityManager {

    private Set<EntityCreature> creatures = Collections.synchronizedSet(new HashSet<>());

    private ExecutorService creaturesPool = Executors.newFixedThreadPool(3);
    private ExecutorService playersPool = Executors.newFixedThreadPool(2);

    private ConnectionManager connectionManager = Main.getConnectionManager();

    public void update() {
        creatures.removeIf(creature -> creature.shouldRemove());

        synchronized (creatures) {
            Iterator<EntityCreature> iterator = creatures.iterator();
            while (iterator.hasNext()) {
                EntityCreature creature = iterator.next();
                creaturesPool.submit(creature::update);
            }
        }

        Collection<Player> players = connectionManager.getOnlinePlayers();
        Iterator<Player> iterator = players.iterator();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            playersPool.submit(player::update);
        }

    }

    public Set<EntityCreature> getCreatures() {
        return Collections.unmodifiableSet(creatures);
    }

    protected void addCreature(EntityCreature creature) {
        this.creatures.add(creature);
    }
}
