package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

public record EnterCombatEventPacket() implements ServerPacket.Play {
    public EnterCombatEventPacket(@NotNull NetworkBuffer reader) {
        this();
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        // Empty
    }

    @Override
    public int playId() {
        return ServerPacketIdentifier.ENTER_COMBAT_EVENT;
    }
}
