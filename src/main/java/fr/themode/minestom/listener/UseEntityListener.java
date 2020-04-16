package fr.themode.minestom.listener;

import fr.themode.minestom.entity.Entity;
import fr.themode.minestom.entity.LivingEntity;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.event.AttackEvent;
import fr.themode.minestom.event.PlayerInteractEvent;
import fr.themode.minestom.net.packet.client.play.ClientInteractEntityPacket;

public class UseEntityListener {

    public static void useEntityListener(ClientInteractEntityPacket packet, Player player) {
        Entity entity = Entity.getEntity(packet.targetId);
        if (entity == null)
            return;
        ClientInteractEntityPacket.Type type = packet.type;
        if (type == ClientInteractEntityPacket.Type.ATTACK) {
            if (entity instanceof LivingEntity && ((LivingEntity) entity).isDead()) // Can't attack dead entities
                return;

            AttackEvent attackEvent = new AttackEvent(entity);
            player.callEvent(AttackEvent.class, attackEvent);
        } else if (type == ClientInteractEntityPacket.Type.INTERACT) {
            PlayerInteractEvent playerInteractEvent = new PlayerInteractEvent(entity, packet.hand);
            player.callEvent(PlayerInteractEvent.class, playerInteractEvent);
        } else {
            PlayerInteractEvent playerInteractEvent = new PlayerInteractEvent(entity, packet.hand); // TODO find difference with INTERACT
            player.callEvent(PlayerInteractEvent.class, playerInteractEvent);
        }
    }

}
