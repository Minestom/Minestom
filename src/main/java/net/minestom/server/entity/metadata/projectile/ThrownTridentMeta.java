package net.minestom.server.entity.metadata.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public final class ThrownTridentMeta extends AbstractArrowMeta {
    public ThrownTridentMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public byte getLoyaltyLevel() {
        return get(MetadataDef.ThrownTrident.LOYALTY_LEVEL);
    }

    public void setLoyaltyLevel(byte value) {
        set(MetadataDef.ThrownTrident.LOYALTY_LEVEL, value);
    }

    public boolean isHasEnchantmentGlint() {
        return get(MetadataDef.ThrownTrident.HAS_ENCHANTMENT_GLINT);
    }

    public void setHasEnchantmentGlint(boolean value) {
        set(MetadataDef.ThrownTrident.HAS_ENCHANTMENT_GLINT, value);
    }

}
