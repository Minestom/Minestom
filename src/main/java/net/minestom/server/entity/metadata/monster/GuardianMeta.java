package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class GuardianMeta extends MonsterMeta {

    private Entity target;

    public GuardianMeta(@NotNull Entity entity) {
        super(entity);
    }

    public boolean isRetractingSpikes() {
        return getMetadata().getIndex((byte) 15, false);
    }

    public void setRetractingSpikes(boolean retractingSpikes) {
        getMetadata().setIndex((byte) 15, Metadata.Boolean(retractingSpikes));
    }

    public Entity getTarget() {
        return this.target;
    }

    public void setTarget(@NotNull Entity target) {
        this.target = target;
        getMetadata().setIndex((byte) 16, Metadata.VarInt(target.getEntityId()));
    }

}
