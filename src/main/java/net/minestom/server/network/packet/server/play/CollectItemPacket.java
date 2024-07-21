package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record CollectItemPacket(int collectedEntityId, int collectorEntityId, int pickupItemCount)
        implements ServerPacket.Play {
    public CollectItemPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(VAR_INT), reader.read(VAR_INT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, collectedEntityId);
        writer.write(VAR_INT, collectorEntityId);
        writer.write(VAR_INT, pickupItemCount);
    }

}
