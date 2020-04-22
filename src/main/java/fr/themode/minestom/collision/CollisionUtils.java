package fr.themode.minestom.collision;

import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.instance.block.Block;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.Position;

public class CollisionUtils {

    public static Position entity(Instance instance, BoundingBox boundingBox, Position currentPosition, Position targetPosition) {

        float currentX = currentPosition.getX();
        float currentY = currentPosition.getY();
        float currentZ = currentPosition.getZ();

        float targetX = targetPosition.getX();
        float targetY = targetPosition.getY();
        float targetZ = targetPosition.getZ();


        BlockPosition xBlock = new BlockPosition(targetX, currentY, currentZ);
        BlockPosition yBlock = new BlockPosition(currentX, targetY, currentZ);
        BlockPosition zBlock = new BlockPosition(currentX, currentY, targetZ);

        boolean xAir = !Block.getFromId(instance.getBlockId(xBlock)).isSolid();
        boolean yAir = !Block.getFromId(instance.getBlockId(yBlock)).isSolid();
        boolean zAir = !Block.getFromId(instance.getBlockId(zBlock)).isSolid();

        boolean xIntersect = boundingBox.intersect(xBlock);
        boolean yIntersect = boundingBox.intersect(yBlock);
        boolean zIntersect = boundingBox.intersect(zBlock);

        float newX = xAir ? targetX : xIntersect ? currentX : targetX;
        float newY = yAir ? targetY : yIntersect ? currentY : targetY;
        float newZ = zAir ? targetZ : zIntersect ? currentZ : targetZ;

        return new Position(newX, newY, newZ);
    }

}
