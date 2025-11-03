package net.minestom.server.network.packet.server.configuration;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

public record CodeOfConductPacket(
        String codeOfConduct
) implements ServerPacket.Configuration {
    public static final NetworkBuffer.Type<CodeOfConductPacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.STRING, CodeOfConductPacket::codeOfConduct,
            CodeOfConductPacket::new);
}
