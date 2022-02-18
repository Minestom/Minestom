package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientUpdateCommandBlockPacket(@NotNull Point blockPosition, @NotNull String command,
                                             @NotNull Mode mode, byte flags) implements ClientPacket {
    public ClientUpdateCommandBlockPacket(BinaryReader reader) {
        this(reader.readBlockPosition(), reader.readSizedString(Short.MAX_VALUE),
                Mode.values()[reader.readVarInt()], reader.readByte());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBlockPosition(blockPosition);
        writer.writeSizedString(command);
        writer.writeVarInt(mode.ordinal());
        writer.writeByte(flags);
    }

    public enum Mode {
        SEQUENCE, AUTO, REDSTONE
    }
}
