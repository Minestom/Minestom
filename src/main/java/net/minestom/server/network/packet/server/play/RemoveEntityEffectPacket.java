package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record RemoveEntityEffectPacket(int entityId, @NotNull PotionEffect potionEffect) implements ServerPacket {
    public RemoveEntityEffectPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), Objects.requireNonNull(PotionEffect.fromId(reader.read(VAR_INT))));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, entityId);
        writer.write(VAR_INT, potionEffect.id());
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case PLAY -> ServerPacketIdentifier.REMOVE_ENTITY_EFFECT;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.PLAY);
        };
    }
}
