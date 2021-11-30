package net.minestom.server.network.packet.client.play;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

public record ClientInteractEntityPacket(int targetId, @NotNull Type type, boolean sneaking) implements ClientPacket {
    public ClientInteractEntityPacket(BinaryReader reader) {
        this(reader.readVarInt(), switch (reader.readVarInt()) {
            case 0 -> new Interact(reader);
            case 1 -> new Attack();
            case 2 -> new InteractAt(reader);
            default -> throw new RuntimeException("Unknown action id");
        }, reader.readBoolean());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(targetId);
        writer.writeVarInt(type.id());
        writer.write(type);
        writer.writeBoolean(sneaking);
    }

    public sealed interface Type extends Writeable
            permits Interact, Attack, InteractAt {
        int id();
    }

    public record Interact(@NotNull Player.Hand hand) implements Type {
        public Interact(BinaryReader reader) {
            this(Player.Hand.values()[reader.readVarInt()]);
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeVarInt(hand.ordinal());
        }

        @Override
        public int id() {
            return 0;
        }
    }

    public record Attack() implements Type {
        @Override
        public void write(@NotNull BinaryWriter writer) {
            // Empty
        }

        @Override
        public int id() {
            return 1;
        }
    }

    public record InteractAt(float targetX, float targetY, float targetZ,
                             Player.Hand hand) implements Type {
        public InteractAt(BinaryReader reader) {
            this(reader.readFloat(), reader.readFloat(), reader.readFloat(),
                    Player.Hand.values()[reader.readVarInt()]);
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeFloat(targetX);
            writer.writeFloat(targetY);
            writer.writeFloat(targetZ);
            writer.writeVarInt(hand.ordinal());
        }

        @Override
        public int id() {
            return 2;
        }
    }
}
