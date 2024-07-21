package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record SetPassengersPacket(int vehicleEntityId,
                                  @NotNull List<Integer> passengersId) implements ServerPacket.Play {
    public static final int MAX_PASSENGERS = 16384;

    public SetPassengersPacket {
        passengersId = List.copyOf(passengersId);
    }

    public SetPassengersPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.readCollection(VAR_INT, MAX_PASSENGERS));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, vehicleEntityId);
        writer.writeCollection(VAR_INT, passengersId);
    }

}
