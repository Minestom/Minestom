package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.FLOAT;

public record PlayerAbilitiesPacket(byte flags, float flyingSpeed, float walkingSpeed) implements ServerPacket.Play {
    public static final byte FLAG_INVULNERABLE = 0x01;
    public static final byte FLAG_FLYING = 0x02;
    public static final byte FLAG_ALLOW_FLYING = 0x04;
    public static final byte FLAG_INSTANT_BREAK = 0x08;

    public PlayerAbilitiesPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(BYTE), reader.read(FLOAT), reader.read(FLOAT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(BYTE, flags);
        writer.write(FLOAT, flyingSpeed);
        writer.write(FLOAT, walkingSpeed);
    }

}
