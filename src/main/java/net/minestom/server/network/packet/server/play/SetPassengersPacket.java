package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record SetPassengersPacket(int vehicleEntityId,
                                  @NotNull List<Integer> passengersId) implements ServerPacket.Play {
    public static final int MAX_PASSENGERS = 16384;

    public static final NetworkBuffer.Type<SetPassengersPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, SetPassengersPacket::vehicleEntityId,
            VAR_INT.list(MAX_PASSENGERS), SetPassengersPacket::passengersId,
            SetPassengersPacket::new);

    public SetPassengersPacket {
        passengersId = List.copyOf(passengersId);
    }
}
