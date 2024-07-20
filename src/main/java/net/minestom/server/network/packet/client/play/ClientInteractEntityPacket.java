package net.minestom.server.network.packet.client.play;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientInteractEntityPacket(int targetId, @NotNull Type type, boolean sneaking) implements ClientPacket {
    public ClientInteractEntityPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), switch (reader.read(VAR_INT)) {
            case 0 -> new Interact(reader);
            case 1 -> new Attack();
            case 2 -> new InteractAt(reader);
            default -> throw new RuntimeException("Unknown action id");
        }, reader.read(BOOLEAN));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, targetId);
        writer.write(VAR_INT, type.id());
        writer.write(type);
        writer.write(BOOLEAN, sneaking);
    }

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
