package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public final class StriderMeta extends AnimalMeta {
    public StriderMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getTimeToBoost() {
        return get(MetadataDef.Strider.FUNGUS_BOOST);
    }

    public void setTimeToBoost(int value) {
        set(MetadataDef.Strider.FUNGUS_BOOST, value);
    }

    public boolean isShaking() {
        return get(MetadataDef.Strider.IS_SHAKING);
    }

    public void setShaking(boolean value) {
        set(MetadataDef.Strider.IS_SHAKING, value);
    }

}
