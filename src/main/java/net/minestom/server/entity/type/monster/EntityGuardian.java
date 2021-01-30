package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.Monster;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

public class EntityGuardian extends EntityCreature implements Monster {

    private Entity target;

    public EntityGuardian(Position spawnPosition) {
        super(EntityType.GUARDIAN, spawnPosition);
        setBoundingBox(0.85f, 0.85f, 0.85f);
    }

    public boolean isRetractingSpikes() {
        return metadata.getIndex((byte) 15, false);
    }

    public void setRetractingSpikes(boolean retractingSpikes) {
        this.metadata.setIndex((byte) 15, Metadata.Boolean(retractingSpikes));
    }

    public Entity getTarget() {
        return target;
    }

    public void setTarget(@NotNull Entity target) {
        this.target = target;
        this.metadata.setIndex((byte) 16, Metadata.VarInt(target.getEntityId()));
    }
}
