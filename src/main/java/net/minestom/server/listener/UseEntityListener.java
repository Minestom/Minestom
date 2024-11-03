package net.minestom.server.listener;

import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.network.packet.client.play.ClientInteractEntityPacket;

public class UseEntityListener {

    public static void useEntityListener(ClientInteractEntityPacket packet, Player player) {
        final Entity entity = player.getInstance().getEntityById(packet.targetId());
        if (entity == null || !entity.isViewer(player))
            return;

        if (ServerFlag.ENFORCE_INTERACTION_LIMIT) {
            double range = Math.pow(player.getAttributeValue(Attribute.ENTITY_INTERACTION_RANGE) + 1, 2); // Add 1 additional block for people with less than stellar ping
            if (player.getDistanceSquared(entity) > range) {
                return;
            }
        }

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
