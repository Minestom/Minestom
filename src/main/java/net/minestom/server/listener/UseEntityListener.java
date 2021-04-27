package net.minestom.server.listener;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.item.ItemEntityInteractEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.event.ItemEvents;
import net.minestom.server.network.packet.client.play.ClientInteractEntityPacket;

public class UseEntityListener {

    public static void useEntityListener(ClientInteractEntityPacket packet, Player player) {
        final Entity entity = Entity.getEntity(packet.targetId);
        if (entity == null)
            return;
        ClientInteractEntityPacket.Type type = packet.type;

        // Player cannot interact with entities he cannot see
        if (!entity.isViewer(player))
            return;

        if (type == ClientInteractEntityPacket.Type.ATTACK) {
            if (entity instanceof LivingEntity && ((LivingEntity) entity).isDead()) // Can't attack dead entities
                return;

            EntityAttackEvent entityAttackEvent = new EntityAttackEvent(player, entity);
            player.callEvent(EntityAttackEvent.class, entityAttackEvent);
        } else if (type == ClientInteractEntityPacket.Type.INTERACT) {
            PlayerEntityInteractEvent playerEntityInteractEvent = new PlayerEntityInteractEvent(player, entity, packet.hand);
            player.callEvent(PlayerEntityInteractEvent.class, playerEntityInteractEvent);

            // Trigger an item event if the player has an item.
            if (!player.getItemInHand(packet.hand).equals(ItemStack.AIR)) {
                ItemEntityInteractEvent itemEntityInteractEvent = new ItemEntityInteractEvent(
                        player.getItemInHand(packet.hand),
                        player, entity, packet.hand
                );

                ItemEvents.callEventOnItem(player.getItemInHand(packet.hand), ItemEntityInteractEvent.class, itemEntityInteractEvent);
            }

        } else {
            // TODO find difference with INTERACT
            //PlayerEntityInteractEvent playerEntityInteractEvent = new PlayerEntityInteractEvent(player, entity, packet.hand);
            //player.callEvent(PlayerEntityInteractEvent.class, playerEntityInteractEvent);
        }
    }

}
