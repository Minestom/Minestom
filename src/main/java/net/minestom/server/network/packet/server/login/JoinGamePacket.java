package net.minestom.server.network.packet.server.login;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.LevelType;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public class JoinGamePacket implements ServerPacket {

    public int entityId;
    public GameMode gameMode = GameMode.SURVIVAL;
    public DimensionType dimensionType = DimensionType.OVERWORLD;
    public long hashedSeed;
    public byte maxPlayers = 0; // Unused
    public LevelType levelType;
    public int viewDistance;
    public boolean reducedDebugInfo = false;
    public boolean enableRespawnScreen = true;

    @Override
    public void write(BinaryWriter writer) {
        writer.writeInt(entityId);
        writer.writeBoolean(MinecraftServer.isHardcoreLook());
        writer.writeByte(gameMode.getId());
        //Previous Gamemode
        writer.writeByte(gameMode.getId());

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
        writer.writeBoolean(false);
        //is flat
        writer.writeBoolean(levelType == LevelType.FLAT);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.JOIN_GAME;
    }

}
