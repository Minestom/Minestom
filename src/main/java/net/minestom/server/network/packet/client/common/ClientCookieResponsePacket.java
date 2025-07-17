package net.minestom.server.network.packet.client.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.server.common.CookieStorePacket;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.BYTE_ARRAY;
import static net.minestom.server.network.NetworkBuffer.STRING;

public record ClientCookieResponsePacket(
        @NotNull String key,
        byte @Nullable [] value
) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientCookieResponsePacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING, ClientCookieResponsePacket::key,
            BYTE_ARRAY.optional(), ClientCookieResponsePacket::value,
            ClientCookieResponsePacket::new);

    public ClientCookieResponsePacket {
        Check.argCondition(value != null && value.length > CookieStorePacket.MAX_VALUE_LENGTH,
                "Value is too long: {0} > {1}", value != null ? value.length : 0, CookieStorePacket.MAX_VALUE_LENGTH);
    }
}
