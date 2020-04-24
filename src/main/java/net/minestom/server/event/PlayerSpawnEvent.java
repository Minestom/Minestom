package net.minestom.server.event;

import net.minestom.server.instance.Instance;

public class PlayerSpawnEvent extends Event {

    private Instance spawnInstance;

    public PlayerSpawnEvent(Instance spawnInstance) {
        this.spawnInstance = spawnInstance;
    }

    public Instance getSpawnInstance() {
        return spawnInstance;
    }
}
