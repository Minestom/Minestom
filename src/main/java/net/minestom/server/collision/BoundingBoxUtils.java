package net.minestom.server.collision;

import net.minestom.server.coordinate.Vec;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BoundingBoxUtils {
    record Faces(Map<Vec, List<Vec>> query) {
        public Faces {
            query = Map.copyOf(query);
        }
    }

    private List<Vec> buildSet(Set<Vec> a) {
        return a.stream().toList();
    }

    private List<Vec> buildSet(Set<Vec> a, Set<Vec> b) {
        Set<Vec> allFaces = new HashSet<>();
        Stream.of(a, b).forEach(allFaces::addAll);
        return allFaces.stream().toList();
    }

    private List<Vec> buildSet(Set<Vec> a, Set<Vec> b, Set<Vec> c) {
        Set<Vec> allFaces = new HashSet<>();
        Stream.of(a, b, c).forEach(allFaces::addAll);
        return allFaces.stream().toList();
    }

    static Faces retrieveFaces(BoundingBox boundingBox) {
        double minX = boundingBox.minX();
        double maxX = boundingBox.maxX();
        double minY = boundingBox.minY();
        double maxY = boundingBox.maxY();
        double minZ = boundingBox.minZ();
        double maxZ = boundingBox.maxZ();

        // Calculate steppings for each axis
        // Start at minimum, increase by step size until we reach maximum
        // This is done to catch all blocks that are part of that axis
        // Since this stops before max point is reached, we add the max point after
        final List<Double> stepsX = IntStream.rangeClosed(0, (int)((maxX-minX))).mapToDouble(x -> x + minX).boxed().collect(Collectors.toCollection(ArrayList<Double>::new));
        final List<Double> stepsY = IntStream.rangeClosed(0, (int)((maxY-minY))).mapToDouble(x -> x + minY).boxed().collect(Collectors.toCollection(ArrayList<Double>::new));
        final List<Double> stepsZ = IntStream.rangeClosed(0, (int)((maxZ-minZ))).mapToDouble(x -> x + minZ).boxed().collect(Collectors.toCollection(ArrayList<Double>::new));

        stepsX.add(maxX);
        stepsY.add(maxY);
        stepsZ.add(maxZ);

        final Set<Vec> bottom = new HashSet<>();
        final Set<Vec> top = new HashSet<>();
        final Set<Vec> left = new HashSet<>();
        final Set<Vec> right = new HashSet<>();
        final Set<Vec> front = new HashSet<>();
        final Set<Vec> back = new HashSet<>();

        CartesianProduct.product(stepsX, stepsY).forEach(cross -> {
            double i = (double) ((List<?>)cross).get(0);
            double j = (double) ((List<?>)cross).get(1);
            front.add(new Vec(i, j, minZ));
            back.add(new Vec(i, j, maxZ));
        });

        CartesianProduct.product(stepsY, stepsZ).forEach(cross -> {
            double j = (double) ((List<?>)cross).get(0);
            double k = (double) ((List<?>)cross).get(1);
            left.add(new Vec(minX, j, k));
            right.add(new Vec(maxX, j, k));
        });

        CartesianProduct.product(stepsX, stepsZ).forEach(cross -> {
            double i = (double) ((List<?>)cross).get(0);
            double k = (double) ((List<?>)cross).get(1);
            bottom.add(new Vec(i, minY, k));
            top.add(new Vec(i, maxY, k));
        });

        // X   -1 left    |  1 right
        // Y   -1 bottom  |  1 top
        // Z   -1 front   |  1 back
        var query = new HashMap<Vec, List<Vec>>();
        query.put(new Vec(0, 0, 0), List.of());

        query.put(new Vec(-1, 0, 0), buildSet(left));
        query.put(new Vec(1, 0, 0), buildSet(right));
        query.put(new Vec(0, -1, 0), buildSet(bottom));
        query.put(new Vec(0, 1, 0), buildSet(top));
        query.put(new Vec(0, 0, -1), buildSet(front));
        query.put(new Vec(0, 0, 1), buildSet(back));

        query.put(new Vec(0, -1, -1), buildSet(bottom, front));
        query.put(new Vec(0, -1, 1), buildSet(bottom, back));
        query.put(new Vec(0, 1, -1), buildSet(top, front));
        query.put(new Vec(0, 1, 1), buildSet(top, back));

        query.put(new Vec(-1, -1, 0), buildSet(left, bottom));
        query.put(new Vec(-1, 1, 0), buildSet(left, top));
        query.put(new Vec(1, -1, 0), buildSet(right, bottom));
        query.put(new Vec(1, 1, 0), buildSet(right, top));

        query.put(new Vec(-1, 0, -1), buildSet(left, front));
        query.put(new Vec(-1, 0, 1), buildSet(left, back));
        query.put(new Vec(1, 0, -1), buildSet(right, front));
        query.put(new Vec(1, 0, 1), buildSet(right, back));

        query.put(new Vec(1, 1, 1), buildSet(right, top, back));
        query.put(new Vec(1, 1, -1), buildSet(right, top, front));
        query.put(new Vec(1, -1, 1), buildSet(right, bottom, back));
        query.put(new Vec(1, -1, -1), buildSet(right, bottom, front));
        query.put(new Vec(-1, 1, 1), buildSet(left, top, back));
        query.put(new Vec(-1, 1, -1), buildSet(left, top, front));
        query.put(new Vec(-1, -1, 1), buildSet(left, bottom, back));
        query.put(new Vec(-1, -1, -1), buildSet(left, bottom, front));

        return new Faces(query);
    }
}
