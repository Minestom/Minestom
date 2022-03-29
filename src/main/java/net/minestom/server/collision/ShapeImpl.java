package net.minestom.server.collision;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ShapeImpl implements Shape {
    private record Rectangle(double x1, double y1, double x2, double y2) {};

    private static final Pattern PATTERN = Pattern.compile("\\d.\\d{1,3}", Pattern.MULTILINE);
    private final BoundingBox[] collisionBoundingBoxes;
    private final Point relativeStart, relativeEnd;

    private final BoundingBox[] occlusionBoundingBoxes;
    private final byte occlusion;

    private final Supplier<Material> block;

    private ShapeImpl(BoundingBox[] collisionBoundingBoxes, BoundingBox[] occlusionBoundingBoxes, Supplier<Material> block) {
        this.collisionBoundingBoxes = collisionBoundingBoxes;
        this.occlusionBoundingBoxes = occlusionBoundingBoxes;
        this.block = block;

        // Find bounds of collision
        {
            double minX = 1, minY = 1, minZ = 1;
            double maxX = 0, maxY = 0, maxZ = 0;
            for (BoundingBox blockSection : this.collisionBoundingBoxes) {
                // Min
                if (blockSection.minX() < minX) minX = blockSection.minX();
                if (blockSection.minY() < minY) minY = blockSection.minY();
                if (blockSection.minZ() < minZ) minZ = blockSection.minZ();
                // Max
                if (blockSection.maxX() > maxX) maxX = blockSection.maxX();
                if (blockSection.maxY() > maxY) maxY = blockSection.maxY();
                if (blockSection.maxZ() > maxZ) maxZ = blockSection.maxZ();
            }
            this.relativeStart = new Vec(minX, minY, minZ);
            this.relativeEnd = new Vec(maxX, maxY, maxZ);
        }

        byte occlusion = 0;
        for (BlockFace f : BlockFace.values()) {
            occlusion |= ((isFaceCovered(computeOcclusionSet(f)) ? 0b1 : 0b0) << (byte) f.ordinal());
        }

        this.occlusion = occlusion;
    }

    static private BoundingBox[] parseRegistryBoundingBoxString(String str) {
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

            final BoundingBox bb = new BoundingBox(boundXSize, boundYSize, boundZSize, new Vec(minX, minY, minZ));
            assert bb.minX() == minX;
            assert bb.minY() == minY;
            assert bb.minZ() == minZ;
            boundingBoxes[i] = bb;
        }

        return boundingBoxes;
    }

    private static Rectangle clipRectangle(Rectangle covering, Rectangle toCover) {
        final double x1 = Math.max(covering.x1(), toCover.x1());
        final double y1 = Math.max(covering.y1(), toCover.y1());
        final double x2 = Math.min(covering.x2(), toCover.x2());
        final double y2 = Math.min(covering.y2(), toCover.y2());
        return new Rectangle(x1, y1, x2, y2);
    }

    private static List<Rectangle> getRemaining (Rectangle covering, Rectangle toCover) {
        List<Rectangle> remaining = new ArrayList<>();
        covering = clipRectangle(covering, toCover);

        // Up
        if (covering.y1() > toCover.y1()) {
            remaining.add(new Rectangle(toCover.x1(), toCover.y1(), toCover.x2(), covering.y1()));
        }

        // Down
        if (covering.y2() < toCover.y2()) {
            remaining.add(new Rectangle(toCover.x1(), covering.y2(), toCover.x1(), toCover.y2()));
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

    // There's an n log n algorithm that involves sorting but it's not worth implementing
    public static boolean isFaceCovered (List<Rectangle> covering) {
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
            if (toCover.isEmpty()) return true;
        }

        return false;
    }

    public boolean isAdditionOccluded(Shape shape, BlockFace face) {
        List<Rectangle> allRectangles = ((ShapeImpl)shape).computeOcclusionSet(face.getOppositeFace());
        allRectangles.addAll(computeOcclusionSet(face));
        return isFaceCovered(allRectangles);
    }

    private List<Rectangle> computeOcclusionSet(BlockFace face) {
        List<Rectangle> rSet = new ArrayList<>();

        for (BoundingBox boundingBox : this.occlusionBoundingBoxes) {
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

    static ShapeImpl parseBlockFromRegistry(String collision, String occlusion, Supplier<Material> block) {
        return new ShapeImpl(parseRegistryBoundingBoxString(collision), parseRegistryBoundingBoxString(occlusion), block);
    }

    @Override
    public @NotNull Point relativeStart() {
        return relativeStart;
    }

    @Override
    public @NotNull Point relativeEnd() {
        return relativeEnd;
    }

    @Override
    public boolean isOccluded(BlockFace face) {
        return ((occlusion >> face.ordinal()) & 1) == 1;
    }

    @Override
    public boolean intersectBox(@NotNull Point position, @NotNull BoundingBox boundingBox) {
        for (BoundingBox blockSection : collisionBoundingBoxes) {
            if (boundingBox.intersectBox(position, blockSection)) return true;
        }
        return false;
    }

    @Override
    public boolean intersectBoxSwept(@NotNull Point rayStart, @NotNull Point rayDirection, @NotNull Point shapePos, @NotNull BoundingBox moving, @NotNull SweepResult finalResult) {
        boolean hitBlock = false;
        for (BoundingBox blockSection : collisionBoundingBoxes) {
            // Fast check to see if a collision happens
            // Uses minkowski sum
            if (!RayUtils.BoundingBoxIntersectionCheck(moving, rayStart, rayDirection, blockSection, shapePos))
                continue;
            // Update final result if the temp result collision is sooner than the current final result
            if (RayUtils.SweptAABB(moving, rayStart, rayDirection, blockSection, shapePos, finalResult)) {
                finalResult.collidedShapePosition = shapePos;
                finalResult.collidedShape = this;
                finalResult.blockType = block.get().block();
            }
            hitBlock = true;
        }
        return hitBlock;
    }
}
