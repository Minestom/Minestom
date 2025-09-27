package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static net.minestom.server.network.NetworkBuffer.*;

public record ServerDataPacket(Component motd, byte @Nullable [] iconBase64) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<ServerDataPacket> SERIALIZER = NetworkBufferTemplate.template(
            COMPONENT, ServerDataPacket::motd,
            BYTE_ARRAY.optional(), ServerDataPacket::iconBase64,
            ServerDataPacket::new);

    public ServerDataPacket {
        iconBase64 = iconBase64 != null ? iconBase64.clone() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ServerDataPacket(Component motd1, byte[] base64))) return false;
        return motd().equals(motd1) && Arrays.equals(iconBase64(), base64);
    }

    @Override
    public int hashCode() {
        int result = motd().hashCode();
        result = 31 * result + Arrays.hashCode(iconBase64());
        return result;
    }
}
