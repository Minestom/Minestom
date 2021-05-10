package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class OcelotMeta extends AnimalMeta {

    public OcelotMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isTrusting() {
        return super.metadata.getIndex((byte) 16, false);
    }

    public void setTrusting(boolean value) {
        super.metadata.setIndex((byte) 16, Metadata.Boolean(value));
    }

}
