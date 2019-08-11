package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientPlayerPositionAndLookPacket implements ClientPlayPacket {

    public double x, y, z;
    public float yaw, pitch;
    public boolean onGround;

    @Override
    public void process(Player player) {
        boolean chunkTest = player.chunkTest(x, z);
        if (chunkTest)
            return;

        player.refreshPosition(x, y, z);
        player.refreshView(yaw, pitch);
        player.refreshOnGround(onGround);
    }

    @Override
    public void read(Buffer buffer) {
        this.x = buffer.getDouble();
        this.y = buffer.getDouble();
        this.z = buffer.getDouble();
        this.yaw = buffer.getFloat();
        this.pitch = buffer.getFloat();
        this.onGround = buffer.getBoolean();
    }
}
