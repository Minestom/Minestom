package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.client.play.ClientPlayerDiggingPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class AcknowledgePlayerDiggingPacket implements ServerPacket {

    public BlockPosition blockPosition;
    public int blockStateId;
    public ClientPlayerDiggingPacket.Status status;
    public boolean successful;

    public AcknowledgePlayerDiggingPacket(@NotNull BlockPosition blockPosition, int blockStateId,
                                          @NotNull ClientPlayerDiggingPacket.Status status, boolean success) {
        this.blockPosition = blockPosition;
        this.blockStateId = blockStateId;
        this.status = status;
        this.successful = success;
    }

    public AcknowledgePlayerDiggingPacket() {
        this(new BlockPosition(0, 0, 0), 0, ClientPlayerDiggingPacket.Status.STARTED_DIGGING, false);
    }

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
