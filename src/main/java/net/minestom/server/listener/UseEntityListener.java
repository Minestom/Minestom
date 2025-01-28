package net.minestom.server.listener;

import net.minestom.server.ServerFlag;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
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
            final double maxDistanceSquared = Math.pow(player.getAttributeValue(Attribute.ENTITY_INTERACTION_RANGE) + 1, 2);

            final double distSquared = getDistSquared(player, entity);

            if (distSquared > maxDistanceSquared) {
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

    private static double getDistSquared(Player player, Entity entity) {
        final Pos playerPos = player.getPosition();
        final double eyeHeight = player.getEyeHeight();
        final double px = playerPos.x();
        final double py = playerPos.y() + eyeHeight;
        final double pz = playerPos.z();

        final BoundingBox box = entity.getBoundingBox();
        final double halfWidth = box.width() / 2;
        final double height = box.height();
        final Pos entityPos = entity.getPosition();

        final double minX = entityPos.x() - halfWidth;
        final double maxX = entityPos.x() + halfWidth;
        final double minY = entityPos.y();
        final double maxY = entityPos.y() + height;
        final double minZ = entityPos.z() - halfWidth;
        final double maxZ = entityPos.z() + halfWidth;

        final double clampX = Math.max(minX, Math.min(px, maxX));
        final double clampY = Math.max(minY, Math.min(py, maxY));
        final double clampZ = Math.max(minZ, Math.min(pz, maxZ));

        final double dx = px - clampX;
        final double dy = py - clampY;
        final double dz = pz - clampZ;
        return dx * dx + dy * dy + dz * dz;
    }
}