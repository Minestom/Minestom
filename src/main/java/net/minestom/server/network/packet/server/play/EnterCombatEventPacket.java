package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryBuffer;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class EnterCombatEventPacket  implements ServerPacket {

    @Override
    public void read(@NotNull BinaryBuffer reader) {
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTER_COMBAT_EVENT;
    }
}
