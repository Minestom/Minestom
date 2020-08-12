package net.minestom.server.network.packet.client.handler;

import net.minestom.server.network.packet.client.ClientPacket;

import java.util.function.Supplier;

public class ClientPacketsHandler {

    // Max packet id
    private static final int SIZE = 0x30;

    private final Supplier<? extends ClientPacket>[] supplierAccesses = new Supplier[SIZE];

    public void register(int id, Supplier<? extends ClientPacket> packetSupplier) {
        supplierAccesses[id] = packetSupplier;
    }

    public ClientPacket getPacketInstance(int id) {
        if (id > SIZE)
            throw new IllegalStateException("Packet ID 0x" + Integer.toHexString(id) + " has been tried to be parsed, debug needed");

        Supplier<? extends ClientPacket> supplier = supplierAccesses[id];
        if (supplierAccesses[id] == null)
            throw new IllegalStateException("Packet id 0x" + Integer.toHexString(id) + " isn't registered!");

        ClientPacket packet = supplier.get();
        //System.out.println("RECEIVED PACKET 0x" + Integer.toHexString(id)+" : "+packet.getClass().getSimpleName());
        return packet;
    }

}
