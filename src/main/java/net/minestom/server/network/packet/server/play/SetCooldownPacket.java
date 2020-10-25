package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class SetCooldownPacket implements ServerPacket {

    public int itemId;
    public int cooldownTicks;

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(itemId);
        writer.writeVarInt(cooldownTicks);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SET_COOLDOWN;
    }
}
