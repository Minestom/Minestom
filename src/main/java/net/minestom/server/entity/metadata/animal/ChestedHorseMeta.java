package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class ChestedHorseMeta extends AbstractHorseMeta {

    protected ChestedHorseMeta(@NotNull Entity entity) {
        super(entity);
    }

    public boolean isHasChest() {
        return getMetadata().getIndex((byte) 18, false);
    }

    public void setHasChest(boolean value) {
        getMetadata().setIndex((byte) 18, Metadata.Boolean(value));
    }

}
