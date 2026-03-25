package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public final class BeeMeta extends AnimalMeta {
    public BeeMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isAngry() {
        return get(MetadataDef.Bee.IS_ANGRY);
    }

    public void setAngry(boolean value) {
        set(MetadataDef.Bee.IS_ANGRY, value);
    }

    public boolean isHasStung() {
        return get(MetadataDef.Bee.HAS_STUNG);
    }

    public void setHasStung(boolean value) {
        set(MetadataDef.Bee.HAS_STUNG, value);
    }

    public boolean isHasNectar() {
        return get(MetadataDef.Bee.HAS_NECTAR);
    }

    public void setHasNectar(boolean value) {
        set(MetadataDef.Bee.HAS_NECTAR, value);
    }

    public long getAngerTicks() {
        return get(MetadataDef.Bee.ANGER_TIME_TICKS);
    }

    public void setAngerTicks(long value) {
        set(MetadataDef.Bee.ANGER_TIME_TICKS, value);
    }

}
