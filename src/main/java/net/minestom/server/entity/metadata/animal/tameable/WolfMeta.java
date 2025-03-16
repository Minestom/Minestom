package net.minestom.server.entity.metadata.animal.tameable;

import net.minestom.server.color.DyeColor;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

public class WolfMeta extends TameableAnimalMeta {
    public WolfMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isBegging() {
        return metadata.get(MetadataDef.Wolf.IS_BEGGING);
    }

    public void setBegging(boolean value) {
        metadata.set(MetadataDef.Wolf.IS_BEGGING, value);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#WOLF_COLLAR} instead.
     */
    public @NotNull DyeColor getCollarColor() {
        return DyeColor.values()[metadata.get(MetadataDef.Wolf.COLLAR_COLOR)];
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#WOLF_COLLAR} instead.
     */
    public void setCollarColor(@NotNull DyeColor value) {
        metadata.set(MetadataDef.Wolf.COLLAR_COLOR, value.ordinal());
    }

    public int getAngerTime() {
        return metadata.get(MetadataDef.Wolf.ANGER_TIME);
    }

    public void setAngerTime(int value) {
        metadata.set(MetadataDef.Wolf.ANGER_TIME, value);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#WOLF_VARIANT} instead.
     */
    public @NotNull DynamicRegistry.Key<WolfVariant> getVariant() {
        return metadata.get(MetadataDef.Wolf.VARIANT);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#WOLF_VARIANT} instead.
     */
    public void setVariant(@NotNull DynamicRegistry.Key<WolfVariant> value) {
        metadata.set(MetadataDef.Wolf.VARIANT, value);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#WOLF_SOUND_VARIANT} instead.
     */
    public @NotNull DynamicRegistry.Key<WolfSoundVariant> getSoundVariant() {
        return metadata.get(MetadataDef.Wolf.SOUND_VARIANT);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#WOLF_SOUND_VARIANT} instead.
     */
    public void setSoundVariant(@NotNull DynamicRegistry.Key<WolfSoundVariant> value) {
        metadata.set(MetadataDef.Wolf.SOUND_VARIANT, value);
    }

}
