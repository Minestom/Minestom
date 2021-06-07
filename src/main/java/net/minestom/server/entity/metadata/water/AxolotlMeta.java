package net.minestom.server.entity.metadata.water;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class AxolotlMeta extends WaterAnimalMeta {
    public static final byte OFFSET = WaterAnimalMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 3;

    public AxolotlMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    //todo variant enum
    public int getVariant() {
        return metadata.getIndex(OFFSET, 0);
    }

    public void setVariant(int variant) {
        metadata.setIndex(variant, Metadata.VarInt(variant));
    }

    public boolean isPlayingDead() {
        return metadata.getIndex(OFFSET + 1, false);
    }

    public void setPlayingDead(boolean playingDead) {
        metadata.setIndex(OFFSET + 1, Metadata.Boolean(playingDead));
    }

    public boolean isFromBucket() {
        return metadata.getIndex(OFFSET + 2, false);
    }

    public void setFromBucket(boolean fromBucket) {
        metadata.setIndex(OFFSET + 2, Metadata.Boolean(fromBucket));
    }
}
