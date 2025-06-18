package net.minestom.server.entity.metadata.other;

import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.registry.Holder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PaintingMeta extends HangingMeta {

    public PaintingMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#PAINTING_VARIANT} instead.
     */
    @Deprecated
    public @NotNull Holder<PaintingVariant> getVariant() {
        return metadata.get(MetadataDef.Painting.VARIANT);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#PAINTING_VARIANT} instead.
     */
    @Deprecated
    public void setVariant(@NotNull Holder<PaintingVariant> value) {
        metadata.set(MetadataDef.Painting.VARIANT, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> @Nullable T get(@NotNull DataComponent<T> component) {
        if (component == DataComponents.PAINTING_VARIANT)
            return (T) getVariant();
        return super.get(component);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> void set(@NotNull DataComponent<T> component, @NotNull T value) {
        if (component == DataComponents.PAINTING_VARIANT)
            setVariant((Holder<PaintingVariant>) value);
        else super.set(component, value);
    }

}
