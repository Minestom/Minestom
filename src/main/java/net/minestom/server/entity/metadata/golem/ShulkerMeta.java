package net.minestom.server.entity.metadata.golem;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

public class ShulkerMeta extends AbstractGolemMeta {
    public static final byte OFFSET = AbstractGolemMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 3;

    public ShulkerMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public Direction getAttachFace() {
        return super.metadata.getIndex(OFFSET, Direction.DOWN);
    }

    public void setAttachFace(Direction value) {
        super.metadata.setIndex(OFFSET, Metadata.Direction(value));
    }

    public byte getShieldHeight() {
        return super.metadata.getIndex(OFFSET + 1, (byte) 0);
    }

    public void setShieldHeight(byte value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.Byte(value));
    }

    public byte getColor() {
        return super.metadata.getIndex(OFFSET + 2, (byte) 10);
    }

    public void setColor(byte value) {
        super.metadata.setIndex(OFFSET + 2, Metadata.Byte(value));
    }

}
