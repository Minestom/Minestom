package fr.themode.minestom.listener;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.event.PlayerUseItemEvent;
import fr.themode.minestom.inventory.PlayerInventory;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.client.play.ClientUseItemPacket;

public class UseItemListener {

    public static void useItemListener(ClientUseItemPacket packet, Player player) {
        PlayerInventory inventory = player.getInventory();
        Player.Hand hand = packet.hand;
        ItemStack itemStack = hand == Player.Hand.MAIN ? inventory.getItemInMainHand() : inventory.getItemInOffHand();
        PlayerUseItemEvent playerUseItemEvent = new PlayerUseItemEvent(hand, itemStack);
        player.callEvent(PlayerUseItemEvent.class, playerUseItemEvent);

        // TODO check if item in main or off hand is food or item with animation (bow/crossbow/riptide)
        // TODO in material enum?
        //player.refreshActiveHand(true, false, false);
        //player.sendPacketToViewers(player.getMetadataPacket());
    }

}
