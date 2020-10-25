package net.minestom.server.network.packet.server.login;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class LoginDisconnect implements ServerPacket {

    private String kickMessage;

    public LoginDisconnect(String kickMessage) {
        this.kickMessage = kickMessage;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(kickMessage);
    }

    @Override
    public int getId() {
        return 0x00;
    }

}
