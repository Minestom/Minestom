package net.minestom.server.entity.type.projectile;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ObjectEntity;
import net.minestom.server.entity.type.Projectile;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryWriter;

import java.util.function.Consumer;

public class EntityEyeOfEnder extends ObjectEntity implements Projectile {

    private ItemStack itemStack;

    public EntityEyeOfEnder(Position spawnPosition) {
        super(EntityType.EYE_OF_ENDER, spawnPosition);
    }

    @Override
    public Consumer<BinaryWriter> getMetadataConsumer() {
        return packet -> {
            super.getMetadataConsumer().accept(packet);
            fillMetadataIndex(packet, 7);
        };
    }

    @Override
    protected void fillMetadataIndex(BinaryWriter packet, int index) {
        super.fillMetadataIndex(packet, index);
        if (index == 7) {
            packet.writeByte((byte) 7);
            packet.writeByte(METADATA_SLOT);
            packet.writeItemStack(itemStack);
        }
    }


    /**
     * Get the eye of ender item
     *
     * @return the item
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Change the eye of ender item
     * <p>
     * Can be null to make it like {@link Material#ENDER_EYE}
     *
     * @param itemStack the new item stack
     */
    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        sendMetadataIndex(7);
    }

    @Override
    public int getObjectData() {
        return 0;
    }
}
