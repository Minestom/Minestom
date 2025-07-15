package net.minestom.server.entity.metadata.other;

import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.registry.Holder;
import org.jspecify.annotations.Nullable;

public class PaintingMeta extends HangingMeta {

    public PaintingMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#PAINTING_VARIANT} instead.
     */
    @Deprecated
    public Holder<PaintingVariant> getVariant() {
        return metadata.get(MetadataDef.Painting.VARIANT);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#PAINTING_VARIANT} instead.
     */
    @Deprecated
    public void setVariant(Holder<PaintingVariant> value) {
        metadata.set(MetadataDef.Painting.VARIANT, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> @Nullable T get(DataComponent<T> component) {
        if (component == DataComponents.PAINTING_VARIANT)
            return (T) getVariant();
        return super.get(component);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> void set(DataComponent<T> component, T value) {
        if (component == DataComponents.PAINTING_VARIANT)
            setVariant((Holder<PaintingVariant>) value);
        else super.set(component, value);
    }

}
