package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;

public class UpdateScorePacket implements ServerPacket {

    public String entityName;
    public byte action;
    public String objectiveName;
    public int value;

    @Override
    public void write(PacketWriter writer) {
        writer.writeSizedString(entityName);
        writer.writeByte(action);
        writer.writeSizedString(objectiveName);
        if (action != 1) {
            writer.writeVarInt(value);
        }
    }

    @Override
    public int getId() {
        return 0x4D;
    }
}
