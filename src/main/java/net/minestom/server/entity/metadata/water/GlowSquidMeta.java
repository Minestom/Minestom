package net.minestom.server.entity.metadata.water;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public final class GlowSquidMeta extends AgeableWaterAnimalMeta {
    public GlowSquidMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    private int getDarkTicksRemaining() {
        return get(MetadataDef.GlowSquid.DARK_TICKS_REMAINING);
    }

    private void setDarkTicksRemaining(int ticks) {
        set(MetadataDef.GlowSquid.DARK_TICKS_REMAINING, ticks);
    }

}
