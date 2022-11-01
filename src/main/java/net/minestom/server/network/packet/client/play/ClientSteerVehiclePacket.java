package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.FLOAT;

public record ClientSteerVehiclePacket(float sideways, float forward,
                                       byte flags) implements ClientPacket {
    public ClientSteerVehiclePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(FLOAT), reader.read(FLOAT), reader.read(BYTE));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(FLOAT, sideways);
        writer.write(FLOAT, forward);
        writer.write(BYTE, flags);
    }
}
