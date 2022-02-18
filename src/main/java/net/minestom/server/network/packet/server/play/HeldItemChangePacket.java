package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record HeldItemChangePacket(byte slot) implements ServerPacket {
    public HeldItemChangePacket(BinaryReader reader) {
        this(reader.readByte());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(slot);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.HELD_ITEM_CHANGE;
    }
}
