package net.minestom.server.network.packet.client.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientClickWindowPacket(byte windowId, int stateId,
                                      short slot, byte button, @NotNull ClickType clickType,
                                      @NotNull List<ChangedSlot> changedSlots,
                                      @NotNull ItemStack clickedItem) implements ClientPacket {
    public ClientClickWindowPacket {
        changedSlots = List.copyOf(changedSlots);
    }

    public ClientClickWindowPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(BYTE), reader.read(VAR_INT),
                reader.read(SHORT), reader.read(BYTE), reader.readEnum(ClickType.class),
                reader.readCollection(ChangedSlot::new), reader.read(ITEM));
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
        public ChangedSlot(@NotNull NetworkBuffer reader) {
            this(reader.read(SHORT), reader.read(ITEM));
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
