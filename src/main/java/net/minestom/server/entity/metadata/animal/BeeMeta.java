package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public class BeeMeta extends AnimalMeta {
    public BeeMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isRolling() {
        return metadata.get(MetadataDef.Bee.IS_ROLLING);
    }

    public void setRolling(boolean value) {
        metadata.set(MetadataDef.Bee.IS_ROLLING, value);
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

    public long getAngerEndTime() {
        return metadata.get(MetadataDef.Bee.ANGER_END_TIME);
    }

    public void setAngerEndTime(long value) {
        metadata.set(MetadataDef.Bee.ANGER_END_TIME, value);
    }

}
