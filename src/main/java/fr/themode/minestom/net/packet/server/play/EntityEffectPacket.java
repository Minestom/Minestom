package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;

public class EntityEffectPacket implements ServerPacket {

    public int entityId;
    public byte effectId;
    public byte amplifier;
    public int duration;
    public byte flags;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeByte(effectId);
        writer.writeByte(amplifier);
        writer.writeVarInt(duration);
        writer.writeByte(flags);
    }

    @Override
    public int getId() {
        return 0x59;
    }
}
