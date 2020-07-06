package net.minestom.server.network.packet.server.login;

import net.minestom.server.entity.GameMode;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.world.Dimension;
import net.minestom.server.world.LevelType;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTTypes;

public class JoinGamePacket implements ServerPacket {

	public int entityId;
	public GameMode gameMode = GameMode.SURVIVAL;
	public Dimension dimension = Dimension.OVERWORLD;
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
		writer.writeSizedString(identifier);
		// TODO: modifiable
		NBTCompound dimension = new NBTCompound()
				.setString("name", "test:normal")
				.setFloat("ambient_light", 1F)
				.setString("infiniburn", "")
				.setByte("natural", (byte) 0x01)
				.setByte("has_ceiling", (byte) 0x01)
				.setByte("has_skylight", (byte) 0x01)
				.setByte("shrunk", (byte) 0x00)
				.setByte("ultrawarm", (byte) 0x00)
				.setByte("has_raids", (byte) 0x00)
				.setByte("respawn_anchor_works", (byte) 0x00)
				.setByte("bed_works", (byte) 0x01)
				.setByte("piglin_safe", (byte) 0x01)
				.setInt("logical_height", 255)
		;
		NBTList<NBTCompound> dimensionList = new NBTList<>(NBTTypes.TAG_Compound);
		dimensionList.add(dimension);
		writer.writeNBT("", new NBTCompound().set("dimension", dimensionList));


		//writer.writeInt(dimension.getId());
		writer.writeSizedString("test:normal");
		writer.writeSizedString(identifier);
		writer.writeLong(hashedSeed);
		writer.writeByte(maxPlayers);
		writer.writeVarInt(viewDistance);
		writer.writeBoolean(reducedDebugInfo);
		writer.writeBoolean(enableRespawnScreen);
		//debug
		writer.writeBoolean(false);
		//is flat
		writer.writeBoolean(true);
	}

	@Override
	public int getId() {
		return ServerPacketIdentifier.JOIN_GAME;
	}

}
