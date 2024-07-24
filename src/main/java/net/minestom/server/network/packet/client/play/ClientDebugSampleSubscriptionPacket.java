package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.server.play.DebugSamplePacket;
import org.jetbrains.annotations.NotNull;

public record ClientDebugSampleSubscriptionPacket(@NotNull DebugSamplePacket.Type type) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientDebugSampleSubscriptionPacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.Enum(DebugSamplePacket.Type.class), ClientDebugSampleSubscriptionPacket::type,
            ClientDebugSampleSubscriptionPacket::new);
}
