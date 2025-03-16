package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.registry.DynamicRegistry;
import org.jetbrains.annotations.NotNull;

public class CowMeta extends AnimalMeta {
    public CowMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#COW_VARIANT} instead.
     */
    public @NotNull DynamicRegistry.Key<CowVariant> getVariant() {
        return metadata.get(MetadataDef.Cow.VARIANT);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#COW_VARIANT} instead.
     */
    public void setVariant(@NotNull DynamicRegistry.Key<CowVariant> variant) {
        metadata.set(MetadataDef.Cow.VARIANT, variant);
    }

}
