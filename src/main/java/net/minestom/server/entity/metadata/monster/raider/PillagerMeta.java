package net.minestom.server.entity.metadata.monster.raider;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class PillagerMeta extends AbstractIllagerMeta {
    public static final byte OFFSET = AbstractIllagerMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    public PillagerMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isChargingCrossbow() {
        return super.metadata.getIndex(OFFSET, false);
    }

    public void setChargingCrossbow(boolean value) {
        super.metadata.setIndex(OFFSET, Metadata.Boolean(value));
    }

}
