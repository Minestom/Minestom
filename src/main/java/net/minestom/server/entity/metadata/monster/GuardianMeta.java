package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class GuardianMeta extends MonsterMeta {

    private Entity target;

    public GuardianMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isRetractingSpikes() {
        return super.metadata.getIndex((byte) 15, false);
    }

    public void setRetractingSpikes(boolean retractingSpikes) {
        super.metadata.setIndex((byte) 15, Metadata.Boolean(retractingSpikes));
    }

    public Entity getTarget() {
        return this.target;
    }

    public void setTarget(@NotNull Entity target) {
        this.target = target;
        super.metadata.setIndex((byte) 16, Metadata.VarInt(target.getEntityId()));
    }

}
