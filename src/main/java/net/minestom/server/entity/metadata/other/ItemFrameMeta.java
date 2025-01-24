package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.Rotation;
import org.jetbrains.annotations.NotNull;

public class ItemFrameMeta extends EntityMeta implements ObjectDataProvider {
    private Orientation orientation;

    public ItemFrameMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
        this.orientation = Orientation.DOWN;
    }

    @NotNull
    public ItemStack getItem() {
        return metadata.get(MetadataDef.ItemFrame.ITEM);
    }

    public void setItem(@NotNull ItemStack value) {
        metadata.set(MetadataDef.ItemFrame.ITEM, value);
    }

    @NotNull
    public Rotation getRotation() {
        return Rotation.values()[metadata.get(MetadataDef.ItemFrame.ROTATION)];
    }

    public void setRotation(@NotNull Rotation value) {
        metadata.set(MetadataDef.ItemFrame.ROTATION, value.ordinal());
    }

    @NotNull
    public Orientation getOrientation() {
        return this.orientation;
    }

    /**
     * Sets orientation of the item frame.
     * This is possible only before spawn packet is sent.
     *
     * @param orientation the orientation of the item frame.
     */
    public void setOrientation(@NotNull Orientation orientation) {
        this.orientation = orientation;
    }

    @Override
    public int getObjectData() {
        return this.orientation.ordinal();
    }

    @Override
    public boolean requiresVelocityPacketAtSpawn() {
        return false;
    }

    public enum Orientation {
        DOWN,
        UP,
        NORTH,
        SOUTH,
        WEST,
        EAST
    }
}
