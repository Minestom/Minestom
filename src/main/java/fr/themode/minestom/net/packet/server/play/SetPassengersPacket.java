package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class SetPassengersPacket implements ServerPacket {

    public int vehicleEntityId;
    public int[] passengersId;

    @Override
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, vehicleEntityId);
        Utils.writeVarInt(buffer, passengersId.length);
        for (int passengerId : passengersId) {
            Utils.writeVarInt(buffer, passengerId);
        }
    }

    @Override
    public int getId() {
        return 0x4A;
    }
}
