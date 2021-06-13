package net.minestom.server.entity.metadata.arrow;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class ThrownTridentMeta extends AbstractArrowMeta {
    public static final byte OFFSET = AbstractArrowMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 2;

    public ThrownTridentMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public int getLoyaltyLevel() {
        return super.metadata.getIndex(OFFSET, 0);
    }

    public void setLoyaltyLevel(int value) {
        super.metadata.setIndex(OFFSET, Metadata.VarInt(value));
    }

    public boolean isHasEnchantmentGlint() {
        return super.metadata.getIndex(OFFSET + 1, false);
    }

    public void setHasEnchantmentGlint(boolean value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.Boolean(value));
    }

}
