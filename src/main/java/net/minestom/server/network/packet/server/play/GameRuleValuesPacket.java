package net.minestom.server.network.packet.server.play;

import net.minestom.server.instance.gamerule.GameRule;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryKey;

import java.util.Map;

public record GameRuleValuesPacket(Map<RegistryKey<GameRule<?>>, String> values) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<GameRuleValuesPacket> SERIALIZER = NetworkBufferTemplate.template(
            RegistryKey.networkType(Registries::gameRule).mapValue(NetworkBuffer.STRING, GameRule.staticRegistry().size()), GameRuleValuesPacket::values,
            GameRuleValuesPacket::new
    );

    public GameRuleValuesPacket {
        values = Map.copyOf(values);
    }
}
