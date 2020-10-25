package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class WindowPropertyPacket implements ServerPacket {

    public byte windowId;
    public short property;
    public short value;

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(windowId);
        writer.writeShort(property);
        writer.writeShort(value);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.WINDOW_PROPERTY;
    }
}
