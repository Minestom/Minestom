package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.potion.Potion;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public record EntityEffectPacket(int entityId, @NotNull Potion potion,
                                 @Nullable NBTCompound factorCodec) implements ServerPacket {
    public EntityEffectPacket(BinaryReader reader) {
        this(reader.readVarInt(), new Potion(reader),
                reader.readBoolean() ? (NBTCompound) reader.readTag() : null);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.write(potion);
        writer.writeBoolean(factorCodec != null);
        if (factorCodec != null) writer.writeNBT("", factorCodec);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_EFFECT;
    }
}
