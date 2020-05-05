package net.minestom.server.event;

import net.minestom.server.instance.Instance;

public class PlayerSpawnEvent extends EntitySpawnEvent {
    public PlayerSpawnEvent(Instance spawnInstance) {
        super(spawnInstance);
    }
}
