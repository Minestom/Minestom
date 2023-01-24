package net.minestom.server.network.packet.client.play;

import net.minestom.server.entity.Player;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

public record ClientAnimationPacket(@NotNull Player.Hand hand) implements ClientPacket {
    public ClientAnimationPacket(@NotNull NetworkBuffer reader) {
        this(reader.readEnum(Player.Hand.class));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeEnum(Player.Hand.class, hand);
    }
}
