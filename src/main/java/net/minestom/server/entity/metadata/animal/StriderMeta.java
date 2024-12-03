package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class StriderMeta extends AnimalMeta {
    public StriderMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getTimeToBoost() {
        return metadata.get(MetadataDef.Strider.FUNGUS_BOOST);
    }

    public void setTimeToBoost(int value) {
        metadata.set(MetadataDef.Strider.FUNGUS_BOOST, value);
    }

    public boolean isShaking() {
        return metadata.get(MetadataDef.Strider.IS_SHAKING);
    }

    public void setShaking(boolean value) {
        metadata.set(MetadataDef.Strider.IS_SHAKING, value);
    }

    public boolean isHasSaddle() {
        return metadata.get(MetadataDef.Strider.HAS_SADDLE);
    }

    public void setHasSaddle(boolean value) {
        metadata.set(MetadataDef.Strider.HAS_SADDLE, value);
    }

}
