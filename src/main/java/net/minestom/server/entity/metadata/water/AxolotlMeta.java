package net.minestom.server.entity.metadata.water;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.animal.AnimalMeta;
import org.jetbrains.annotations.NotNull;

public class AxolotlMeta extends AnimalMeta {
    public AxolotlMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public Variant getVariant() {
        return Variant.VALUES[metadata.get(MetadataDef.Axolotl.VARIANT)];
    }

    public void setVariant(Variant variant) {
        metadata.set(MetadataDef.Axolotl.VARIANT, variant.ordinal());
    }

    public boolean isPlayingDead() {
        return metadata.get(MetadataDef.Axolotl.IS_PLAYING_DEAD);
    }

    public void setPlayingDead(boolean playingDead) {
        metadata.set(MetadataDef.Axolotl.IS_PLAYING_DEAD, playingDead);
    }

    public boolean isFromBucket() {
        return metadata.get(MetadataDef.Axolotl.IS_FROM_BUCKET);
    }

    public void setFromBucket(boolean fromBucket) {
        metadata.set(MetadataDef.Axolotl.IS_FROM_BUCKET, fromBucket);
    }

    public enum Variant {
        LUCY,
        WILD,
        GOLD,
        CYAN,
        BLUE;

        private final static AxolotlMeta.Variant[] VALUES = values();
    }
}
