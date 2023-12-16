package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.potion.Potion;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import static net.minestom.server.network.NetworkBuffer.*;

public record EntityEffectPacket(int entityId, @NotNull Potion potion,
                                 @Nullable NBTCompound factorCodec) implements ServerPacket {
    public EntityEffectPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), new Potion(reader),
                reader.read(BOOLEAN) ? (NBTCompound) reader.read(NBT) : null);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, entityId);
        writer.write(potion);
        writer.writeOptional(NBT, factorCodec);
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case PLAY -> ServerPacketIdentifier.ENTITY_EFFECT;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.PLAY);
        };
    }
}
