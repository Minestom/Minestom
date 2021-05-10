package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

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
    public void write(@NotNull BinaryWriter writer) {
        byte flags = 0;
        if (invulnerable)
            flags |= 0x01;
        if (flying)
            flags |= 0x02;
        if (allowFlying)
            flags |= 0x04;
        if (instantBreak)
            flags |= 0x08;

        writer.writeByte(flags);
        writer.writeFloat(flyingSpeed);
        writer.writeFloat(fieldViewModifier);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        byte flags = reader.readByte();
        invulnerable = (flags & 1) == 1;
        flying = (flags & 2) == 2;
        allowFlying = (flags & 4) == 4;
        instantBreak = (flags & 8) == 8;

        flyingSpeed = reader.readFloat();
        fieldViewModifier = reader.readFloat();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PLAYER_ABILITIES;
    }
}
