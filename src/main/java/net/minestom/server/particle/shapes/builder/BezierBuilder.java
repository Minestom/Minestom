package net.minestom.server.particle.shapes.builder;

import net.minestom.server.particle.shapes.BezierLine;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BezierBuilder {
    private final List<Position> controlPoints = new ArrayList<>();
    private Position start = new Position();
    private Position end = new Position();
    private double step = 0.1;

    public @NotNull BezierBuilder start(@NotNull Position start) {
        this.start = start;
        return this;
    }

    public @NotNull BezierBuilder end(@NotNull Position end) {
        this.end = end;
        return this;
    }

    public @NotNull BezierBuilder addControlPoint(@NotNull Position controlPoint) {
        this.controlPoints.add(controlPoint);
        return this;
    }

    public @NotNull BezierBuilder step(double step) {
        this.step = step;
        return this;
    }

    public @NotNull BezierLine build() {
        return new BezierLine(start, end, controlPoints.toArray(Position[]::new), step);
    }
}
