package net.minestom.server.network.packet.client.play;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientInteractEntityPacket(int targetId, @NotNull Type type, boolean sneaking) implements ClientPacket {

    public static final NetworkBuffer.Type<ClientInteractEntityPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, ClientInteractEntityPacket value) {
            buffer.write(VAR_INT, value.targetId);
            buffer.write(VAR_INT, value.type.id());
            @SuppressWarnings("unchecked") NetworkBuffer.Type<Type> serializer = (NetworkBuffer.Type<Type>) typeSerializer(value.type.id());
            buffer.write(serializer, value.type);
            buffer.write(BOOLEAN, value.sneaking);
        }

        @Override
        public ClientInteractEntityPacket read(@NotNull NetworkBuffer buffer) {
            final int targetId = buffer.read(VAR_INT);
            final Type type = typeSerializer(buffer.read(VAR_INT)).read(buffer);
            final boolean sneaking = buffer.read(BOOLEAN);
            return new ClientInteractEntityPacket(targetId, type, sneaking);
        }
    };

    private static NetworkBuffer.Type<? extends Type> typeSerializer(int id) {
        return switch (id) {
            case 0 -> Interact.SERIALIZER;
            case 1 -> Attack.SERIALIZER;
            case 2 -> InteractAt.SERIALIZER;
            default -> throw new RuntimeException("Unknown action id");
        };
    }

    public sealed interface Type permits Interact, Attack, InteractAt {
        int id();
    }

    public record Interact(@NotNull PlayerHand hand) implements Type {
        public static final NetworkBuffer.Type<Interact> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.Enum(PlayerHand.class), Interact::hand,
                Interact::new
        );

        @Override
        public int id() {
            return 0;
        }
    }

    public record Attack() implements Type {
        public static final NetworkBuffer.Type<Attack> SERIALIZER = NetworkBufferTemplate.template(Attack::new);

        @Override
        public int id() {
            return 1;
        }
    }

    public record InteractAt(float targetX, float targetY, float targetZ,
                             @NotNull PlayerHand hand) implements Type {
        public static final NetworkBuffer.Type<InteractAt> SERIALIZER = NetworkBufferTemplate.template(
                FLOAT, InteractAt::targetX,
                FLOAT, InteractAt::targetY,
                FLOAT, InteractAt::targetZ,
                NetworkBuffer.Enum(PlayerHand.class), InteractAt::hand,
                InteractAt::new
        );

        @Override
        public int id() {
            return 2;
        }
    }
}
