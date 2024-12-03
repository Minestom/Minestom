package net.minestom.server.entity.metadata.animal.tameable;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.animal.AnimalMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TameableAnimalMeta extends AnimalMeta {
    protected TameableAnimalMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isSitting() {
        return metadata.get(MetadataDef.TameableAnimal.IS_SITTING);
    }

    public void setSitting(boolean value) {
        metadata.set(MetadataDef.TameableAnimal.IS_SITTING, value);
    }

    public boolean isTamed() {
        return metadata.get(MetadataDef.TameableAnimal.IS_TAMED);
    }

    public void setTamed(boolean value) {
        metadata.set(MetadataDef.TameableAnimal.IS_TAMED, value);
    }

    @Nullable
    public UUID getOwner() {
        return metadata.get(MetadataDef.TameableAnimal.OWNER);
    }

    public void setOwner(@Nullable UUID value) {
        metadata.set(MetadataDef.TameableAnimal.OWNER, value);
    }

}
