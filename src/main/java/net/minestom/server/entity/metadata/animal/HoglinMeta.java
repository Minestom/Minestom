package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.MetaTarget;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.Nullable;

public class HoglinMeta extends AnimalMeta {
    public HoglinMeta(@Nullable MetaTarget entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isImmuneToZombification() {
        return metadata.get(MetadataDef.Hoglin.IMMUNE_ZOMBIFICATION);
    }

    public void setImmuneToZombification(boolean value) {
        metadata.set(MetadataDef.Hoglin.IMMUNE_ZOMBIFICATION, value);
    }

}
