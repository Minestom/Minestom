package net.minestom.server.entity.type.other;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.Constructable;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * TODO: update bounding box depending on state
 */
public class EntityShulker extends EntityCreature implements Constructable {

    public EntityShulker(@NotNull Position spawnPosition) {
        super(EntityType.SHULKER, spawnPosition);
        setBoundingBox(1D, 1D, 1D);
    }

    public EntityShulker(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.SHULKER, spawnPosition, instance);
        setBoundingBox(1D, 1D, 1D);
    }

    public Direction getAttachFace() {
        return this.metadata.getIndex((byte) 15, Direction.DOWN);
    }

    public void setAttachFace(Direction value) {
        this.metadata.setIndex((byte) 15, Metadata.Direction(value));
    }

    public BlockPosition getAttachmentPosition() {
        return this.metadata.getIndex((byte) 16, null);
    }

    public void setAttachmentPosition(BlockPosition value) {
        this.metadata.setIndex((byte) 16, Metadata.OptPosition(value));
    }

    public byte getShieldHeight() {
        return this.metadata.getIndex((byte) 17, (byte) 0);
    }

    public void setShieldHeight(byte value) {
        this.metadata.setIndex((byte) 17, Metadata.Byte(value));
    }

    public byte getColor() {
        return this.metadata.getIndex((byte) 18, (byte) 10);
    }

    public void setColor(byte value) {
        this.metadata.setIndex((byte) 18, Metadata.Byte(value));
    }

}
