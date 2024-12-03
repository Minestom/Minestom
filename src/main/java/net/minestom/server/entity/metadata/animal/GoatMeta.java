package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class GoatMeta extends AnimalMeta {
    public GoatMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isScreaming() {
        return metadata.get(MetadataDef.Goat.IS_SCREAMING_GOAT);
    }

    public void setScreaming(boolean screaming) {
        metadata.set(MetadataDef.Goat.IS_SCREAMING_GOAT, screaming);
    }

    public boolean hasLeftHorn() {
        return metadata.get(MetadataDef.Goat.HAS_LEFT_HORN);
    }

    public void setLeftHorn(boolean leftHorn) {
        metadata.set(MetadataDef.Goat.HAS_LEFT_HORN, leftHorn);
    }

    public boolean hasRightHorn() {
        return metadata.get(MetadataDef.Goat.HAS_RIGHT_HORN);
    }

    public void setRightHorn(boolean rightHorn) {
        metadata.set(MetadataDef.Goat.HAS_RIGHT_HORN, rightHorn);
    }
}
