package net.minestom.server.network.packet.client.play;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

public record ClientAnimationPacket(LivingEntity.@NotNull Hand hand) implements ClientPacket {
    public ClientAnimationPacket(@NotNull NetworkBuffer reader) {
        this(reader.readEnum(LivingEntity.Hand.class));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeEnum(LivingEntity.Hand.class, hand);
    }
}
