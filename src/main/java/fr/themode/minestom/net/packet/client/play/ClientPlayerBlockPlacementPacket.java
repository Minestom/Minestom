package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.utils.Position;
import fr.themode.minestom.utils.Utils;

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

        int offsetX = blockFace == ClientPlayerDiggingPacket.BlockFace.WEST ? -1 : blockFace == ClientPlayerDiggingPacket.BlockFace.EAST ? 1 : 0;
        int offsetY = blockFace == ClientPlayerDiggingPacket.BlockFace.BOTTOM ? -1 : blockFace == ClientPlayerDiggingPacket.BlockFace.TOP ? 1 : 0;
        int offsetZ = blockFace == ClientPlayerDiggingPacket.BlockFace.NORTH ? -1 : blockFace == ClientPlayerDiggingPacket.BlockFace.SOUTH ? 1 : 0;

        instance.setBlock(position.getX() + offsetX, position.getY() + offsetY, position.getZ() + offsetZ, "custom_block");
        player.getInventory().refreshSlot(player.getHeldSlot());
        // TODO consume block in hand for survival players
        /*Random random = new Random();
        BlockBatch blockBatch = instance.createBlockBatch();
        for (int x = -64; x < 64; x++)
            for (int z = -64; z < 64; z++) {
                if (random.nextInt(100) > 75)
                    blockBatch.setBlock(x, position.getY() + 1, z, new Block(1));
            }
        blockBatch.flush();*/
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
