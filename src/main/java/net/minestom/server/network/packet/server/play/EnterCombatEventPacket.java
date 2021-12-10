package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record EnterCombatEventPacket() implements ServerPacket {
    public EnterCombatEventPacket(BinaryReader reader) {
        this();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        // Empty
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTER_COMBAT_EVENT;
    }
}
