package net.minestom.server.network.packet.client.play;

import net.minestom.server.instance.gamerule.GameRule;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryKey;

import java.util.List;
import java.util.Objects;

public record ClientSetGameRulesPacket(List<Entry> entries) implements ClientPacket.Play {
    public static final NetworkBuffer.Type<ClientSetGameRulesPacket> SERIALIZER = NetworkBufferTemplate.template(
            Entry.NETWORK_TYPE.list(GameRule.staticRegistry().size()), ClientSetGameRulesPacket::entries,
            ClientSetGameRulesPacket::new
    );

    public ClientSetGameRulesPacket {
        entries = List.copyOf(entries);
    }

    public record Entry(RegistryKey<GameRule<?>> key, String value) {
        public static final NetworkBuffer.Type<Entry> NETWORK_TYPE = NetworkBufferTemplate.template(
                RegistryKey.networkType(Registries::gameRule), Entry::key,
                NetworkBuffer.STRING, Entry::value,
                Entry::new);

        public Entry {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(value, "value");
        }
    }
}
