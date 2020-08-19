package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.sound.Sound;
import net.minestom.server.sound.SoundCategory;
import net.minestom.server.utils.binary.BinaryWriter;

public class SoundEffectPacket implements ServerPacket {

    public int soundId;
    public SoundCategory soundCategory;
    public int x, y, z;
    public float volume;
    public float pitch;

    public static SoundEffectPacket create(SoundCategory category, Sound sound, float x, float y, float z, float volume, float pitch) {
        SoundEffectPacket packet = new SoundEffectPacket();
        packet.soundId = sound.getId();
        packet.soundCategory = category;
        // *8 converts to fixed-point representation with 3 bits for fractional part
        packet.x = (int) (x * 8);
        packet.y = (int) (y * 8);
        packet.z = (int) (z * 8);
        packet.volume = volume;
        packet.pitch = pitch;
        return packet;
    }

    @Override
    public void write(BinaryWriter writer) {
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
