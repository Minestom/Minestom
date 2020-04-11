package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientVehicleMovePacket extends ClientPlayPacket {

    public double x, y, z;
    public float yaw, pitch;

    @Override
    public void read(PacketReader reader, Runnable callback) {
        reader.readDouble(v -> x = v);
        reader.readDouble(v -> y = v);
        reader.readDouble(v -> z = v);

        reader.readFloat(value -> yaw = value);
        reader.readFloat(value -> {
            pitch = value;
            callback.run();
        });
    }
}
