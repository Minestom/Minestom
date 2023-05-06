package net.minestom.server.listener;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.network.packet.client.play.ClientInteractEntityPacket;

public class UseEntityListener {

    public static void useEntityListener(ClientInteractEntityPacket packet, Player player) {
        final Entity entity = Entity.getEntity(packet.targetId());
        if (entity == null || !entity.isViewer(player) || player.getDistanceSquared(entity) > 6 * 6)
            return;

        ClientInteractEntityPacket.Type type = packet.type();
        if (type instanceof ClientInteractEntityPacket.Attack) {
            if (entity instanceof LivingEntity && ((LivingEntity) entity).isDead()) // Can't attack dead entities
                return;
            EventDispatcher.call(new EntityAttackEvent(player, entity));
        } else if (type instanceof ClientInteractEntityPacket.InteractAt interactAt) {
            Point interactPosition = new Vec(interactAt.targetX(), interactAt.targetY(), interactAt.targetZ());
            EventDispatcher.call(new PlayerEntityInteractEvent(player, entity, interactAt.hand(), interactPosition));
        }
    }
}
