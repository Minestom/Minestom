package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

public class PaintingMeta extends EntityMeta implements ObjectDataProvider {
    private Orientation orientation = null;

    public PaintingMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#PAINTING_VARIANT} instead.
     */
    public @NotNull DynamicRegistry.Key<PaintingVariant> getVariant() {
        return metadata.get(MetadataDef.Painting.VARIANT);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#PAINTING_VARIANT} instead.
     */
    public void setVariant(@NotNull DynamicRegistry.Key<PaintingVariant> value) {
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
