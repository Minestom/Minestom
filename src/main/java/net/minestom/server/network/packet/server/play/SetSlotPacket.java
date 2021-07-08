package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

public class SetSlotPacket implements ComponentHoldingServerPacket {

    public byte windowId;
    public short slot;
    public ItemStack itemStack;

    public SetSlotPacket() {
        itemStack = ItemStack.AIR;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(windowId);
        writer.writeShort(slot);
        writer.writeItemStack(itemStack);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        windowId = reader.readByte();
        slot = reader.readShort();
        itemStack = reader.readItemStack();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SET_SLOT;
    }

    /**
     * Returns a {@link SetSlotPacket} used to change a player cursor item.
     *
     * @param cursorItem the cursor item
     * @return a set slot packet to change a player cursor item
     */
    @NotNull
    public static SetSlotPacket createCursorPacket(@NotNull ItemStack cursorItem) {
        SetSlotPacket setSlotPacket = new SetSlotPacket();
        setSlotPacket.windowId = -1;
        setSlotPacket.slot = -1;
        setSlotPacket.itemStack = cursorItem;
        return setSlotPacket;
    }

    @Override
    public @NotNull Collection<Component> components() {
        return itemStack.components();
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        SetSlotPacket packet = new SetSlotPacket();
        packet.windowId = this.windowId;
        packet.slot = this.slot;
        packet.itemStack = this.itemStack.copyWithOperator(operator);
        return packet;
    }
}
