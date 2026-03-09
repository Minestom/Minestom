package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import java.util.List;
import java.util.Objects;

public record ClientSetGameRulesPacket(List<Entry> entries) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientSetGameRulesPacket> SERIALIZER = NetworkBufferTemplate.template(
            Entry.NETWORK_TYPE.list(/* todo(26.1) Gamerule */), ClientSetGameRulesPacket::entries,
            ClientSetGameRulesPacket::new
    );

    public ClientSetGameRulesPacket {
        entries = List.copyOf(entries);
    }

    //TODO(26.1) key should be GameRule key
    public record Entry(String key, String value) {
        public static final NetworkBuffer.Type<Entry> NETWORK_TYPE = NetworkBufferTemplate.template(
                NetworkBuffer.STRING, Entry::key,
                NetworkBuffer.STRING, Entry::value,
                Entry::new);

        public Entry {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(value, "value");
        }
    }
}
