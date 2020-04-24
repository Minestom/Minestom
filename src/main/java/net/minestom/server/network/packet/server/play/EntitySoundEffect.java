package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.sound.SoundCategory;

public class EntitySoundEffect implements ServerPacket {

    public int soundId;
    public SoundCategory soundCategory;
    public int entityId;
    public float volume;
    public float pitch;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(soundId);
        writer.writeVarInt(soundCategory.ordinal());
        writer.writeVarInt(entityId);
        writer.writeFloat(volume);
        writer.writeFloat(pitch);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_SOUND_EFFECT;
    }
}
