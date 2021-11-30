package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientHeldItemChangePacket(short slot) implements ClientPacket {
    public ClientHeldItemChangePacket(BinaryReader reader) {
        this(reader.readShort());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeShort(slot);
    }
}
