package net.minestom.server.entity.metadata.water;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class GlowSquidMeta extends AgeableWaterAnimalMeta {
    public static final byte OFFSET = AgeableWaterAnimalMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    public GlowSquidMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    private int getDarkTicksRemaining() {
        return metadata.getIndex(OFFSET, 0);
    }

    private void setDarkTicksRemaining(int ticks) {
        metadata.setIndex(OFFSET, Metadata.VarInt(ticks));
    }

}
