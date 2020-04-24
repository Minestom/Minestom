package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.sound.SoundCategory;

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
