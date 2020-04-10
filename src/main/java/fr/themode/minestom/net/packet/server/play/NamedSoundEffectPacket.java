package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;
import fr.themode.minestom.sound.SoundCategory;

public class NamedSoundEffectPacket implements ServerPacket {

    public String soundName;
    public SoundCategory soundCategory;
    public int x, y, z;
    public float volume;
    public float pitch;

    @Override
    public void write(PacketWriter writer) {
        writer.writeSizedString(soundName);
        writer.writeVarInt(soundCategory.ordinal());
        writer.writeInt(x);
        writer.writeInt(y);
        writer.writeInt(z);
        writer.writeFloat(volume);
        writer.writeFloat(pitch);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.NAMED_SOUND_EFFECT;
    }
}
