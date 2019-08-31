package fr.themode.minestom.net.packet.server.login;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;

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
    public void write(PacketWriter writer) {
        writer.writeSizedString(uuid.toString()); // TODO mojang auth
        writer.writeSizedString(username);
    }

    @Override
    public int getId() {
        return 0x02;
    }
}
