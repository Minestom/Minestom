package net.minestom.server.particle.shapes;

import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

public class BezierCurves {
    public static Position bezier(@NotNull Position start, @NotNull Position end,
                                  @NotNull Position[] controlPoints,
                                  double time) {
        Position[] points = new Position[2 + controlPoints.length];
        points[0] = start;
        System.arraycopy(controlPoints, 0, points, 1, controlPoints.length);
        points[points.length - 1] = end;

        return bezierN(points, time);
    }

    public static Position bezierN(@NotNull Position[] points, double time) {
        double x = 0;
        double y = 0;
        double z = 0;

        int order = points.length - 1;
        for (int i = 0; i <= order; i++) {
            double preCompute = binomial(order, i) * Math.pow((1 - time), (order - i)) * Math.pow(time, i);

            x = x + (preCompute * points[i].getX());
            y = y + (preCompute * points[i].getY());
            z = z + (preCompute * points[i].getZ());
        }

        return new Position(x, y, z);
    }

    public static int binomial(int n, int k) {
        if (k > n - k) {
            k = n - k;
        }

        int binomial = 1;
        for (int i = 1; i <= k; i++) {
            binomial = binomial * (n + 1 - i) / i;
        }
        return binomial;
    }
}
