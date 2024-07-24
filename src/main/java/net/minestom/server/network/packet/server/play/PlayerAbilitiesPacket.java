package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.FLOAT;

public record PlayerAbilitiesPacket(byte flags, float flyingSpeed, float walkingSpeed) implements ServerPacket.Play {
    public static final byte FLAG_INVULNERABLE = 0x01;
    public static final byte FLAG_FLYING = 0x02;
    public static final byte FLAG_ALLOW_FLYING = 0x04;
    public static final byte FLAG_INSTANT_BREAK = 0x08;

    public static final NetworkBuffer.Type<PlayerAbilitiesPacket> SERIALIZER = NetworkBufferTemplate.template(
            BYTE, PlayerAbilitiesPacket::flags,
            FLOAT, PlayerAbilitiesPacket::flyingSpeed,
            FLOAT, PlayerAbilitiesPacket::walkingSpeed,
            PlayerAbilitiesPacket::new);
}
