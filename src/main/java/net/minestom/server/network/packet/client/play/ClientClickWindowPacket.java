package net.minestom.server.network.packet.client.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
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
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(BYTE, windowId);
        writer.write(VAR_INT, stateId);
        writer.write(SHORT, slot);
        writer.write(BYTE, button);
        writer.write(VAR_INT, clickType.ordinal());
        writer.writeCollection(changedSlots);
        writer.write(ITEM, clickedItem);
    }

    public record ChangedSlot(short slot, @NotNull ItemStack item) implements NetworkBuffer.Writer {
        public ChangedSlot(@NotNull NetworkBuffer reader) {
            this(reader.read(SHORT), reader.read(ITEM));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(SHORT, slot);
            writer.write(ITEM, item);
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
