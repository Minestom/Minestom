package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.BlockEntityType;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.*;

public record BlockEntityDataPacket(Point blockPosition,
                                    BlockEntityType type,
                                    @Nullable CompoundBinaryTag data
) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<BlockEntityDataPacket> SERIALIZER = NetworkBufferTemplate.template(
            BLOCK_POSITION, BlockEntityDataPacket::blockPosition,
            BlockEntityType.NETWORK_TYPE, BlockEntityDataPacket::type,
            OPTIONAL_NBT_COMPOUND, BlockEntityDataPacket::data,
            BlockEntityDataPacket::new
    );
}
