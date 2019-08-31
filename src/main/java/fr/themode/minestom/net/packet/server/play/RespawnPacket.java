package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.entity.GameMode;
import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.world.Dimension;
import fr.themode.minestom.world.LevelType;

public class RespawnPacket implements ServerPacket {

    public Dimension dimension;
    public GameMode gameMode;
    public LevelType levelType;

    @Override
    public void write(PacketWriter writer) {
        int gameModeId = gameMode.getId();
        if (gameMode.isHardcore())
            gameModeId |= 8;

        writer.writeByte((byte) gameModeId);
        writer.writeInt(dimension.getId());
        writer.writeSizedString(levelType.getType());
    }

    @Override
    public int getId() {
        return 0x3A;
    }
}
