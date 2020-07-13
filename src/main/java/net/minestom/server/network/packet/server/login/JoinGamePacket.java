package net.minestom.server.network.packet.server.login;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.LevelType;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTTypes;

public class JoinGamePacket implements ServerPacket {

	public int entityId;
	public GameMode gameMode = GameMode.SURVIVAL;
	public DimensionType dimensionType = DimensionType.OVERWORLD;
	public long hashedSeed;
	public byte maxPlayers = 0; // Unused
	//TODO remove
	public LevelType levelType;
	//TODO add api
	String identifier = "test:spawn";
	public int viewDistance;
	public boolean reducedDebugInfo = false;
	public boolean enableRespawnScreen = true;

	@Override
	public void write(PacketWriter writer) {
		int gameModeId = gameMode.getId();
		if (gameMode.isHardcore())
			gameModeId |= 8;

		writer.writeInt(entityId);
		writer.writeByte((byte) gameModeId);
		//Previous Gamemode
		writer.writeByte((byte) gameModeId);

		//array of worlds
		writer.writeVarInt(1);
		writer.writeSizedString("test:spawn_name");

		NBTList<NBTCompound> dimensionList = new NBTList<>(NBTTypes.TAG_Compound);
		for(DimensionType type : MinecraftServer.getDimensionTypeManager().unmodifiableList()) {
			dimensionList.add(type.toNBT());
		}
		writer.writeNBT("", new NBTCompound().set("dimension", dimensionList));

		writer.writeSizedString(dimensionType.getName().toString());
		writer.writeSizedString(identifier+"_"+ dimensionType.getName().getPath());
		writer.writeLong(hashedSeed);
		writer.writeByte(maxPlayers);
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
