package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.GameMode;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;
import fr.themode.minestom.world.Dimension;
import fr.themode.minestom.world.LevelType;

public class RespawnPacket implements ServerPacket {

    public Dimension dimension;
    public GameMode gameMode;
    public LevelType levelType;

    @Override
    public void write(Buffer buffer) {
        int gameModeId = gameMode.getId();
        if (gameMode.isHardcore())
            gameModeId |= 8;

        buffer.putByte((byte) gameModeId);
        buffer.putInt(dimension.getId());
        Utils.writeString(buffer, levelType.getType());
    }

    @Override
    public int getId() {
        return 0x3A;
    }
}
