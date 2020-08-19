package net.minestom.server.entity.type.decoration;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ObjectEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.item.ArmorEquipEvent;
import net.minestom.server.inventory.EquipmentHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.item.ItemStackUtils;

import java.util.function.Consumer;

public class EntityArmorStand extends ObjectEntity implements EquipmentHandler {

    private boolean small;
    private boolean hasArms;
    private boolean noBasePlate;
    private boolean setMarker;

    private Vector headRotation;
    private Vector bodyRotation;
    private Vector leftArmRotation;
    private Vector rightArmRotation;
    private Vector leftLegRotation;
    private Vector rightLegRotation;

    // Equipments
    private ItemStack mainHandItem;
    private ItemStack offHandItem;

    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;

    public EntityArmorStand(Position spawnPosition) {
        super(EntityType.ARMOR_STAND, spawnPosition);

        // Refresh BoundingBox
        setSmall(false);

        setHeadRotation(new Vector(0, 0, 0));
        setBodyRotation(new Vector(0, 0, 0));
        setLeftArmRotation(new Vector(-10f, 0, -10f));
        setRightArmRotation(new Vector(-15f, 0, -10f));
        setLeftLegRotation(new Vector(-1f, 0, -1f));
        setRightLegRotation(new Vector(1, 0, 1));

        this.mainHandItem = ItemStack.getAirItem();
        this.offHandItem = ItemStack.getAirItem();

        this.helmet = ItemStack.getAirItem();
        this.chestplate = ItemStack.getAirItem();
        this.leggings = ItemStack.getAirItem();
        this.boots = ItemStack.getAirItem();

    }

    @Override
    public boolean addViewer(Player player) {
        boolean result = super.addViewer(player);
        syncEquipments(player.getPlayerConnection());
        return result;
    }

    @Override
    public int getObjectData() {
        return 0;
    }

    @Override
    public Consumer<BinaryWriter> getMetadataConsumer() {
        return packet -> {
            super.getMetadataConsumer().accept(packet);
            fillMetadataIndex(packet, 14);
            fillMetadataIndex(packet, 15);
            fillMetadataIndex(packet, 16);
            fillMetadataIndex(packet, 17);
            fillMetadataIndex(packet, 18);
            fillMetadataIndex(packet, 19);
            fillMetadataIndex(packet, 20);
        };
    }

    @Override
    protected void fillMetadataIndex(BinaryWriter packet, int index) {
        super.fillMetadataIndex(packet, index);
        if (index == 14) {
            packet.writeByte((byte) 14);
            packet.writeByte(METADATA_BYTE);
            byte dataValue = 0;
            if (isSmall())
                dataValue += 1;
            if (hasArms)
                dataValue += 2;
            if (hasNoBasePlate())
                dataValue += 4;
            if (hasMarker())
                dataValue += 8;
            packet.writeByte(dataValue);
        } else if (index == 15) {
            packet.writeByte((byte) 15);
            packet.writeByte(METADATA_ROTATION);
            packet.writeFloat(getRotationX(headRotation));
            packet.writeFloat(getRotationY(headRotation));
            packet.writeFloat(getRotationZ(headRotation));
        } else if (index == 16) {
            packet.writeByte((byte) 16);
            packet.writeByte(METADATA_ROTATION);
            packet.writeFloat(getRotationX(bodyRotation));
            packet.writeFloat(getRotationY(bodyRotation));
            packet.writeFloat(getRotationZ(bodyRotation));
        } else if (index == 17) {
            packet.writeByte((byte) 17);
            packet.writeByte(METADATA_ROTATION);
            packet.writeFloat(getRotationX(leftArmRotation));
            packet.writeFloat(getRotationY(leftArmRotation));
            packet.writeFloat(getRotationZ(leftArmRotation));
        } else if (index == 18) {
            packet.writeByte((byte) 18);
            packet.writeByte(METADATA_ROTATION);
            packet.writeFloat(getRotationX(rightArmRotation));
            packet.writeFloat(getRotationY(rightArmRotation));
            packet.writeFloat(getRotationZ(rightArmRotation));
        } else if (index == 19) {
            packet.writeByte((byte) 19);
            packet.writeByte(METADATA_ROTATION);
            packet.writeFloat(getRotationX(leftLegRotation));
            packet.writeFloat(getRotationY(leftLegRotation));
            packet.writeFloat(getRotationZ(leftLegRotation));
        } else if (index == 20) {
            packet.writeByte((byte) 20);
            packet.writeByte(METADATA_ROTATION);
            packet.writeFloat(getRotationX(rightLegRotation));
            packet.writeFloat(getRotationY(rightLegRotation));
            packet.writeFloat(getRotationZ(rightLegRotation));
        }
    }

    @Override
    public ItemStack getItemInMainHand() {
        return mainHandItem;
    }

    @Override
    public void setItemInMainHand(ItemStack itemStack) {
        this.mainHandItem = ItemStackUtils.notNull(itemStack);
        syncEquipment(EntityEquipmentPacket.Slot.MAIN_HAND);
    }

    @Override
    public ItemStack getItemInOffHand() {
        return offHandItem;
    }

    @Override
    public void setItemInOffHand(ItemStack itemStack) {
        this.offHandItem = ItemStackUtils.notNull(itemStack);
        syncEquipment(EntityEquipmentPacket.Slot.OFF_HAND);
    }

    @Override
    public ItemStack getHelmet() {
        return helmet;
    }

    @Override
    public void setHelmet(ItemStack itemStack) {
        this.helmet = getEquipmentItem(itemStack, ArmorEquipEvent.ArmorSlot.HELMET);
        syncEquipment(EntityEquipmentPacket.Slot.HELMET);
    }

    @Override
    public ItemStack getChestplate() {
        return chestplate;
    }

    @Override
    public void setChestplate(ItemStack itemStack) {
        this.chestplate = getEquipmentItem(itemStack, ArmorEquipEvent.ArmorSlot.CHESTPLATE);
        syncEquipment(EntityEquipmentPacket.Slot.CHESTPLATE);
    }

    @Override
    public ItemStack getLeggings() {
        return leggings;
    }

    @Override
    public void setLeggings(ItemStack itemStack) {
        this.leggings = getEquipmentItem(itemStack, ArmorEquipEvent.ArmorSlot.LEGGINGS);
        syncEquipment(EntityEquipmentPacket.Slot.LEGGINGS);
    }

    @Override
    public ItemStack getBoots() {
        return boots;
    }

    @Override
    public void setBoots(ItemStack itemStack) {
        this.boots = getEquipmentItem(itemStack, ArmorEquipEvent.ArmorSlot.BOOTS);
        syncEquipment(EntityEquipmentPacket.Slot.BOOTS);
    }

    public boolean isSmall() {
        return small;
    }

    public void setSmall(boolean small) {
        this.small = small;
        sendMetadataIndex(14);

        if (small) {
            setBoundingBox(0.25f, 0.9875f, 0.25f);
        } else {
            setBoundingBox(0.5f, 1.975f, 0.5f);
        }
    }

    public boolean hasArms() {
        return hasArms;
    }

    public void setHasArms(boolean hasArms) {
        this.hasArms = hasArms;
        sendMetadataIndex(14);
    }

    public boolean hasNoBasePlate() {
        return noBasePlate;
    }

    public void setNoBasePlate(boolean noBasePlate) {
        this.noBasePlate = noBasePlate;
        sendMetadataIndex(14);
    }

    public boolean hasMarker() {
        return setMarker;
    }

    public void setMarker(boolean setMarker) {
        this.setMarker = setMarker;
        sendMetadataIndex(14);
    }

    public Vector getHeadRotation() {
        return headRotation;
    }

    public void setHeadRotation(Vector headRotation) {
        this.headRotation = headRotation;
        sendMetadataIndex(15);
    }

    public Vector getBodyRotation() {
        return bodyRotation;
    }

    public void setBodyRotation(Vector bodyRotation) {
        this.bodyRotation = bodyRotation;
        sendMetadataIndex(16);
    }

    public Vector getLeftArmRotation() {
        return leftArmRotation;
    }

    public void setLeftArmRotation(Vector leftArmRotation) {
        this.leftArmRotation = leftArmRotation;
        sendMetadataIndex(17);
    }

    public Vector getRightArmRotation() {
        return rightArmRotation;
    }

    public void setRightArmRotation(Vector rightArmRotation) {
        this.rightArmRotation = rightArmRotation;
        sendMetadataIndex(18);
    }

    public Vector getLeftLegRotation() {
        return leftLegRotation;
    }

    public void setLeftLegRotation(Vector leftLegRotation) {
        this.leftLegRotation = leftLegRotation;
        sendMetadataIndex(19);
    }

    public Vector getRightLegRotation() {
        return rightLegRotation;
    }

    public void setRightLegRotation(Vector rightLegRotation) {
        this.rightLegRotation = rightLegRotation;
        sendMetadataIndex(20);
    }

    private float getRotationX(Vector vector) {
        return vector != null ? vector.getX() : 0;
    }

    private float getRotationY(Vector vector) {
        return vector != null ? vector.getY() : 0;
    }

    private float getRotationZ(Vector vector) {
        return vector != null ? vector.getZ() : 0;
    }

    // Equipments

    private ItemStack getEquipmentItem(ItemStack itemStack, ArmorEquipEvent.ArmorSlot armorSlot) {
        itemStack = ItemStackUtils.notNull(itemStack);

        ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(this, itemStack, armorSlot);
        callEvent(ArmorEquipEvent.class, armorEquipEvent);
        return armorEquipEvent.getArmorItem();
    }
}
