package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class ChestedHorseMeta extends AbstractHorseMeta {

    protected ChestedHorseMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isHasChest() {
        return super.metadata.getIndex((byte) 18, false);
    }

    public void setHasChest(boolean value) {
        super.metadata.setIndex((byte) 18, Metadata.Boolean(value));
    }

}
