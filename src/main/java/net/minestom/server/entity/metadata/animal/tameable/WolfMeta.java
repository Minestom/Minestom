package net.minestom.server.entity.metadata.animal.tameable;

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

    public int getCollarColor() {
        return metadata.get(MetadataDef.Wolf.COLLAR_COLOR);
    }

    public void setCollarColor(int value) {
        metadata.set(MetadataDef.Wolf.COLLAR_COLOR, value);
    }

    public int getAngerTime() {
        return metadata.get(MetadataDef.Wolf.ANGER_TIME);
    }

    public void setAngerTime(int value) {
        metadata.set(MetadataDef.Wolf.ANGER_TIME, value);
    }

    public @NotNull DynamicRegistry.Key<WolfVariant> getVariant() {
        return metadata.get(MetadataDef.Wolf.VARIANT);
    }

    public void setVariant(@NotNull DynamicRegistry.Key<WolfVariant> value) {
        metadata.set(MetadataDef.Wolf.VARIANT, value);
    }

}
