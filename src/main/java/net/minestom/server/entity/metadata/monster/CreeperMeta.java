package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public final class CreeperMeta extends MonsterMeta {
    public CreeperMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public State getState() {
        int id = get(MetadataDef.Creeper.STATE);
        return id == -1 ? State.IDLE : State.FUSE;
    }

    public void setState(State value) {
        set(MetadataDef.Creeper.STATE, value == State.IDLE ? -1 : 1);
    }

    public boolean isCharged() {
        return get(MetadataDef.Creeper.IS_CHARGED);
    }

    public void setCharged(boolean value) {
        set(MetadataDef.Creeper.IS_CHARGED, value);
    }

    public boolean isIgnited() {
        return get(MetadataDef.Creeper.IS_IGNITED);
    }

    public void setIgnited(boolean value) {
        set(MetadataDef.Creeper.IS_IGNITED, value);
    }

    public enum State {
        IDLE,
        FUSE
    }

}
