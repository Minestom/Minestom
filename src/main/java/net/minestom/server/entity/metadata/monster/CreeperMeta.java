package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class CreeperMeta extends MonsterMeta {
    public CreeperMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    @NotNull
    public State getState() {
        int id = metadata.get(MetadataDef.Creeper.STATE);
        return id == -1 ? State.IDLE : State.FUSE;
    }

    public void setState(@NotNull State value) {
        metadata.set(MetadataDef.Creeper.STATE, value == State.IDLE ? -1 : 1);
    }

    public boolean isCharged() {
        return metadata.get(MetadataDef.Creeper.IS_CHARGED);
    }

    public void setCharged(boolean value) {
        metadata.set(MetadataDef.Creeper.IS_CHARGED, value);
    }

    public boolean isIgnited() {
        return metadata.get(MetadataDef.Creeper.IS_IGNITED);
    }

    public void setIgnited(boolean value) {
        metadata.set(MetadataDef.Creeper.IS_IGNITED, value);
    }

    public enum State {
        IDLE,
        FUSE
    }

}
