package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;

final class SweepResult {
    double res;
    double normalX, normalY, normalZ;
    Point collisionBlock;
    Block blockType;

    SweepResult(double res, double normalX, double normalY, double normalZ) {
        this.res = res;
        this.normalX = normalX;
        this.normalY = normalY;
        this.normalZ = normalZ;
    }
}
