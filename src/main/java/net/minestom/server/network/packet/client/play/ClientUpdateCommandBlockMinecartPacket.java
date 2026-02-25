package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientUpdateCommandBlockMinecartPacket(int entityId, String command,
                                                     boolean trackOutput) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientUpdateCommandBlockMinecartPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, ClientUpdateCommandBlockMinecartPacket::entityId,
            STRING, ClientUpdateCommandBlockMinecartPacket::command,
            BOOLEAN, ClientUpdateCommandBlockMinecartPacket::trackOutput,
            ClientUpdateCommandBlockMinecartPacket::new);
}
