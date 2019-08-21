package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class UpdateHealthPacket implements ServerPacket {

    public float health;
    public int food;
    public float foodSaturation;

    @Override
    public void write(Buffer buffer) {
        buffer.putFloat(health);
        Utils.writeVarInt(buffer, food);
        buffer.putFloat(foodSaturation);
    }

    @Override
    public int getId() {
        return 0x48;
    }
}
