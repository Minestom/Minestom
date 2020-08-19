package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.sound.SoundCategory;
import net.minestom.server.utils.binary.BinaryWriter;

public class NamedSoundEffectPacket implements ServerPacket {

    public String soundName;
    public SoundCategory soundCategory;
    public int x, y, z;
    public float volume;
    public float pitch;

    @Override
    public void write(BinaryWriter writer) {
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
