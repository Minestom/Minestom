package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.validate.Check;

import static net.minestom.server.network.NetworkBuffer.BLOCK_POSITION;
import static net.minestom.server.network.NetworkBuffer.STRING;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ClientUpdateJigsawBlockPacket(
        Point location,
        String name,
        String target,
        String pool,
        String finalState,
        String jointType,
        int selectionPriority,
        int placementPriority
) implements ClientPacket.Play {
    public static final NetworkBuffer.Type<ClientUpdateJigsawBlockPacket> SERIALIZER = NetworkBufferTemplate.template(
            BLOCK_POSITION, ClientUpdateJigsawBlockPacket::location,
            STRING, ClientUpdateJigsawBlockPacket::name,
            STRING, ClientUpdateJigsawBlockPacket::target,
            STRING, ClientUpdateJigsawBlockPacket::pool,
            STRING, ClientUpdateJigsawBlockPacket::finalState,
            STRING, ClientUpdateJigsawBlockPacket::jointType,
            VAR_INT, ClientUpdateJigsawBlockPacket::selectionPriority,
            VAR_INT, ClientUpdateJigsawBlockPacket::placementPriority,
            ClientUpdateJigsawBlockPacket::new);

    public ClientUpdateJigsawBlockPacket {
        Check.argCondition(name.length() > Short.MAX_VALUE, "Name length cannot be greater than Short.MAX_VALUE");
        Check.argCondition(target.length() > Short.MAX_VALUE, "Target length cannot be greater than Short.MAX_VALUE");
        Check.argCondition(pool.length() > Short.MAX_VALUE, "Pool length cannot be greater than Short.MAX_VALUE");
        Check.argCondition(finalState.length() > Short.MAX_VALUE, "Final state length cannot be greater than Short.MAX_VALUE");
        Check.argCondition(jointType.length() > Short.MAX_VALUE, "Joint type length cannot be greater than Short.MAX_VALUE");
    }
}
