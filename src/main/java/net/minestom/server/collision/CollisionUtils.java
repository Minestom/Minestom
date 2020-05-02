package net.minestom.server.collision;

import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;

public class CollisionUtils {

    /**
     * Moves an entity with physics applied (ie checking against blocks)
     * @param entity the entity to move
     * @param positionOut the Position object in which the new position will be saved
     * @param velocityOut the Vector object in which the new velocity will be saved
     * @return whether this entity is on the ground
     */
    public static boolean handlePhysics(Entity entity, Vector deltaPosition, Position positionOut, Vector velocityOut) {
        Instance instance = entity.getInstance();
        Position currentPosition = entity.getPosition();
        BoundingBox boundingBox = entity.getBoundingBox();

        float currentX = currentPosition.getX();
        float currentY = currentPosition.getY();
        float currentZ = currentPosition.getZ();

        // target_WithBB is the target_ with the length in the _ direction of the bounding box added. Used to determinate block intersections

        // step Y
        float targetY = entity.getPosition().getY() + deltaPosition.getY();
        float targetYWithBB = targetY;
        if(deltaPosition.getY() > 0) {
            targetYWithBB += boundingBox.getHeight();
        }
        BlockPosition yBlock = new BlockPosition(currentX, (int) targetYWithBB, currentZ);
        boolean yAir = !Block.fromId(instance.getBlockId(yBlock)).isSolid();
        boolean yIntersect = boundingBox.intersect(yBlock);

        boolean yCollision = true;
        if(yAir || !yIntersect)
            yCollision = false;
        float newY = yCollision ? currentY : targetY;

        if(yCollision) {
            if(deltaPosition.getY() < 0) {
                newY = (float) (Math.ceil(newY)+0.01f); // TODO: custom block bounding boxes
            } else if(deltaPosition.getY() > 0) {
                newY = (float) (Math.floor(newY)-0.01f); // TODO: custom block bounding boxes
            }
        }

        // step X/Z
        float targetX = entity.getPosition().getX() + deltaPosition.getX();
        float targetXWithBB = targetX+Math.signum(deltaPosition.getX()) * boundingBox.getWidth()/2f;

        float targetZ = entity.getPosition().getZ() + deltaPosition.getZ();
        float targetZWithBB = targetZ+Math.signum(deltaPosition.getZ()) * boundingBox.getDepth()/2f;

        BlockPosition xBlock = new BlockPosition(targetXWithBB, (int) newY, currentZ);
        BlockPosition zBlock = new BlockPosition(currentX, (int) newY, targetZWithBB);

        boolean xAir = !Block.fromId(instance.getBlockId(xBlock)).isSolid();
        boolean zAir = !Block.fromId(instance.getBlockId(zBlock)).isSolid();

        boolean xIntersect = boundingBox.intersect(xBlock);
        boolean zIntersect = boundingBox.intersect(zBlock);

        boolean xCollision = true;
        if(xAir || !xIntersect)
            xCollision = false;
        boolean zCollision = true;
        if(zAir || !zIntersect)
            zCollision = false;
        float newX = xCollision ? currentX : targetX;
        float newZ = zCollision ? currentZ : targetZ;

        velocityOut.copy(entity.getVelocity());

        if(xCollision) {
            velocityOut.setX(0f);
        }
        if(yCollision) {
            velocityOut.setY(0f);
        }
        if(zCollision) {
            velocityOut.setZ(0f);
        }

        positionOut.setX(newX);
        positionOut.setY(newY);
        positionOut.setZ(newZ);


        return yCollision && deltaPosition.getY() < 0;
    }

}
