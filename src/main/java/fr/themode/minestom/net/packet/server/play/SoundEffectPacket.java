package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;
import fr.themode.minestom.sound.SoundCategory;

public class SoundEffectPacket implements ServerPacket {

    public int soundId;
    public SoundCategory soundCategory;
    public int x, y, z;
    public float volume;
    public float pitch;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(soundId);
        writer.writeVarInt(soundCategory.ordinal());
        writer.writeInt(x);
        writer.writeInt(y);
        writer.writeInt(z);
        writer.writeFloat(volume);
        writer.writeFloat(pitch);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SOUND_EFFECT;
    }
}
