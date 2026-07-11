package net.minestom.server.network.packet.server.common;

import net.kyori.adventure.key.Key;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.validate.Check;

import java.util.Arrays;

public record CookieStorePacket(
        String key, byte[] value
) implements ServerPacket.Configuration, ServerPacket.Play {
    public static final int MAX_VALUE_LENGTH = 5120;

    public static final NetworkBuffer.Type<CookieStorePacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.STRING, CookieStorePacket::key,
            NetworkBuffer.BYTE_ARRAY, CookieStorePacket::value,
            CookieStorePacket::new);

    public CookieStorePacket {
        Check.argCondition(value.length > MAX_VALUE_LENGTH, "Cookie value length too long: {0} > {1}", value.length, MAX_VALUE_LENGTH);
        value = value.clone();
    }

    public CookieStorePacket(Key key, byte[] value) {
        this(key.asString(), value);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof CookieStorePacket(String key1, byte[] value1))) return false;
        return key().equals(key1) && Arrays.equals(value(), value1);
    }

    @Override
    public int hashCode() {
        int result = key().hashCode();
        result = 31 * result + Arrays.hashCode(value());
        return result;
    }
}
