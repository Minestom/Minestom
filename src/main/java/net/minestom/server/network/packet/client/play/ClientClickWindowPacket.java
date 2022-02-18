package net.minestom.server.network.packet.client.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ClientClickWindowPacket(byte windowId, int stateId,
                                      short slot, byte button, @NotNull ClickType clickType,
                                      @NotNull List<ChangedSlot> changedSlots,
                                      @NotNull ItemStack clickedItem) implements ClientPacket {
    public ClientClickWindowPacket {
        changedSlots = List.copyOf(changedSlots);
    }

    public ClientClickWindowPacket(BinaryReader reader) {
        this(reader.readByte(), reader.readVarInt(),
                reader.readShort(), reader.readByte(), ClickType.values()[reader.readVarInt()],
                reader.readVarIntList(ChangedSlot::new), reader.readItemStack());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(windowId);
        writer.writeVarInt(stateId);
        writer.writeShort(slot);
        writer.writeByte(button);
        writer.writeVarInt(clickType.ordinal());
        writer.writeVarIntList(changedSlots, BinaryWriter::write);
        writer.writeItemStack(clickedItem);
    }

    public record ChangedSlot(short slot, @NotNull ItemStack item) implements Writeable {
        public ChangedSlot(BinaryReader reader) {
            this(reader.readShort(), reader.readItemStack());
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeShort(slot);
            writer.writeItemStack(item);
        }
    }

    public enum ClickType {
        PICKUP,
        QUICK_MOVE,
        SWAP,
        CLONE,
        THROW,
        QUICK_CRAFT,
        PICKUP_ALL
    }
}
