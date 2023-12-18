package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

// This is the packet sent when you toggle a slot in a crafter UI
public record ClientWindowSlotStatePacket(int slot, int windowId, boolean newState) implements ClientPacket {

    public ClientWindowSlotStatePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(VAR_INT), reader.read(BOOLEAN));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, slot);
        writer.write(VAR_INT, windowId);
        writer.write(BOOLEAN, newState);
    }
}
