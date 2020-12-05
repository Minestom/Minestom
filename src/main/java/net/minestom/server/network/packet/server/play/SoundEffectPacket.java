package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.sound.Sound;
import net.minestom.server.sound.SoundCategory;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class SoundEffectPacket implements ServerPacket {

    public int soundId;
    public SoundCategory soundCategory;
    public int x, y, z;
    public float volume;
    public float pitch;

    public static SoundEffectPacket create(SoundCategory category, Sound sound, Position position, float volume, float pitch) {
        SoundEffectPacket packet = new SoundEffectPacket();
        packet.soundId = sound.getId();
        packet.soundCategory = category;
        // *8 converts to fixed-point representation with 3 bits for fractional part
        packet.x = (int) position.getX();
        packet.y = (int) position.getY();
        packet.z = (int) position.getZ();
        packet.volume = volume;
        packet.pitch = pitch;
        return packet;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(soundId);
        writer.writeVarInt(soundCategory.ordinal());
        writer.writeInt(x * 8);
        writer.writeInt(y * 8);
        writer.writeInt(z * 8);
        writer.writeFloat(volume);
        writer.writeFloat(pitch);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SOUND_EFFECT;
    }
}
