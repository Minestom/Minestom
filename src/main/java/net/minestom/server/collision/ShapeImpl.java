package net.minestom.server.collision;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record ShapeImpl(ShapeData shapeData, OcclusionData occlusionData) implements Shape {
    private static final Pattern PATTERN = Pattern.compile("\\d.\\d+", Pattern.MULTILINE);

    record ShapeData(List<BoundingBox> boundingBoxes,
                     Point relativeStart, Point relativeEnd,
                     byte fullFaces) {
        public ShapeData {
            boundingBoxes = List.copyOf(boundingBoxes);
        }
    }

    record OcclusionData(byte blockOcclusion, byte airOcclusion, byte lightEmission) {}

    /**
     * Computes the occlusion for a given face.
     *
     * @param covering The rectangle set to check for covering.
     * @return 0 if face is not covered, 1 if face is covered partially, 2 if face is fully covered.
     */
    private static byte isFaceCovered(List<Rectangle> covering) {
        if (covering.isEmpty()) return 0;
        Rectangle r = new Rectangle(0, 0, 1, 1);
        List<Rectangle> toCover = new ArrayList<>();
        toCover.add(r);
        for (Rectangle rect : covering) {
            List<Rectangle> nextCovering = new ArrayList<>();
            for (Rectangle toCoverRect : toCover) {
                List<Rectangle> remaining = getRemaining(rect, toCoverRect);
                nextCovering.addAll(remaining);
            }
            toCover = nextCovering;
            if (toCover.isEmpty()) return 2;
        }
        return 1;
    }

    @Override
    public Point relativeStart() {
        return shapeData.relativeStart;
    }

    @Override
    public Point relativeEnd() {
        return shapeData.relativeEnd;
    }

    @Override
    public boolean isOccluded(Shape shape, BlockFace face) {
        final OcclusionData occlusionData = this.occlusionData;
        final OcclusionData otherOcclusionData = ((ShapeImpl) shape).occlusionData;

        final boolean hasBlockOcclusion = (((occlusionData.blockOcclusion >> face.ordinal()) & 1) == 1);
        final boolean hasBlockOcclusionOther = ((otherOcclusionData.blockOcclusion >> face.getOppositeFace().ordinal()) & 1) == 1;

        if (occlusionData.lightEmission > 0) return hasBlockOcclusionOther;

        // If either face is full, return true
        if (hasBlockOcclusion || hasBlockOcclusionOther) return true;

        final boolean hasAirOcclusion = (((occlusionData.airOcclusion >> face.ordinal()) & 1) == 1);
        final boolean hasAirOcclusionOther = ((otherOcclusionData.airOcclusion >> face.getOppositeFace().ordinal()) & 1) == 1;

        // If a single face is air, return false
        if (hasAirOcclusion || hasAirOcclusionOther) return false;

        final ShapeData shapeData = this.shapeData;
        final ShapeData otherShapeData = ((ShapeImpl) shape).shapeData;

        // Comparing two partial faces. Computation needed
        List<Rectangle> allRectangles = computeOcclusionSet(face.getOppositeFace(), otherShapeData.boundingBoxes);
        allRectangles.addAll(computeOcclusionSet(face, shapeData.boundingBoxes));
        return isFaceCovered(allRectangles) == 2;
    }

    @Override
    public boolean isFaceFull(BlockFace face) {
        return (((shapeData.fullFaces >> face.ordinal()) & 1) == 1);
    }

    @Override
    public boolean intersectBox(Point position, BoundingBox boundingBox) {
        for (BoundingBox blockSection : shapeData.boundingBoxes) {
            if (boundingBox.intersectBox(position, blockSection)) return true;
        }
        return false;
    }

    @Override
    public boolean intersectBoxSwept(Point rayStart, Point rayDirection,
                                     Point shapePos, BoundingBox moving, SweepResult finalResult) {
        boolean hitBlock = false;
        for (BoundingBox blockSection : shapeData.boundingBoxes) {
            // Update final result if the temp result collision is sooner than the current final result
            if (RayUtils.BoundingBoxIntersectionCheck(moving, rayStart, rayDirection, blockSection, shapePos, finalResult)) {
                finalResult.collidedPositionX = rayStart.x() + rayDirection.x() * finalResult.res;
                finalResult.collidedPositionY = rayStart.y() + rayDirection.y() * finalResult.res;
                finalResult.collidedPositionZ = rayStart.z() + rayDirection.z() * finalResult.res;
                finalResult.collidedShapeX = shapePos.x();
                finalResult.collidedShapeY = shapePos.y();
                finalResult.collidedShapeZ = shapePos.z();
                finalResult.collidedShape = this;
                hitBlock = true;
            }
        }
        return hitBlock;
    }

    /**
     * Gets the bounding boxes for this shape. There will be more than one bounds for more complex shapes e.g.
     * stairs.
     *
     * @return the bounding boxes for this shape
     */
    public @Unmodifiable List<BoundingBox> boundingBoxes() {
        return shapeData.boundingBoxes;
    }

    static ShapeImpl parseShapeFromRegistry(String shape, byte lightEmission) {
        BoundingBox[] boundingBoxes = parseRegistryBoundingBoxString(shape);
        final ShapeData shapeData = shapeData(List.of(boundingBoxes));
        final OcclusionData occlusionData = occlusionData(shapeData, lightEmission);
        return new ShapeImpl(shapeData, occlusionData);
    }

    static ShapeImpl emptyShape(byte lightEmission) {
        BoundingBox[] boundingBoxes = new BoundingBox[0];
        final ShapeData shapeData = shapeData(List.of(boundingBoxes));
        final OcclusionData occlusionData = occlusionData(shapeData, lightEmission);
        return new ShapeImpl(shapeData, occlusionData);
    }

    private static BoundingBox[] parseRegistryBoundingBoxString(String str) {
        final Matcher matcher = PATTERN.matcher(str);
        DoubleList vals = new DoubleArrayList();
        while (matcher.find()) {
            double newVal = Double.parseDouble(matcher.group());
            vals.add(newVal);
        }
        final int count = vals.size() / 6;
        BoundingBox[] boundingBoxes = new BoundingBox[count];
        for (int i = 0; i < count; ++i) {
            final double minX = vals.getDouble(0 + 6 * i);
            final double minY = vals.getDouble(1 + 6 * i);
            final double minZ = vals.getDouble(2 + 6 * i);

            final double boundXSize = vals.getDouble(3 + 6 * i) - minX;
            final double boundYSize = vals.getDouble(4 + 6 * i) - minY;
            final double boundZSize = vals.getDouble(5 + 6 * i) - minZ;

            final Vec min = new Vec(minX, minY, minZ);
            final Vec max = new Vec(minX + boundXSize, minY + boundYSize, minZ + boundZSize);
            final BoundingBox bb = new BoundingBox(min, max);
            assert bb.minX() == minX;
            assert bb.minY() == minY;
            assert bb.minZ() == minZ;
            boundingBoxes[i] = bb;
        }
        return boundingBoxes;
    }

    private static ShapeData shapeData(List<BoundingBox> collisionBoundingBoxes) {
        // Find bounds of collision
        Vec relativeStart;
        Vec relativeEnd;
        if (!collisionBoundingBoxes.isEmpty()) {
            double minX = 1, minY = 1, minZ = 1;
            double maxX = 0, maxY = 0, maxZ = 0;
            for (BoundingBox blockSection : collisionBoundingBoxes) {
                // Min
                if (blockSection.minX() < minX) minX = blockSection.minX();
                if (blockSection.minY() < minY) minY = blockSection.minY();
                if (blockSection.minZ() < minZ) minZ = blockSection.minZ();
                // Max
                if (blockSection.maxX() > maxX) maxX = blockSection.maxX();
                if (blockSection.maxY() > maxY) maxY = blockSection.maxY();
                if (blockSection.maxZ() > maxZ) maxZ = blockSection.maxZ();
            }
            relativeStart = new Vec(minX, minY, minZ);
            relativeEnd = new Vec(maxX, maxY, maxZ);
        } else {
            relativeStart = Vec.ZERO;
            relativeEnd = Vec.ZERO;
        }

        byte fullCollisionFaces = 0;
        for (BlockFace f : BlockFace.values()) {
            final byte res = isFaceCovered(computeOcclusionSet(f, collisionBoundingBoxes));
            fullCollisionFaces |= ((res == 2) ? 0b1 : 0b0) << (byte) f.ordinal();
        }

        return new ShapeData(collisionBoundingBoxes, relativeStart, relativeEnd, fullCollisionFaces);
    }

    private static OcclusionData occlusionData(ShapeData shapeData, byte lightEmission) {
        byte fullFaces = 0;
        byte airFaces = 0;
        for (BlockFace f : BlockFace.values()) {
            final byte res = isFaceCovered(computeOcclusionSet(f, shapeData.boundingBoxes));
            fullFaces |= ((res == 2) ? 0b1 : 0b0) << (byte) f.ordinal();
            airFaces |= ((res == 0) ? 0b1 : 0b0) << (byte) f.ordinal();
        }
        return new OcclusionData(fullFaces, airFaces, lightEmission);
    }

    private static List<Rectangle> computeOcclusionSet(BlockFace face, List<BoundingBox> boundingBoxes) {
        List<Rectangle> rSet = new ArrayList<>();
        for (BoundingBox boundingBox : boundingBoxes) {
            switch (face) {
                case NORTH -> // negative Z
                {
                    if (boundingBox.minZ() == 0)
                        rSet.add(new Rectangle(boundingBox.minX(), boundingBox.minY(), boundingBox.maxX(), boundingBox.maxY()));
                }
                case SOUTH -> // positive Z
                {
                    if (boundingBox.maxZ() == 1)
                        rSet.add(new Rectangle(boundingBox.minX(), boundingBox.minY(), boundingBox.maxX(), boundingBox.maxY()));
                }
                case WEST -> // negative X
                {
                    if (boundingBox.minX() == 0)
                        rSet.add(new Rectangle(boundingBox.minY(), boundingBox.minZ(), boundingBox.maxY(), boundingBox.maxZ()));
                }
                case EAST -> // positive X
                {
                    if (boundingBox.maxX() == 1)
                        rSet.add(new Rectangle(boundingBox.minY(), boundingBox.minZ(), boundingBox.maxY(), boundingBox.maxZ()));
                }
                case BOTTOM -> // negative Y
                {
                    if (boundingBox.minY() == 0)
                        rSet.add(new Rectangle(boundingBox.minX(), boundingBox.minZ(), boundingBox.maxX(), boundingBox.maxZ()));
                }
                case TOP -> // positive Y
                {
                    if (boundingBox.maxY() == 1)
                        rSet.add(new Rectangle(boundingBox.minX(), boundingBox.minZ(), boundingBox.maxX(), boundingBox.maxZ()));
                }
            }
        }
        return rSet;
    }

    private static List<Rectangle> getRemaining(Rectangle covering, Rectangle toCover) {
        List<Rectangle> remaining = new ArrayList<>();
        covering = clipRectangle(covering, toCover);
        // Up
        if (covering.y1() > toCover.y1()) {
            remaining.add(new Rectangle(toCover.x1(), toCover.y1(), toCover.x2(), covering.y1()));
        }
        // Down
        if (covering.y2() < toCover.y2()) {
            remaining.add(new Rectangle(toCover.x1(), covering.y2(), toCover.x2(), toCover.y2()));
        }
        // Left
        if (covering.x1() > toCover.x1()) {
            remaining.add(new Rectangle(toCover.x1(), covering.y1(), covering.x1(), covering.y2()));
        }
        //Right
        if (covering.x2() < toCover.x2()) {
            remaining.add(new Rectangle(covering.x2(), covering.y1(), toCover.x2(), covering.y2()));
        }
        return remaining;
    }

    private static Rectangle clipRectangle(Rectangle covering, Rectangle toCover) {
        final double x1 = Math.max(covering.x1(), toCover.x1());
        final double y1 = Math.max(covering.y1(), toCover.y1());
        final double x2 = Math.min(covering.x2(), toCover.x2());
        final double y2 = Math.min(covering.y2(), toCover.y2());
        return new Rectangle(x1, y1, x2, y2);
    }

    private record Rectangle(double x1, double y1, double x2, double y2) {
    }
}
