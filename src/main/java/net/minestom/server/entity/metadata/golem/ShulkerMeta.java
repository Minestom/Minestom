package net.minestom.server.entity.metadata.golem;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

public class ShulkerMeta extends AbstractGolemMeta {
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

    public byte getColor() {
        return metadata.get(MetadataDef.Shulker.COLOR);
    }

    public void setColor(byte value) {
        metadata.set(MetadataDef.Shulker.COLOR, value);
    }

}
