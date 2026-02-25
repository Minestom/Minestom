package net.minestom.server.entity.metadata.water;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public class GlowSquidMeta extends AgeableWaterAnimalMeta {
    public GlowSquidMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    private int getDarkTicksRemaining() {
        return metadata.get(MetadataDef.GlowSquid.DARK_TICKS_REMAINING);
    }

    private void setDarkTicksRemaining(int ticks) {
        metadata.set(MetadataDef.GlowSquid.DARK_TICKS_REMAINING, ticks);
    }

}
