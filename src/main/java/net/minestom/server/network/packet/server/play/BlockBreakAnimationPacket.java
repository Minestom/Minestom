package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record BlockBreakAnimationPacket(int entityId, @NotNull Point blockPosition,
                                        byte destroyStage) implements ServerPacket {
    public BlockBreakAnimationPacket(BinaryReader reader) {
        this(reader.readVarInt(), reader.readBlockPosition(), reader.readByte());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeBlockPosition(blockPosition);
        writer.writeByte(destroyStage);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.BLOCK_BREAK_ANIMATION;
    }
}