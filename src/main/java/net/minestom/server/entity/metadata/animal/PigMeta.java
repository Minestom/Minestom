package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class PigMeta extends AnimalMeta {
    public PigMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isHasSaddle() {
        return metadata.get(MetadataDef.Pig.HAS_SADDLE);
    }

    public void setHasSaddle(boolean value) {
        metadata.set(MetadataDef.Pig.HAS_SADDLE, value);
    }

    public int getTimeToBoost() {
        return metadata.get(MetadataDef.Pig.BOOST_TIME);
    }

    public void setTimeToBoost(int value) {
        metadata.set(MetadataDef.Pig.BOOST_TIME, value);
    }

}
