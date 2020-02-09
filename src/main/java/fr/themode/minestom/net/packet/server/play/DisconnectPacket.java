package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.chat.Chat;
import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;

public class DisconnectPacket implements ServerPacket {

    public String message;

    @Override
    public void write(PacketWriter writer) {
        writer.writeSizedString(Chat.legacyTextString(message));
    }

    @Override
    public int getId() {
        return 0x1B;
    }
}
