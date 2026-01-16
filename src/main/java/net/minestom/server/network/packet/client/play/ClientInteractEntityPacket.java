package net.minestom.server.network.packet.client.play;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.ApiStatus;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientInteractEntityPacket(int targetId, Type type, boolean sneaking) implements ClientPacket.Play {
    public static final NetworkBuffer.Type<ClientInteractEntityPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, ClientInteractEntityPacket::targetId,
  /*VarInt*/BYTE.unionType(Type::serializer, Type::id), ClientInteractEntityPacket::type,
            BOOLEAN, ClientInteractEntityPacket::sneaking,
            ClientInteractEntityPacket::new
    );

    public sealed interface Type {
        private static NetworkBuffer.Type<? extends Type> serializer(byte id) {
            return switch (id) {
                case 0 -> Interact.SERIALIZER;
                case 1 -> Attack.SERIALIZER;
                case 2 -> InteractAt.SERIALIZER;
                default -> throw new RuntimeException("Unknown action id: " + id);
            };
        }

        @ApiStatus.OverrideOnly
        byte id();
    }

    public record Interact(PlayerHand hand) implements Type {
        public static final NetworkBuffer.Type<Interact> SERIALIZER = NetworkBufferTemplate.template(
                PlayerHand.NETWORK_TYPE, Interact::hand,
                Interact::new
        );

        @Override
        public byte id() {
            return 0;
        }
    }

    public record Attack() implements Type {
        public static final Attack INSTANCE = new Attack();
        public static final NetworkBuffer.Type<Attack> SERIALIZER = NetworkBufferTemplate.template(INSTANCE);

        @Override
        public byte id() {
            return 1;
        }
    }

    public record InteractAt(float targetX, float targetY, float targetZ,
                             PlayerHand hand) implements Type {
        public static final NetworkBuffer.Type<InteractAt> SERIALIZER = NetworkBufferTemplate.template(
                FLOAT, InteractAt::targetX,
                FLOAT, InteractAt::targetY,
                FLOAT, InteractAt::targetZ,
                PlayerHand.NETWORK_TYPE, InteractAt::hand,
                InteractAt::new
        );

        @Override
        public byte id() {
            return 2;
        }
    }
}
