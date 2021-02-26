package net.minestom.server.entity.type.decoration;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.ObjectEntity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Rotation;
import org.jetbrains.annotations.NotNull;

// FIXME: https://wiki.vg/Object_Data#Item_Frame_.28id_71.29
// "You have to set both Orientation and Yaw/Pitch accordingly, otherwise it will not work."
/**
 * @deprecated Use {@link net.minestom.server.entity.metadata.other.ItemFrameMeta} instead.
 */
@Deprecated
public class EntityItemFrame extends ObjectEntity {

    private final ItemFrameOrientation orientation;

    public EntityItemFrame(@NotNull Position spawnPosition, @NotNull ItemFrameOrientation orientation) {
        super(EntityType.ITEM_FRAME, spawnPosition);
        this.orientation = orientation;
        setNoGravity(true);
        setGravity(0f, 0f, 0f);
    }

    @Override
    public int getObjectData() {
        return orientation.ordinal();
    }

    /**
     * Gets the item stack in the frame.
     *
     * @return the item stack in the frame
     */
    @NotNull
    public ItemStack getItemStack() {
        return metadata.getIndex((byte) 7, ItemStack.getAirItem());
    }

    /**
     * Changes the item stack in the frame.
     *
     * @param itemStack the new item stack in the frame
     */
    public void setItemStack(@NotNull ItemStack itemStack) {
        this.metadata.setIndex((byte) 7, Metadata.Slot(itemStack));
    }

    /**
     * Gets the item rotation.
     *
     * @return the item rotation
     */
    @NotNull
    public Rotation getRotation() {
        final int ordinal = metadata.getIndex((byte) 8, 0);
        return Rotation.values()[ordinal];
    }

    /**
     * Changes the item rotation.
     *
     * @param rotation the new item rotation
     */
    public void setRotation(@NotNull Rotation rotation) {
        this.metadata.setIndex((byte) 8, Metadata.VarInt(rotation.ordinal()));
    }

    /**
     * Represents the orientation of the frame.
     */
    public enum ItemFrameOrientation {
        DOWN, UP, NORTH, SOUTH, WEST, EAST
    }

}
