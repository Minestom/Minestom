package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.client.play.ClientPlayerDiggingPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class AcknowledgePlayerDiggingPacket implements ServerPacket {

    public BlockPosition blockPosition = new BlockPosition(0,0,0);
    public int blockStateId;
    public ClientPlayerDiggingPacket.Status status = ClientPlayerDiggingPacket.Status.STARTED_DIGGING;
    public boolean successful;

    public AcknowledgePlayerDiggingPacket() {}

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBlockPosition(blockPosition);
        writer.writeVarInt(blockStateId);
        writer.writeVarInt(status.ordinal());
        writer.writeBoolean(successful);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        blockPosition = reader.readBlockPosition();
        blockStateId = reader.readVarInt();
        status = ClientPlayerDiggingPacket.Status.values()[reader.readVarInt()];
        successful = reader.readBoolean();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ACKNOWLEDGE_PLAYER_DIGGING;
    }
}
