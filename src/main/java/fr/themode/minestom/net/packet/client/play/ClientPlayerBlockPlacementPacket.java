package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.instance.Block;
import fr.themode.minestom.instance.BlockBatch;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.utils.Position;
import fr.themode.minestom.utils.Utils;

import java.util.Random;

public class ClientPlayerBlockPlacementPacket implements ClientPlayPacket {

    public Hand hand;
    public Position position;
    public ClientPlayerDiggingPacket.BlockFace blockFace;
    public float cursorPositionX, cursorPositionY, cursorPositionZ;
    public boolean insideBlock;

    @Override
    public void process(Player player) {
        Instance instance = player.getInstance();
        if (instance == null)
            return;

        Random random = new Random();
        BlockBatch blockBatch = instance.createBlockBatch();
        for (int x = -64; x < 64; x++)
            for (int z = -64; z < 64; z++) {
                if (random.nextInt(100) > 75)
                    blockBatch.setBlock(x, position.getY() + 1, z, new Block(1));
            }
        blockBatch.flush();
    }

    @Override
    public void read(Buffer buffer) {
        this.hand = Hand.values()[Utils.readVarInt(buffer)];
        this.position = Utils.readPosition(buffer);
        this.blockFace = ClientPlayerDiggingPacket.BlockFace.values()[Utils.readVarInt(buffer)];
        this.cursorPositionX = buffer.getFloat();
        this.cursorPositionY = buffer.getFloat();
        this.cursorPositionZ = buffer.getFloat();
        this.insideBlock = buffer.getBoolean();
    }

    public enum Hand {
        MAIN_HAND,
        OFF_HAND;
    }

}
