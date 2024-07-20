package net.minestom.server.network.packet.client.play;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ClientUseItemPacket(@NotNull PlayerHand hand, int sequence, float yaw, float pitch) implements ClientPacket {
    public ClientUseItemPacket(@NotNull NetworkBuffer reader) {
        this(reader.readEnum(PlayerHand.class), reader.read(VAR_INT),
                reader.read(NetworkBuffer.FLOAT), reader.read(NetworkBuffer.FLOAT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeEnum(PlayerHand.class, hand);
        writer.write(VAR_INT, sequence);
        writer.write(NetworkBuffer.FLOAT, yaw);
        writer.write(NetworkBuffer.FLOAT, pitch);
    }
}
