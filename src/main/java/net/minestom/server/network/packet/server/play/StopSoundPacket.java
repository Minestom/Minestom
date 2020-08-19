package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

public class StopSoundPacket implements ServerPacket {

    public byte flags;
    public int source;
    public String sound;

    @Override
    public void write(BinaryWriter writer) {
        writer.writeByte(flags);
        if (flags == 3 || flags == 1)
            writer.writeVarInt(source);
        if (flags == 2 || flags == 3)
            writer.writeSizedString(sound);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.STOP_SOUND;
    }
}
