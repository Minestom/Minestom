package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record PlayerAbilitiesPacket(byte flags, float flyingSpeed, float fieldViewModifier) implements ServerPacket {
    public static final byte FLAG_INVULNERABLE = 0x01;
    public static final byte FLAG_FLYING = 0x02;
    public static final byte FLAG_ALLOW_FLYING = 0x04;
    public static final byte FLAG_INSTANT_BREAK = 0x08;

    public PlayerAbilitiesPacket(BinaryReader reader) {
        this(reader.readByte(), reader.readFloat(), reader.readFloat());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(flags);
        writer.writeFloat(flyingSpeed);
        writer.writeFloat(fieldViewModifier);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PLAYER_ABILITIES;
    }
}
