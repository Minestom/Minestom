package fr.themode.minestom.listener;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.event.PlayerBlockPlaceEvent;
import fr.themode.minestom.instance.Chunk;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.inventory.PlayerInventory;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.client.play.ClientPlayerBlockPlacementPacket;
import fr.themode.minestom.net.packet.client.play.ClientPlayerDiggingPacket;
import fr.themode.minestom.utils.BlockPosition;

public class BlockPlacementListener {

    public static void listener(ClientPlayerBlockPlacementPacket packet, Player player) {
        PlayerInventory playerInventory = player.getInventory();
        Player.Hand hand = packet.hand;
        ClientPlayerDiggingPacket.BlockFace blockFace = packet.blockFace;
        BlockPosition blockPosition = packet.blockPosition;

        Instance instance = player.getInstance();
        if (instance == null)
            return;

        ItemStack usedItem = hand == Player.Hand.MAIN ? playerInventory.getItemInMainHand() : playerInventory.getItemInOffHand();
        if (!usedItem.getMaterial().isBlock())
            return;

        int offsetX = blockFace == ClientPlayerDiggingPacket.BlockFace.WEST ? -1 : blockFace == ClientPlayerDiggingPacket.BlockFace.EAST ? 1 : 0;
        int offsetY = blockFace == ClientPlayerDiggingPacket.BlockFace.BOTTOM ? -1 : blockFace == ClientPlayerDiggingPacket.BlockFace.TOP ? 1 : 0;
        int offsetZ = blockFace == ClientPlayerDiggingPacket.BlockFace.NORTH ? -1 : blockFace == ClientPlayerDiggingPacket.BlockFace.SOUTH ? 1 : 0;

        blockPosition.add(offsetX, offsetY, offsetZ);
        boolean intersectPlayer = player.getBoundingBox().intersect(blockPosition);
        if (!intersectPlayer) {
            PlayerBlockPlaceEvent playerBlockPlaceEvent = new PlayerBlockPlaceEvent((short) 10, blockPosition, packet.hand);
            player.callEvent(PlayerBlockPlaceEvent.class, playerBlockPlaceEvent);
            if (!playerBlockPlaceEvent.isCancelled()) {
                instance.setBlock(blockPosition, "custom_block"); // TODO set useItem's block instead
                if (playerBlockPlaceEvent.doesConsumeBlock()) {
                    usedItem.setAmount((byte) (usedItem.getAmount() - 1));
                    if (usedItem.getAmount() <= 0)
                        usedItem = ItemStack.AIR_ITEM;
                    if (hand == Player.Hand.OFF) {
                        playerInventory.setItemInOffHand(usedItem);
                    } else { // Main
                        playerInventory.setItemInMainHand(usedItem);
                    }
                }
            } else {
                Chunk chunk = instance.getChunkAt(blockPosition);
                instance.sendChunkUpdate(player, chunk);
            }
        }
        player.getInventory().refreshSlot(player.getHeldSlot());
    }

}
