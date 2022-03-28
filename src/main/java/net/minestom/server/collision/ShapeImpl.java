package net.minestom.server.collision;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ShapeImpl implements Shape {
    private static final Pattern PATTERN = Pattern.compile("\\d.\\d{1,3}", Pattern.MULTILINE);
    private final BoundingBox[] collisionBoundingBoxes;
    private final Point relativeStart, relativeEnd;

    private final BoundingBox[] occlusionBoundingBoxes;
    private final Point relativeOcclusionEnd, relativeOcclusionStart;

    private final Supplier<Material> block;

    private ShapeImpl(BoundingBox[] collisionBoundingBoxes, BoundingBox[] occlusionBoundingBoxes, Supplier<Material> block) {
        this.collisionBoundingBoxes = collisionBoundingBoxes;
        this.occlusionBoundingBoxes = occlusionBoundingBoxes;
        this.block = block;

        // Find bounds of collision
        {
            double minX = 1, minY = 1, minZ = 1;
            double maxX = 1, maxY = 1, maxZ = 1;
            for (BoundingBox blockSection : this.collisionBoundingBoxes) {
                // Min
                if (blockSection.minX() < minX) minX = blockSection.minX();
                if (blockSection.minY() < minY) minY = blockSection.minY();
                if (blockSection.minZ() < minZ) minZ = blockSection.minZ();
                // Max
                if (blockSection.maxX() < maxX) maxX = blockSection.maxX();
                if (blockSection.maxY() < maxY) maxY = blockSection.maxY();
                if (blockSection.maxZ() < maxZ) maxZ = blockSection.maxZ();
            }
            this.relativeStart = new Vec(minX, minY, minZ);
            this.relativeEnd = new Vec(maxX, maxY, maxZ);
        }

        // Find bounds of occlusion
        {
            double minX = 1, minY = 1, minZ = 1;
            double maxX = 1, maxY = 1, maxZ = 1;
            for (BoundingBox blockSection : this.occlusionBoundingBoxes) {
                // Min
                if (blockSection.minX() < minX) minX = blockSection.minX();
                if (blockSection.minY() < minY) minY = blockSection.minY();
                if (blockSection.minZ() < minZ) minZ = blockSection.minZ();
                // Max
                if (blockSection.maxX() < maxX) maxX = blockSection.maxX();
                if (blockSection.maxY() < maxY) maxY = blockSection.maxY();
                if (blockSection.maxZ() < maxZ) maxZ = blockSection.maxZ();
            }
            this.relativeOcclusionStart = new Vec(minX, minY, minZ);
            this.relativeOcclusionEnd = new Vec(maxX, maxY, maxZ);
        }
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
    public boolean isOcclusionFaceFull(BlockFace face) {
        return switch (face) {
            case NORTH -> // negative Z
                    relativeOcclusionStart.z() == 0
                            && relativeOcclusionStart.x() == 0 && relativeOcclusionEnd.x() == 1
                            && relativeOcclusionStart.y() == 0 && relativeOcclusionEnd.y() == 1;
            case SOUTH -> // positive Z
                    relativeOcclusionStart.z() == 1
                            && relativeOcclusionStart.x() == 0 && relativeOcclusionEnd.x() == 1
                            && relativeOcclusionStart.y() == 0 && relativeOcclusionEnd.y() == 1;
            case WEST -> // negative X
                    relativeOcclusionStart.x() == 0
                            && relativeOcclusionStart.z() == 0 && relativeOcclusionEnd.z() == 1
                            && relativeOcclusionStart.y() == 0 && relativeOcclusionEnd.y() == 1;
            case EAST -> // positive X
                    relativeOcclusionStart.x() == 1
                            && relativeOcclusionStart.z() == 0 && relativeOcclusionEnd.z() == 1
                            && relativeOcclusionStart.y() == 0 && relativeOcclusionEnd.y() == 1;
            case BOTTOM -> // negative Y
                    relativeOcclusionStart.y() == 0
                            && relativeOcclusionStart.x() == 0 && relativeOcclusionEnd.x() == 1
                            && relativeOcclusionStart.z() == 0 && relativeOcclusionEnd.z() == 1;
            case TOP -> // positive Y
                    relativeOcclusionStart.y() == 1
                            && relativeOcclusionStart.x() == 0 && relativeOcclusionEnd.x() == 1
                            && relativeOcclusionStart.z() == 0 && relativeOcclusionEnd.z() == 1;
        };
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
