package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

public class StopSoundPacket implements ServerPacket {

    public byte flags;
    public int source;
    public String sound;

    @Override
    public void write(PacketWriter writer) {
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
