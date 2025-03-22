package net.minestom.server.entity.metadata.golem;

import net.minestom.server.color.DyeColor;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

public class ShulkerMeta extends AbstractGolemMeta {
    private static final DyeColor[] DYE_VALUES = DyeColor.values();

    public ShulkerMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
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
    public @NotNull DyeColor getColor() {
        return DYE_VALUES[metadata.get(MetadataDef.Shulker.COLOR)];
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#SHULKER_COLOR} instead.
     */
    @Deprecated
    public void setColor(@NotNull DyeColor value) {
        metadata.set(MetadataDef.Shulker.COLOR, (byte) value.ordinal());
    }

}
