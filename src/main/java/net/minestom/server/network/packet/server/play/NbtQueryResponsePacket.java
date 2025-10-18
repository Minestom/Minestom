package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.*;

public record NbtQueryResponsePacket(int transactionId, @Nullable CompoundBinaryTag data) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<NbtQueryResponsePacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, NbtQueryResponsePacket::transactionId,
            OPTIONAL_NBT_COMPOUND, NbtQueryResponsePacket::data,
            NbtQueryResponsePacket::new
    );
}
