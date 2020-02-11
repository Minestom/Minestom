package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

public class SetCooldownPacket implements ServerPacket {

    public int itemId;
    public int cooldownTicks;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(itemId);
        writer.writeVarInt(cooldownTicks);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SET_COOLDOWN;
    }
}
