package net.minestom.server.entity.metadata.arrow;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class ThrownTridentMeta extends AbstractArrowMeta {

    public ThrownTridentMeta(@NotNull Entity entity) {
        super(entity);
    }

    public int getLoyaltyLevel() {
        return getMetadata().getIndex((byte) 9, 0);
    }

    public void setLoyaltyLevel(int value) {
        getMetadata().setIndex((byte) 9, Metadata.VarInt(value));
    }

    public boolean isHasEnchantmentGlint() {
        return getMetadata().getIndex((byte) 10, false);
    }

    public void setHasEnchantmentGlint(boolean value) {
        getMetadata().setIndex((byte) 10, Metadata.Boolean(value));
    }

}
