package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

public class EntityAnimationPacket implements ServerPacket {

    public int entityId;
    public Animation animation;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeByte((byte) animation.ordinal());
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_ANIMATION;
    }

    public enum Animation {
        SWING_MAIN_ARM,
        TAKE_DAMAGE,
        LEAVE_BED,
        SWING_OFF_HAND,
        CRITICAL_EFFECT,
        MAGICAL_CRITICAL_EFFECT;
    }
}
