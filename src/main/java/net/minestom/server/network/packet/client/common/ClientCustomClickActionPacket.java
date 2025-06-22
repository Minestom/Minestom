package net.minestom.server.network.packet.client.common;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ClientCustomClickActionPacket(@NotNull Key key, @NotNull BinaryTag payload) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientCustomClickActionPacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.KEY, ClientCustomClickActionPacket::key,
            NetworkBuffer.NBT.lengthPrefixed(65536), ClientCustomClickActionPacket::payload,
            ClientCustomClickActionPacket::new);
}
