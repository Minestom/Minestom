package net.minestom.server.coordinate;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface AreaQuery {

    static boolean contains(Area area, Point point) {
        if (area.min().x() > point.x() || area.max().x() < point.x() ||
                area.min().y() > point.y() || area.max().y() < point.y() ||
                area.min().z() > point.z() || area.max().z() < point.z()) {
            return false;
        }

        Class<?> areaClass = area.getClass();
        if (areaClass == AreaImpl.Fill.class) {
            // We can return true here as we know the point is within the (filled) area
            return true;
        }

        if (areaClass == AreaImpl.SetArea.class) {
            // Just check if the point is in the set
            return ((AreaImpl.SetArea) area).points().contains(point);
        }

        // Attempt to subdivide the area for a faster check
        if (area instanceof Area.HasChildren parent) {
            return parent.children()
                    .stream()
                    .anyMatch(child -> contains(child, point));
        }


        // Last resort, iterate over all points (Very slow)
        for (Point pos : area) {
            if (pos.equals(point)) {
                return true;
            }
        }

        return false;
    }

    static boolean hasOverlap(Area a, Area b) {
        if (a.min().x() > b.max().x() || a.max().x() < b.min().x() ||
                a.min().y() > b.max().y() || a.max().y() < b.min().y() ||
                a.min().z() > b.max().z() || a.max().z() < b.min().z()) {
            return false;
        }

        // TODO: Add optimizations for specific area types
        Class<?> aClass = a.getClass();
        Class<?> bClass = b.getClass();

        if (aClass == AreaImpl.Fill.class) {
            if (bClass == AreaImpl.Fill.class && hasOverlapFillFill((AreaImpl.Fill) a, (AreaImpl.Fill) b)) return true;
            if (bClass == AreaImpl.SetArea.class && hasOverlapFillSet((AreaImpl.Fill) a, (AreaImpl.SetArea) b)) return true;
        }
        if (aClass == AreaImpl.SetArea.class) {
            if (bClass == AreaImpl.Fill.class && hasOverlapFillSet((AreaImpl.Fill) b, (AreaImpl.SetArea) a)) return true;
            if (bClass == AreaImpl.SetArea.class && hasOverlapSetSet((AreaImpl.SetArea) a, (AreaImpl.SetArea) b)) return true;
        }

        // Attempt to subdivide the area for a faster check
        if (a instanceof Area.HasChildren parentA) {
            if (b instanceof Area.HasChildren parentB) {
                return parentA.children()
                        .stream()
                        .anyMatch(childA ->
                                parentB.children()
                                        .stream()
                                        .anyMatch(childB -> hasOverlap(childA, childB)));
            } else {
                return parentA.children()
                        .stream()
                        .anyMatch(childA -> hasOverlap(childA, b));
            }
        } else if (b instanceof Area.HasChildren parentB) {
            return parentB.children()
                    .stream()
                    .anyMatch(childB -> hasOverlap(a, childB));
        }


        // Last resort, iterate over all points (Very slow)
        for (Point pointA : a) {
            for (Point pointB : b) {
                if (pointA.equals(pointB)) {
                    return true;
                }
            }
        }

        return false;
    }

    static boolean hasOverlapSetSet(AreaImpl.SetArea a, AreaImpl.SetArea b) {
        // We want A to be the smallest set (for performance reasons)
        if (a.points().size() > b.points().size()) {
            return hasOverlapSetSet(b, a);
        }
        return a.points().stream().anyMatch(b.points()::contains);
    }

    static boolean hasOverlapFillSet(AreaImpl.Fill a, AreaImpl.SetArea b) {
        return b.points().stream().anyMatch(a::contains);
    }

    static boolean hasOverlapFillFill(AreaImpl.Fill a, AreaImpl.Fill b) {
        // Fill areas can be checked for overlap by checking if the min/max points are within the other area
        return a.contains(b.min()) || a.contains(b.max());
    }
}
