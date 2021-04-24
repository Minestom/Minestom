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

    public static Position bezier2(@NotNull Position start, @NotNull Position end,
                                   @NotNull Position controlPoint, double time) {
        Position position = new Position();
        position.setX(Math.pow(1 - time, 2) * start.getX() +
                (1 - time) * 2 * time * controlPoint.getX() +
                time * time * end.getX());
        position.setY(Math.pow(1 - time, 2) * start.getY() +
                (1 - time) * 2 * time * controlPoint.getY() +
                time * time * end.getY());
        position.setZ(Math.pow(1 - time, 2) * start.getZ() +
                (1 - time) * 2 * time * controlPoint.getZ() +
                time * time * end.getZ());

        return position;
    }

    public static Position bezier3(@NotNull Position start, @NotNull Position end,
                                   @NotNull Position controlPoint1, @NotNull Position controlPoint2,
                                   double time) {
        Position position = new Position();
        position.setX(Math.pow(1 - time, 3) * start.getX() +
                Math.pow(1 - time, 2) * 3 * time * controlPoint1.getX() +
                (1 - time) * 3 * time * time * controlPoint2.getX() +
                time * time * time * end.getX());
        position.setY(Math.pow(1 - time, 3) * start.getY() +
                Math.pow(1 - time, 2) * 3 * time * controlPoint1.getY() +
                (1 - time) * 3 * time * time * controlPoint2.getY() +
                time * time * time * end.getY());
        position.setZ(Math.pow(1 - time, 3) * start.getZ() +
                Math.pow(1 - time, 2) * 3 * time * controlPoint1.getZ() +
                (1 - time) * 3 * time * time * controlPoint2.getZ() +
                time * time * time * end.getZ());

        return position;
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
