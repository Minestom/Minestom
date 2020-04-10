package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;
import fr.themode.minestom.sound.SoundCategory;

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
