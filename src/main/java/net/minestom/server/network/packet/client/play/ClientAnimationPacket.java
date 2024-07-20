package net.minestom.server.network.packet.client.play;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

public record ClientAnimationPacket(@NotNull PlayerHand hand) implements ClientPacket {
    public ClientAnimationPacket(@NotNull NetworkBuffer reader) {
        this(reader.readEnum(PlayerHand.class));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeEnum(PlayerHand.class, hand);
    }
}
