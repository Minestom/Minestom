package net.minestom.server.event.player;

import net.minestom.server.event.entity.EntitySpawnEvent;
import net.minestom.server.instance.Instance;

public class PlayerSpawnEvent extends EntitySpawnEvent {
    public PlayerSpawnEvent(Instance spawnInstance) {
        super(spawnInstance);
    }
}
