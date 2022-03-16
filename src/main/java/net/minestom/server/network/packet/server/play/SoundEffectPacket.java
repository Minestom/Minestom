package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.sound.Sound.Source;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record SoundEffectPacket(int soundId, @NotNull Source source,
                                int x, int y, int z,
                                float volume, float pitch, long seed) implements ServerPacket {
    public SoundEffectPacket(BinaryReader reader) {
        this(reader.readVarInt(), Source.values()[reader.readVarInt()],
                reader.readInt() * 8, reader.readInt() * 8, reader.readInt() * 8,
                reader.readFloat(), reader.readFloat(), reader.readLong());
    }

    public SoundEffectPacket(@NotNull SoundEvent sound, @NotNull Source source,
                             @NotNull Point position, float volume, float pitch) {
        this(sound.id(), source, (int) position.x(), (int) position.y(), (int) position.z(),
                volume, pitch, 0);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(soundId);
        writer.writeVarInt(AdventurePacketConvertor.getSoundSourceValue(source));
        writer.writeInt(x * 8);
        writer.writeInt(y * 8);
        writer.writeInt(z * 8);
        writer.writeFloat(volume);
        writer.writeFloat(pitch);
        writer.writeLong(seed);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SOUND_EFFECT;
    }
}
