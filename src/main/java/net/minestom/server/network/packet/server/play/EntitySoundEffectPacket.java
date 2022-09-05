package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record EntitySoundEffectPacket(int soundId, Sound.Source source, int entityId,
                                      float volume, float pitch, long seed) implements ServerPacket {
    public EntitySoundEffectPacket(BinaryReader reader) {
        this(reader.readVarInt(), Sound.Source.values()[reader.readVarInt()], reader.readVarInt(),
                reader.readFloat(), reader.readFloat(), reader.readLong());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(soundId);
        writer.writeVarInt(AdventurePacketConvertor.getSoundSourceValue(source));
        writer.writeVarInt(entityId);
        writer.writeFloat(volume);
        writer.writeFloat(pitch);
        writer.writeLong(seed);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_SOUND_EFFECT;
    }
}
