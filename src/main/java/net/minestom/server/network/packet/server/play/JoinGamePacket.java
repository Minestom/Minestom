package net.minestom.server.network.packet.server.play;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;

import java.io.IOException;

public class JoinGamePacket implements ServerPacket {

    public int entityId;
    public boolean hardcore;
    public GameMode gameMode;
    public GameMode previousGameMode;
    public DimensionType dimensionType;
    public long hashedSeed;
    public int maxPlayers = 0; // Unused
    public int viewDistance;
    public boolean reducedDebugInfo = false;
    public boolean enableRespawnScreen = true;
    public boolean isDebug = false;
    public boolean isFlat = false;

    public JoinGamePacket() {
        gameMode = GameMode.SURVIVAL;
        dimensionType = DimensionType.OVERWORLD;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeInt(entityId);
        writer.writeBoolean(hardcore);
        writer.writeByte(gameMode.getId());

        if (previousGameMode == null) {
            writer.writeByte(gameMode.getId());
        } else {
            writer.writeByte(previousGameMode.getId());
        }

        //array of worlds
        writer.writeVarInt(1);
        writer.writeSizedString("minestom:world");
        NBTCompound nbt = new NBTCompound();
        NBTCompound dimensions = MinecraftServer.getDimensionTypeManager().toNBT();
        NBTCompound biomes = MinecraftServer.getBiomeManager().toNBT();

        nbt.set("minecraft:dimension_type", dimensions);
        nbt.set("minecraft:worldgen/biome", biomes);

        writer.writeNBT("", nbt);
        writer.writeNBT("", dimensionType.toNBT());

        writer.writeSizedString(dimensionType.getName().toString());
        writer.writeLong(hashedSeed);
        writer.writeVarInt(maxPlayers);
        writer.writeVarInt(viewDistance);
        writer.writeBoolean(reducedDebugInfo);
        writer.writeBoolean(enableRespawnScreen);
        //debug
        writer.writeBoolean(isDebug);
        //is flat
        writer.writeBoolean(isFlat);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        entityId = reader.readInt();
        hardcore = reader.readBoolean();
        gameMode = GameMode.fromId(reader.readByte());
        previousGameMode = GameMode.fromId(reader.readByte());
        int worldCount = reader.readVarInt();
        Check.stateCondition(worldCount != 1, "Only 1 world is supported per JoinGamePacket by Minestom for the moment.");
        //for (int i = 0; i < worldCount; i++) {
        String worldName = reader.readSizedString();
        try {
            NBTCompound dimensionCodec = (NBTCompound) reader.readTag();
            dimensionType = DimensionType.fromNBT((NBTCompound) reader.readTag());

            String dimensionName = reader.readSizedString();
            hashedSeed = reader.readLong();
            maxPlayers = reader.readVarInt();
            viewDistance = reader.readVarInt();
            reducedDebugInfo = reader.readBoolean();
            enableRespawnScreen = reader.readBoolean();
            isDebug = reader.readBoolean();
            isFlat = reader.readBoolean();
        } catch (IOException | NBTException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            // TODO: should we throw as the packet is invalid?
        }
        //}
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.JOIN_GAME;
    }

}
