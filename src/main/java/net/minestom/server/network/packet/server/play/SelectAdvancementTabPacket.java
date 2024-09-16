package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record SelectAdvancementTabPacket(@Nullable String identifier) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<SelectAdvancementTabPacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING.optional(), SelectAdvancementTabPacket::identifier,
            SelectAdvancementTabPacket::new);
}
