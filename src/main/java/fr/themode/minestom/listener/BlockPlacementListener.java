package fr.themode.minestom.listener;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.event.BlockPlaceEvent;
import fr.themode.minestom.instance.Chunk;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.net.packet.client.play.ClientPlayerBlockPlacementPacket;
import fr.themode.minestom.net.packet.client.play.ClientPlayerDiggingPacket;
import fr.themode.minestom.utils.Position;

public class BlockPlacementListener {

    public static void listener(ClientPlayerBlockPlacementPacket packet, Player player) {
        ClientPlayerDiggingPacket.BlockFace blockFace = packet.blockFace;
        Position position = packet.position;

        Instance instance = player.getInstance();
        if (instance == null)
            return;

        int offsetX = blockFace == ClientPlayerDiggingPacket.BlockFace.WEST ? -1 : blockFace == ClientPlayerDiggingPacket.BlockFace.EAST ? 1 : 0;
        int offsetY = blockFace == ClientPlayerDiggingPacket.BlockFace.BOTTOM ? -1 : blockFace == ClientPlayerDiggingPacket.BlockFace.TOP ? 1 : 0;
        int offsetZ = blockFace == ClientPlayerDiggingPacket.BlockFace.NORTH ? -1 : blockFace == ClientPlayerDiggingPacket.BlockFace.SOUTH ? 1 : 0;

        BlockPlaceEvent blockPlaceEvent = new BlockPlaceEvent((short) 10);
        player.callEvent(BlockPlaceEvent.class, blockPlaceEvent);
        if (!blockPlaceEvent.isCancelled()) {
            instance.setBlock(position.getX() + offsetX, position.getY() + offsetY, position.getZ() + offsetZ, "custom_block");
            // TODO consume block in hand for survival players
        } else {
            Chunk chunk = instance.getChunkAt(position);
            instance.sendChunkUpdate(player, chunk);
        }
        player.getInventory().refreshSlot(player.getHeldSlot());
        /*Random random = new Random();
        BlockBatch blockBatch = instance.createBlockBatch();
        for (int x = -64; x < 64; x++)
            for (int z = -64; z < 64; z++) {
                if (random.nextInt(100) > 75)
                    blockBatch.setBlock(x, position.getY() + 1, z, new Block(1));
            }
        blockBatch.flush();*/
    }

}
