package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class GoatMeta extends AnimalMeta {
    public static final byte OFFSET = AnimalMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    public GoatMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isScreaming() {
        return metadata.getIndex(OFFSET, false);
    }

    public void setScreaming(boolean screaming) {
        metadata.setIndex(OFFSET, Metadata.Boolean(screaming));
    }
}
