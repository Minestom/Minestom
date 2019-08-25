package fr.themode.minestom.listener;

import fr.themode.minestom.entity.Entity;
import fr.themode.minestom.entity.LivingEntity;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.event.AttackEvent;
import fr.themode.minestom.event.PlayerInteractEvent;
import fr.themode.minestom.net.packet.client.play.ClientUseEntityPacket;

public class UseEntityListener {

    public static void useEntityListener(ClientUseEntityPacket packet, Player player) {
        Entity entity = Entity.getEntity(packet.targetId);
        if (entity == null)
            return;
        ClientUseEntityPacket.Type type = packet.type;
        if (type == ClientUseEntityPacket.Type.ATTACK) {
            if (entity instanceof LivingEntity && ((LivingEntity) entity).isDead()) // Can't attack dead entities
                return;

            AttackEvent attackEvent = new AttackEvent(entity);
            player.callEvent(AttackEvent.class, attackEvent);
        } else if (type == ClientUseEntityPacket.Type.INTERACT) {
            PlayerInteractEvent playerInteractEvent = new PlayerInteractEvent(entity, packet.hand);
            player.callEvent(PlayerInteractEvent.class, playerInteractEvent);
        } else {
            PlayerInteractEvent playerInteractEvent = new PlayerInteractEvent(entity, packet.hand); // TODO find difference with INTERACT
            player.callEvent(PlayerInteractEvent.class, playerInteractEvent);
        }
    }

}
