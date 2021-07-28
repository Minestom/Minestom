package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class ChestedHorseMeta extends AbstractHorseMeta {
    public static final byte OFFSET = AbstractHorseMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    protected ChestedHorseMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isHasChest() {
        return super.metadata.getIndex(OFFSET, false);
    }

    public void setHasChest(boolean value) {
        super.metadata.setIndex(OFFSET, Metadata.Boolean(value));
    }

}
