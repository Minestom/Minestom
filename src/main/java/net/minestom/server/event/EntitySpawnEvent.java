package net.minestom.server.event;

import net.minestom.server.instance.Instance;

public class EntitySpawnEvent extends Event {

    private Instance spawnInstance;

    public EntitySpawnEvent(Instance spawnInstance) {
        this.spawnInstance = spawnInstance;
    }

    public Instance getSpawnInstance() {
        return spawnInstance;
    }

}
