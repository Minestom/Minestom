package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class PlayerAbilitiesPacket implements ServerPacket {

    // Flags
    public boolean invulnerable;
    public boolean flying;
    public boolean allowFlying;
    public boolean instantBreak;

    // Options
    public float flyingSpeed;
    public float fieldViewModifier;

    @Override
    public void write(PacketWriter writer) {
        byte flags = 0;
        if (invulnerable)
            flags += 1;
        if (flying)
            flags += 2;
        if (allowFlying)
            flags += 4;
        if (instantBreak)
            flags += 8;

        writer.writeByte(flags);
        writer.writeFloat(flyingSpeed);
        writer.writeFloat(fieldViewModifier);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PLAYER_ABILITIES;
    }
}
