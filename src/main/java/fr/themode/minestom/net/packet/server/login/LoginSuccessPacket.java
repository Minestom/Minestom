package fr.themode.minestom.net.packet.server.login;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

import java.util.UUID;

public class LoginSuccessPacket implements ServerPacket {

    public UUID uuid;
    public String username;

    public LoginSuccessPacket(String username) {
        this.uuid = UUID.randomUUID();
        this.username = username;
    }

    public LoginSuccessPacket(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }

    @Override
    public void write(Buffer buffer) {
        Utils.writeString(buffer, uuid.toString()); // TODO mojang auth
        Utils.writeString(buffer, username);
    }

    @Override
    public int getId() {
        return 0x02;
    }
}
