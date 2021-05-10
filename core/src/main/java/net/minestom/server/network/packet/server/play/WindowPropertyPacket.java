package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class WindowPropertyPacket implements ServerPacket {

    public byte windowId;
    public short property;
    public short value;

    /**
     * Default constructor, required for reflection operations.
     */
    public WindowPropertyPacket() {}

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(windowId);
        writer.writeShort(property);
        writer.writeShort(value);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        windowId = reader.readByte();
        property = reader.readShort();
        value = reader.readShort();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.WINDOW_PROPERTY;
    }
}
