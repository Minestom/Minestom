package fr.themode.minestom.net.packet.server.login;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.GameMode;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;
import fr.themode.minestom.world.Dimension;

public class JoinGamePacket implements ServerPacket {

    public int entityId;
    public GameMode gameMode = GameMode.SURVIVAL;
    public Dimension dimension = Dimension.OVERWORLD;
    public byte maxPlayers = 0; // Unused
    public String levelType = "default";
    public boolean reducedDebugInfo = false;

    @Override
    public void write(Buffer buffer) {
        int gameModeId = gameMode.getId();
        if (gameMode.isHardcore())
            gameModeId |= 8;

        buffer.putInt(entityId);
        buffer.putByte((byte) gameModeId);
        buffer.putInt(dimension.getId());
        buffer.putByte(maxPlayers);
        Utils.writeString(buffer, levelType);
        Utils.writeVarInt(buffer, 8);
        buffer.putBoolean(reducedDebugInfo);
    }

    @Override
    public int getId() {
        return 0x25;
    }
}
