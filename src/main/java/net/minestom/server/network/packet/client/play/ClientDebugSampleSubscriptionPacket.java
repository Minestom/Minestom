package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.server.play.DebugSamplePacket;
import org.jetbrains.annotations.NotNull;

public record ClientDebugSampleSubscriptionPacket(@NotNull DebugSamplePacket.Type type) implements ClientPacket {

    public ClientDebugSampleSubscriptionPacket(@NotNull NetworkBuffer reader) {
        this(reader.readEnum(DebugSamplePacket.Type.class));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeEnum(DebugSamplePacket.Type.class, type);
    }
}
