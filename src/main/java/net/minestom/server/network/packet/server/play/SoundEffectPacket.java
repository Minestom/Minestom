package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.sound.Sound.Source;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

public class SoundEffectPacket implements ServerPacket {

    public int soundId;
    public Source soundSource;
    public int x, y, z;
    public float volume;
    public float pitch;

    public SoundEffectPacket() {
        soundSource = Source.AMBIENT;
    }

    @NotNull
    public static SoundEffectPacket create(Source category, SoundEvent sound, Pos position, float volume, float pitch) {
        SoundEffectPacket packet = new SoundEffectPacket();
        packet.soundId = sound.id();
        packet.soundSource = category;
        // *8 converts to fixed-point representation with 3 bits for fractional part
        packet.x = (int) position.x();
        packet.y = (int) position.y();
        packet.z = (int) position.z();
        packet.volume = volume;
        packet.pitch = pitch;
        return packet;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(soundId);
        writer.writeVarInt(AdventurePacketConvertor.getSoundSourceValue(soundSource));
        writer.writeInt(x * 8);
        writer.writeInt(y * 8);
        writer.writeInt(z * 8);
        writer.writeFloat(volume);
        writer.writeFloat(pitch);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        soundId = reader.readVarInt();
        soundSource = Source.values()[reader.readVarInt()];
        x = reader.readInt() / 8;
        y = reader.readInt() / 8;
        z = reader.readInt() / 8;
        volume = reader.readFloat();
        pitch = reader.readFloat();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SOUND_EFFECT;
    }
}
