package fr.themode.minestom.net.packet.client.handler;

import fr.themode.minestom.net.packet.client.ClientPacket;

import java.util.HashMap;
import java.util.Map;

public class ClientPacketsHandler {

    private Map<Integer, Class<? extends ClientPacket>> idPacketMap = new HashMap<>();

    public void register(int id, Class<? extends ClientPacket> packet) {
        this.idPacketMap.put(id, packet);
    }

    public Class<? extends ClientPacket> getPacketClass(int id) {
        return idPacketMap.get(id);
    }

}
