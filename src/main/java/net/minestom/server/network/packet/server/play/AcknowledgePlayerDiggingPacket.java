package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.client.play.ClientPlayerDiggingPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class AcknowledgePlayerDiggingPacket implements ServerPacket {

    public BlockPosition blockPosition;
    public int blockStateId;
    public ClientPlayerDiggingPacket.Status status;
    public boolean successful;

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBlockPosition(blockPosition);
        writer.writeVarInt(blockStateId);
        writer.writeVarInt(status.ordinal());
        writer.writeBoolean(successful);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ACKNOWLEDGE_PLAYER_DIGGING;
    }
}
