package net.minestom.server.entity.metadata.other;

import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import net.minestom.server.registry.Holder;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PaintingMeta extends EntityMeta implements ObjectDataProvider {
    private Orientation orientation = null;

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

    @NotNull
    public Orientation getOrientation() {
        return this.orientation;
    }

    /**
     * Sets orientation of the painting.
     * This is possible only before spawn packet is sent.
     *
     * @param orientation the orientation of the painting.
     */
    public void setOrientation(@NotNull Orientation orientation) {
        this.orientation = orientation;
    }

    @Override
    public int getObjectData() {
        Check.stateCondition(this.orientation == null, "Painting orientation must be set before spawn");
        return this.orientation.id();
    }

    @Override
    public boolean requiresVelocityPacketAtSpawn() {
        return false;
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

    public enum Orientation {
        NORTH(2),
        SOUTH(3),
        WEST(4),
        EAST(5);

        private final int id;

        Orientation(int id) {
            this.id = id;
        }

        public int id() {
            return id;
        }
    }

}
