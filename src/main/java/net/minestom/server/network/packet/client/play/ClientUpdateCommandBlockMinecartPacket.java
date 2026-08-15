package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.validate.Check;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;
import static net.minestom.server.network.NetworkBuffer.STRING;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ClientUpdateCommandBlockMinecartPacket(int entityId, String command,
                                                     boolean trackOutput) implements ClientPacket.Play {
    public static final NetworkBuffer.Type<ClientUpdateCommandBlockMinecartPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, ClientUpdateCommandBlockMinecartPacket::entityId,
            STRING, ClientUpdateCommandBlockMinecartPacket::command,
            BOOLEAN, ClientUpdateCommandBlockMinecartPacket::trackOutput,
            ClientUpdateCommandBlockMinecartPacket::new);

    public ClientUpdateCommandBlockMinecartPacket {
        Check.argCondition(command.length() > Short.MAX_VALUE, "Command length cannot be greater than Short.MAX_VALUE");
    }
}
