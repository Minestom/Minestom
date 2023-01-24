package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.INT;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record EndCombatEventPacket(int duration, int entityId) implements ServerPacket {
    public EndCombatEventPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(INT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, duration);
        writer.write(INT, entityId);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.END_COMBAT_EVENT;
    }
}
