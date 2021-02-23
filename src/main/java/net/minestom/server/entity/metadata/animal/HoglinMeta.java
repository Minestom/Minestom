package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class HoglinMeta extends AnimalMeta {

    public HoglinMeta(@NotNull Entity entity) {
        super(entity);
    }

    public boolean isImmuneToZombification() {
        return getMetadata().getIndex((byte) 16, false);
    }

    public void setImmuneToZombification(boolean value) {
        getMetadata().setIndex((byte) 16, Metadata.Boolean(value));
    }

}
