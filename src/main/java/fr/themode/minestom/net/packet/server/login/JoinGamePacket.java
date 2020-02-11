package fr.themode.minestom.net.packet.server.login;

import fr.themode.minestom.entity.GameMode;
import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;
import fr.themode.minestom.world.Dimension;
import fr.themode.minestom.world.LevelType;

public class JoinGamePacket implements ServerPacket {

    public int entityId;
    public GameMode gameMode = GameMode.SURVIVAL;
    public Dimension dimension = Dimension.OVERWORLD;
    public long hashedSeed;
    public byte maxPlayers = 0; // Unused
    public LevelType levelType;
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
        writer.writeInt(dimension.getId());
        writer.writeLong(hashedSeed);
        writer.writeByte(maxPlayers);
        writer.writeSizedString(levelType.getType());
        writer.writeVarInt(viewDistance);
        writer.writeBoolean(reducedDebugInfo);
        writer.writeBoolean(enableRespawnScreen);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.JOIN_GAME;
    }
}
