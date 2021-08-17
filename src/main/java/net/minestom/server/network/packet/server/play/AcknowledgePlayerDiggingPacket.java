package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.client.play.ClientPlayerDiggingPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class AcknowledgePlayerDiggingPacket implements ServerPacket {

    public Point blockPosition;
    public int blockStateId;
    public ClientPlayerDiggingPacket.Status status;
    public boolean successful;

    public AcknowledgePlayerDiggingPacket(@NotNull Point blockPosition, int blockStateId,
                                          @NotNull ClientPlayerDiggingPacket.Status status, boolean success) {
        this.blockPosition = blockPosition;
        this.blockStateId = blockStateId;
        this.status = status;
        this.successful = success;
    }

    public AcknowledgePlayerDiggingPacket(@NotNull Point blockPosition, Block block,
                                          @NotNull ClientPlayerDiggingPacket.Status status, boolean success) {
        this(blockPosition, block.stateId(), status, success);
    }

    public AcknowledgePlayerDiggingPacket() {
        this(Vec.ZERO, 0, ClientPlayerDiggingPacket.Status.STARTED_DIGGING, false);
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
        this.blockPosition = reader.readBlockPosition();
        this.blockStateId = reader.readVarInt();
        this.status = ClientPlayerDiggingPacket.Status.values()[reader.readVarInt()];
        this.successful = reader.readBoolean();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ACKNOWLEDGE_PLAYER_DIGGING;
    }
}
