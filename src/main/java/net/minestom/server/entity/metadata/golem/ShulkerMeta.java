package net.minestom.server.entity.metadata.golem;

import net.minestom.server.color.DyeColor;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.utils.Direction;
import org.jspecify.annotations.Nullable;

public class ShulkerMeta extends AbstractGolemMeta {
    private static final DyeColor[] DYE_VALUES = DyeColor.values();

    public ShulkerMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public Direction getAttachFace() {
        return metadata.get(MetadataDef.Shulker.ATTACH_FACE);
    }

    public void setAttachFace(Direction value) {
        metadata.set(MetadataDef.Shulker.ATTACH_FACE, value);
    }

    public byte getShieldHeight() {
        return metadata.get(MetadataDef.Shulker.SHIELD_HEIGHT);
    }

    public void setShieldHeight(byte value) {
        metadata.set(MetadataDef.Shulker.SHIELD_HEIGHT, value);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#SHULKER_COLOR} instead.
     */
    @Deprecated
    public DyeColor getColor() {
        return DYE_VALUES[metadata.get(MetadataDef.Shulker.COLOR)];
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#SHULKER_COLOR} instead.
     */
    @Deprecated
    public void setColor(DyeColor value) {
        metadata.set(MetadataDef.Shulker.COLOR, (byte) value.ordinal());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> @Nullable T get(DataComponent<T> component) {
        if (component == DataComponents.SHULKER_COLOR)
            return (T) getColor();
        return super.get(component);
    }

    @Override
    protected <T> void set(DataComponent<T> component, T value) {
        if (component == DataComponents.SHULKER_COLOR)
            setColor((DyeColor) value);
        else super.set(component, value);
    }

}
