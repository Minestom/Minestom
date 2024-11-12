package net.minestom.server.entity.metadata.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class ThrownTridentMeta extends AbstractArrowMeta {
    public ThrownTridentMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public byte getLoyaltyLevel() {
        return metadata.get(MetadataDef.ThrownTrident.LOYALTY_LEVEL);
    }

    public void setLoyaltyLevel(byte value) {
        metadata.set(MetadataDef.ThrownTrident.LOYALTY_LEVEL, value);
    }

    public boolean isHasEnchantmentGlint() {
        return metadata.get(MetadataDef.ThrownTrident.HAS_ENCHANTMENT_GLINT);
    }

    public void setHasEnchantmentGlint(boolean value) {
        metadata.set(MetadataDef.ThrownTrident.HAS_ENCHANTMENT_GLINT, value);
    }

}
