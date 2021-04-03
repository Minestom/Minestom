package net.minestom.testing.miniclient;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.server.ServerPacket;

import java.util.List;

/**
 * MiniClient that can connect to a Minecraft server. Used for testing Minestom.
 *
 * This is *not* meant to be used as a custom client to play the game.
 */
public class MiniClient {

    public MiniClient() {

    }

    public void sendPacket(ClientPacket p) {}

    public int getEntityId() { return 0; }

    public <Packet extends ServerPacket> List<Packet> expect(Class<Packet> toExpect) {
        return null;
    }

    public <Packet extends ServerPacket> Packet expectSingle(Class<Packet> toExpect) {
        return null;
    }

}
