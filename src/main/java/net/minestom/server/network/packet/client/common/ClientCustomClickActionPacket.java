package net.minestom.server.network.packet.client.common;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

public record ClientCustomClickActionPacket(Key key, BinaryTag payload) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientCustomClickActionPacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.KEY, ClientCustomClickActionPacket::key,
            NetworkBuffer.NBT.lengthPrefixed(65536), ClientCustomClickActionPacket::payload,
            ClientCustomClickActionPacket::new);
}
