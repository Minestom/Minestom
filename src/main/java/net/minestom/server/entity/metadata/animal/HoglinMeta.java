package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class HoglinMeta extends AnimalMeta {
    public HoglinMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isImmuneToZombification() {
        return metadata.get(MetadataDef.Hoglin.IMMUNE_ZOMBIFICATION);
    }

    public void setImmuneToZombification(boolean value) {
        metadata.set(MetadataDef.Hoglin.IMMUNE_ZOMBIFICATION, value);
    }

}
