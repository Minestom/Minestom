package fr.themode.minestom.listener;

import fr.themode.minestom.entity.Entity;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.event.AttackEvent;
import fr.themode.minestom.event.InteractEvent;
import fr.themode.minestom.net.packet.client.play.ClientUseEntityPacket;

public class UseEntityListener {

    public static void useEntityListener(ClientUseEntityPacket packet, Player player) {
        Entity entity = Entity.getEntity(packet.targetId);
        if (entity == null)
            return;
        ClientUseEntityPacket.Type type = packet.type;
        if (type == ClientUseEntityPacket.Type.ATTACK) {
            AttackEvent attackEvent = new AttackEvent(entity);
            player.callEvent(AttackEvent.class, attackEvent);
        } else if (type == ClientUseEntityPacket.Type.INTERACT) {
            InteractEvent interactEvent = new InteractEvent(entity);
            player.callEvent(InteractEvent.class, interactEvent);
        } else {
            InteractEvent interactEvent = new InteractEvent(entity); // TODO find difference with INTERACT
            player.callEvent(InteractEvent.class, interactEvent);
        }
    }

}
