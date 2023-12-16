package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import static net.minestom.server.network.NetworkBuffer.*;

public record BlockEntityDataPacket(@NotNull Point blockPosition, int action,
                                    @Nullable NBTCompound data) implements ServerPacket {
    public BlockEntityDataPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(BLOCK_POSITION), reader.read(VAR_INT), (NBTCompound) reader.read(NBT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(BLOCK_POSITION, blockPosition);
        writer.write(VAR_INT, action);
        if (data != null) {
            writer.write(NBT, data);
        } else {
            // TAG_End
            writer.write(BYTE, (byte) 0x00);
        }
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case PLAY -> ServerPacketIdentifier.BLOCK_ENTITY_DATA;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.PLAY);
        };
    }
}
