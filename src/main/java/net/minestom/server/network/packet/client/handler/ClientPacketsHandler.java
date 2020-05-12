package net.minestom.server.network.packet.client.handler;

import com.esotericsoftware.reflectasm.ConstructorAccess;
import net.minestom.server.network.packet.client.ClientPacket;

public class ClientPacketsHandler {

    // Max packet id
    private static final int SIZE = 0x2E;

    private ConstructorAccess[] constructorAccesses = new ConstructorAccess[SIZE];

    public void register(int id, Class<? extends ClientPacket> packet) {
        constructorAccesses[id] = ConstructorAccess.get(packet);
    }

    public ClientPacket getPacketInstance(int id) {
        // System.out.println("RECEIVED PACKET 0x" + Integer.toHexString(id));
        if (id > SIZE)
            throw new IllegalStateException("Packet ID 0x" + Integer.toHexString(id) + " has been tried to be parsed, debug needed");

        ConstructorAccess<? extends ClientPacket> constructorAccess = constructorAccesses[id];
        if (constructorAccess == null)
            throw new IllegalStateException("Packet id 0x" + Integer.toHexString(id) + " isn't registered!");

        ClientPacket packet = constructorAccess.newInstance();
        return packet;
    }

}
