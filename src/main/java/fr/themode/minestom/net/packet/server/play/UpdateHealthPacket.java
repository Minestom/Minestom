package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;

public class UpdateHealthPacket implements ServerPacket {

    public float health;
    public int food;
    public float foodSaturation;

    @Override
    public void write(PacketWriter writer) {
        writer.writeFloat(health);
        writer.writeVarInt(food);
        writer.writeFloat(foodSaturation);
    }

    @Override
    public int getId() {
        return 0x48;
    }
}
