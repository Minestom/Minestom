package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;

public class SweepResult {
    public double res;
    public double normalx, normaly, normalz;
    public Point collisionBlock;
    public Block blockType;

    public SweepResult(double res, double normalx, double normaly, double normalz) {
        this.res = res;
        this.normalx = normalx;
        this.normaly = normaly;
        this.normalz = normalz;
    }
}
