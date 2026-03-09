package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import java.util.Map;

//todo(26.1) gamerule key
public record GameRuleValuesPacket(Map<String, String> values) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<GameRuleValuesPacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.STRING.mapValue(NetworkBuffer.STRING), GameRuleValuesPacket::values,
            GameRuleValuesPacket::new
    );

    public GameRuleValuesPacket {
        values = Map.copyOf(values);
    }
}
