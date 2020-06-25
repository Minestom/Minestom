package net.minestom.server.network.packet.server.login;

import net.minestom.server.entity.GameMode;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.nbt.NbtWriter;
import net.minestom.server.world.Dimension;
import net.minestom.server.world.LevelType;

import static net.minestom.server.utils.nbt.NBT.*;

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
		NbtWriter nbtWriter = new NbtWriter(writer);
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
		nbtWriter.writeCompound("", (writer1) -> {
			writer1.writeList("dimension", NBT_COMPOUND, 1, () -> {
				writer1.writeString("name", "test:normal");
				writer1.writeFloat("ambient_light", 1F);
				writer1.writeString("infiniburn", "");
				writer1.writeByte("natural", (byte) 0x01);
				writer1.writeByte("has_ceiling", (byte) 0x01);
				writer1.writeByte("has_skylight", (byte) 0x01);
				writer1.writeByte("shrunk", (byte) 0x00);
				writer1.writeByte("ultrawarm", (byte) 0x00);
				writer1.writeByte("has_raids", (byte) 0x00);
				writer1.writeByte("respawn_anchor_works", (byte) 0x00);
				writer1.writeByte("bed_works", (byte) 0x01);
				writer1.writeByte("piglin_safe", (byte) 0x01);
				writer1.writeInt("logical_height", 255);
			});
		});

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
