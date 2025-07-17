package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class BeeMeta extends AnimalMeta {
    public BeeMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isAngry() {
        return metadata.get(MetadataDef.Bee.IS_ANGRY);
    }

    public void setAngry(boolean value) {
        metadata.set(MetadataDef.Bee.IS_ANGRY, value);
    }

    public boolean isHasStung() {
        return metadata.get(MetadataDef.Bee.HAS_STUNG);
    }

    public void setHasStung(boolean value) {
        metadata.set(MetadataDef.Bee.HAS_STUNG, value);
    }

    public boolean isHasNectar() {
        return metadata.get(MetadataDef.Bee.HAS_NECTAR);
    }

    public void setHasNectar(boolean value) {
        metadata.set(MetadataDef.Bee.HAS_NECTAR, value);
    }

    public int getAngerTicks() {
        return metadata.get(MetadataDef.Bee.ANGER_TIME_TICKS);
    }

    public void setAngerTicks(int value) {
        metadata.set(MetadataDef.Bee.ANGER_TIME_TICKS, value);
    }

}
