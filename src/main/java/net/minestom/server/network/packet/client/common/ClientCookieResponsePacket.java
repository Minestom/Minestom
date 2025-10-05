package net.minestom.server.network.packet.client.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.server.common.CookieStorePacket;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static net.minestom.server.network.NetworkBuffer.BYTE_ARRAY;
import static net.minestom.server.network.NetworkBuffer.STRING;

public record ClientCookieResponsePacket(
        String key,
        byte @Nullable [] value
) implements ClientPacket.Login, ClientPacket.Configuration, ClientPacket.Play {
    public static final NetworkBuffer.Type<ClientCookieResponsePacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING, ClientCookieResponsePacket::key,
            BYTE_ARRAY.optional(), ClientCookieResponsePacket::value,
            ClientCookieResponsePacket::new);

    public ClientCookieResponsePacket {
        Check.argCondition(value != null && value.length > CookieStorePacket.MAX_VALUE_LENGTH,
                "Value is too long: {0} > {1}", value != null ? value.length : 0, CookieStorePacket.MAX_VALUE_LENGTH);
        value = value != null ? value.clone() : null;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ClientCookieResponsePacket(String key1, byte[] value1))) return false;
        return key().equals(key1) && Arrays.equals(value(), value1);
    }

    @Override
    public int hashCode() {
        int result = key().hashCode();
        result = 31 * result + Arrays.hashCode(value());
        return result;
    }
}
