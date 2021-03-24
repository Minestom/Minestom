package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class EntitySoundEffectPacket implements ServerPacket {

    public int soundId;
    public Sound.Source soundSource;
    public int entityId;
    public float volume;
    public float pitch;

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(soundId);
        writer.writeVarInt(AdventurePacketConvertor.getSoundSourceValue(soundSource));
        writer.writeVarInt(entityId);
        writer.writeFloat(volume);
        writer.writeFloat(pitch);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_SOUND_EFFECT;
    }
}
