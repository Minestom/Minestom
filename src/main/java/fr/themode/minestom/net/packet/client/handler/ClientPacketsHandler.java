package fr.themode.minestom.net.packet.client.handler;

import com.esotericsoftware.reflectasm.ConstructorAccess;
import fr.themode.minestom.net.packet.client.ClientPacket;

import java.util.HashMap;
import java.util.Map;

public class ClientPacketsHandler {

    private Map<Integer, ConstructorAccess<? extends ClientPacket>> constructorAccessMap = new HashMap<>();

    public void register(int id, Class<? extends ClientPacket> packet) {
        this.constructorAccessMap.put(id, ConstructorAccess.get(packet));
    }

    public ClientPacket getPacketInstance(int id) {
        return constructorAccessMap.get(id).newInstance();
    }

}
