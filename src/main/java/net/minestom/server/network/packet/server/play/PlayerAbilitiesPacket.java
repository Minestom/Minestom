package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

public class PlayerAbilitiesPacket implements ServerPacket {

    // Flags
    public boolean invulnerable;
    public boolean flying;
    public boolean allowFlying;
    public boolean instantBreak;

    // Options
    public float flyingSpeed;
    public float walkingSpeed;

    @Override
    public void write(BinaryWriter writer) {
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
        writer.writeFloat(walkingSpeed);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PLAYER_ABILITIES;
    }
}
