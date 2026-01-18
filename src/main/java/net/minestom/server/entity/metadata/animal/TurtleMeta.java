package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public final class TurtleMeta extends AnimalMeta {
    public TurtleMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isHasEgg() {
        return get(MetadataDef.Turtle.HAS_EGG);
    }

    public void setHasEgg(boolean value) {
        set(MetadataDef.Turtle.HAS_EGG, value);
    }

    public boolean isLayingEgg() {
        return get(MetadataDef.Turtle.IS_LAYING_EGG);
    }

    public void setLayingEgg(boolean value) {
        set(MetadataDef.Turtle.IS_LAYING_EGG, value);
    }

}
