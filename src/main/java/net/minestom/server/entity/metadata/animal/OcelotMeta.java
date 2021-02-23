package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class OcelotMeta extends AnimalMeta {

    public OcelotMeta(@NotNull Entity entity) {
        super(entity);
    }

    public boolean isTrusting() {
        return getMetadata().getIndex((byte) 16, false);
    }

    public void setTrusting(boolean value) {
        getMetadata().setIndex((byte) 16, Metadata.Boolean(value));
    }

}
