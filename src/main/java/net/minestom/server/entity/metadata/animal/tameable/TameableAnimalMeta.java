package net.minestom.server.entity.metadata.animal.tameable;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.animal.AbstractNautilusMeta;
import net.minestom.server.entity.metadata.animal.AnimalMeta;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public sealed abstract class TameableAnimalMeta extends AnimalMeta permits AbstractNautilusMeta, CatMeta, ParrotMeta, WolfMeta {
    protected TameableAnimalMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isSitting() {
        return get(MetadataDef.TameableAnimal.IS_SITTING);
    }

    public void setSitting(boolean value) {
        set(MetadataDef.TameableAnimal.IS_SITTING, value);
    }

    public boolean isTamed() {
        return get(MetadataDef.TameableAnimal.IS_TAMED);
    }

    public void setTamed(boolean value) {
        set(MetadataDef.TameableAnimal.IS_TAMED, value);
    }

    @Nullable
    public UUID getOwner() {
        return get(MetadataDef.TameableAnimal.OWNER);
    }

    public void setOwner(@Nullable UUID value) {
        set(MetadataDef.TameableAnimal.OWNER, value);
    }

}
