package net.minestom.server.network.packet.client.play;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientInteractEntityPacket(int targetId, @NotNull Type type, boolean sneaking) implements ClientPacket {

    public static NetworkBuffer.Type<ClientInteractEntityPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, ClientInteractEntityPacket value) {
            buffer.write(VAR_INT, value.targetId);
            buffer.write(VAR_INT, value.type.id());
            buffer.write(value.type);
            buffer.write(BOOLEAN, value.sneaking);
        }

        @Override
        public ClientInteractEntityPacket read(@NotNull NetworkBuffer buffer) {
            return new ClientInteractEntityPacket(buffer.read(VAR_INT), switch (buffer.read(VAR_INT)) {
                case 0 -> new Interact(buffer);
                case 1 -> new Attack();
                case 2 -> new InteractAt(buffer);
                default -> throw new RuntimeException("Unknown action id");
            }, buffer.read(BOOLEAN));
        }
    };

    public sealed interface Type extends Writer
            permits Interact, Attack, InteractAt {
        int id();
    }

    public record Interact(@NotNull PlayerHand hand) implements Type {
        public Interact(@NotNull NetworkBuffer reader) {
            this(reader.readEnum(PlayerHand.class));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.writeEnum(PlayerHand.class, hand);
        }

        @Override
        public int id() {
            return 0;
        }
    }

    public record Attack() implements Type {
        @Override
        public void write(@NotNull NetworkBuffer writer) {
            // Empty
        }

        @Override
        public int id() {
            return 1;
        }
    }

    public record InteractAt(float targetX, float targetY, float targetZ,
                             @NotNull PlayerHand hand) implements Type {
        public InteractAt(@NotNull NetworkBuffer reader) {
            this(reader.read(FLOAT), reader.read(FLOAT), reader.read(FLOAT),
                    reader.readEnum(PlayerHand.class));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(FLOAT, targetX);
            writer.write(FLOAT, targetY);
            writer.write(FLOAT, targetZ);
            writer.writeEnum(PlayerHand.class, hand);
        }

        @Override
        public int id() {
            return 2;
        }
    }
}
