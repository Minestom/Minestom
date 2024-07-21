package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.potion.Potion;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record EntityEffectPacket(int entityId, @NotNull Potion potion) implements ServerPacket.Play {
    public EntityEffectPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), new Potion(reader));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, entityId);
        writer.write(potion);
    }

}
