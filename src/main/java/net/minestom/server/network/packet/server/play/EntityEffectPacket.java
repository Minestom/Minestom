package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.potion.Potion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.*;

public record EntityEffectPacket(int entityId, @NotNull Potion potion,
                                 @Nullable CompoundBinaryTag factorCodec) implements ServerPacket.Play {
    public EntityEffectPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), new Potion(reader),
                reader.read(BOOLEAN) ? (CompoundBinaryTag) reader.read(NBT) : null);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, entityId);
        writer.write(potion);
        writer.writeOptional(NBT, factorCodec);
    }

    @Override
    public int playId() {
        return ServerPacketIdentifier.ENTITY_EFFECT;
    }
}
