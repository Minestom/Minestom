package net.minestom.server.entity.metadata.golem;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

public class ShulkerMeta extends AbstractGolemMeta {

    public ShulkerMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public Direction getAttachFace() {
        return super.metadata.getIndex((byte) 15, Direction.DOWN);
    }

    public void setAttachFace(Direction value) {
        super.metadata.setIndex((byte) 15, Metadata.Direction(value));
    }

    public BlockPosition getAttachmentPosition() {
        return super.metadata.getIndex((byte) 16, null);
    }

    public void setAttachmentPosition(BlockPosition value) {
        super.metadata.setIndex((byte) 16, Metadata.OptPosition(value));
    }

    public byte getShieldHeight() {
        return super.metadata.getIndex((byte) 17, (byte) 0);
    }

    public void setShieldHeight(byte value) {
        super.metadata.setIndex((byte) 17, Metadata.Byte(value));
    }

    public byte getColor() {
        return super.metadata.getIndex((byte) 18, (byte) 10);
    }

    public void setColor(byte value) {
        super.metadata.setIndex((byte) 18, Metadata.Byte(value));
    }

}
