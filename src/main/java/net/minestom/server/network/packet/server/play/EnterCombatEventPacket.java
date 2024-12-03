package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

public record EnterCombatEventPacket() implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EnterCombatEventPacket> SERIALIZER = NetworkBufferTemplate.template(EnterCombatEventPacket::new);
}
