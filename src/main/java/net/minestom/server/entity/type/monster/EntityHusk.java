package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityType;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

public class EntityHusk extends EntityZombie {

    public EntityHusk(@NotNull Position spawnPosition) {
        super(EntityType.HUSK, spawnPosition);
    }
    
}
