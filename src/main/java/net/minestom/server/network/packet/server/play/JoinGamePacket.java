package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.GameMode;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.List;

public record JoinGamePacket(int entityId, boolean isHardcore, GameMode gameMode, GameMode previousGameMode,
                             List<String> worlds, NBTCompound dimensionCodec, NBTCompound dimension, String world,
                             long hashedSeed, int maxPlayers, int viewDistance, int simulationDistance,
                             boolean reducedDebugInfo, boolean enableRespawnScreen, boolean isDebug,
                             boolean isFlat) implements ServerPacket {
    public JoinGamePacket(BinaryReader reader) {
        this(reader.readVarInt(), reader.readBoolean(), GameMode.fromId(reader.readByte()), GameMode.fromId(reader.readByte()),
                List.of(reader.readSizedStringArray()), (NBTCompound) reader.readTag(), (NBTCompound) reader.readTag(), reader.readSizedString(),
                reader.readLong(), reader.readVarInt(), reader.readVarInt(), reader.readVarInt(),
                reader.readBoolean(), reader.readBoolean(), reader.readBoolean(), reader.readBoolean());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeInt(entityId);
        writer.writeBoolean(isHardcore);
        writer.writeByte(gameMode.getId());
        if (previousGameMode != null) {
            writer.writeByte(previousGameMode.getId());
        } else {
            writer.writeByte((byte) -1);
        }

        //array of worlds
        writer.writeVarInt(1);
        writer.writeSizedString("minestom:world");

        writer.writeNBT("", dimensionCodec);
        writer.writeNBT("", dimension);

        writer.writeSizedString(world);
        writer.writeLong(hashedSeed);
        writer.writeVarInt(maxPlayers);
        writer.writeVarInt(viewDistance);
        writer.writeVarInt(simulationDistance);
        writer.writeBoolean(reducedDebugInfo);
        writer.writeBoolean(enableRespawnScreen);
        //debug
        writer.writeBoolean(isDebug);
        //is flat
        writer.writeBoolean(isFlat);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.JOIN_GAME;
    }

}
