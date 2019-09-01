package fr.themode.minestom.event;

import fr.themode.minestom.instance.Instance;

public class PlayerSpawnEvent extends Event {

    private Instance spawnInstance;

    public PlayerSpawnEvent(Instance spawnInstance) {
        this.spawnInstance = spawnInstance;
    }

    public Instance getSpawnInstance() {
        return spawnInstance;
    }
}
