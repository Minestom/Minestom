package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public interface Shape {
    boolean intersectBox(Point positionRelative, BoundingBox boundingBox);

    boolean intersectEntitySwept(Point rayStart, Point rayDirection, Point blockPos, BoundingBox moving, Point entityPosition, SweepResult tempResult, SweepResult finalResult);

    @NotNull Point relativeStart();

    @NotNull Point relativeEnd();
}
