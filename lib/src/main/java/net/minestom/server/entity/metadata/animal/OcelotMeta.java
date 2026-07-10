package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.MetaTarget;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.Nullable;

public class OcelotMeta extends AnimalMeta {
    public OcelotMeta(@Nullable MetaTarget entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isTrusting() {
        return metadata.get(MetadataDef.Ocelot.IS_TRUSTING);
    }

    public void setTrusting(boolean value) {
        metadata.set(MetadataDef.Ocelot.IS_TRUSTING, value);
    }

}
