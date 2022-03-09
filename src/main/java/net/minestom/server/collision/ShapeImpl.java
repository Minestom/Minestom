package net.minestom.server.collision;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ShapeImpl implements Shape {
    private static final Pattern PATTERN = Pattern.compile("\\d.\\d{1,3}", Pattern.MULTILINE);
    private final BoundingBox[] blockSections;
    private final Supplier<Material> block;

    private ShapeImpl(BoundingBox[] boundingBoxes, Supplier<Material> block) {
        this.blockSections = boundingBoxes;
        this.block = block;
    }

    static ShapeImpl parseBlockFromRegistry(String str, Supplier<Material> block) {
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
        return new ShapeImpl(boundingBoxes, block);
    }

    @Override
    public @NotNull Point relativeStart() {
        double minX = 1, minY = 1, minZ = 1;
        for (BoundingBox blockSection : blockSections) {
            if (blockSection.minX() < minX) minX = blockSection.minX();
            if (blockSection.minY() < minY) minY = blockSection.minY();
            if (blockSection.minZ() < minZ) minZ = blockSection.minZ();
        }
        return new Vec(minX, minY, minZ);
    }

    @Override
    public @NotNull Point relativeEnd() {
        double maxX = 1, maxY = 1, maxZ = 1;
        for (BoundingBox blockSection : blockSections) {
            if (blockSection.maxX() < maxX) maxX = blockSection.maxX();
            if (blockSection.maxY() < maxY) maxY = blockSection.maxY();
            if (blockSection.maxZ() < maxZ) maxZ = blockSection.maxZ();
        }
        return new Vec(maxX, maxY, maxZ);
    }

    @Override
    public boolean intersectBox(@NotNull Point position, @NotNull BoundingBox boundingBox) {
        for (BoundingBox blockSection : blockSections) {
            if (boundingBox.intersectBox(position, blockSection)) return true;
        }
        return false;
    }

    @Override
    public boolean intersectBoxSwept(@NotNull Point rayStart, @NotNull Point rayDirection, @NotNull Point shapePos, @NotNull BoundingBox moving, @NotNull SweepResult finalResult) {
        boolean hitBlock = false;
        SweepResult tempResult = new SweepResult(1, 0, 0, 0, null);
        for (BoundingBox blockSection : blockSections) {
            // Fast check to see if a collision happens
            // Uses minkowski sum
            if (!RayUtils.BoundingBoxIntersectionCheck(moving, rayStart, rayDirection, blockSection, shapePos))
                continue;

            // Longer check to get result of collision
            RayUtils.SweptAABB(moving, rayStart, rayDirection, blockSection, shapePos, tempResult);
            // Update final result if the temp result collision is sooner than the current final result

            if (tempResult.res < finalResult.res) {
                finalResult.res = tempResult.res;
                finalResult.normalX = tempResult.normalX;
                finalResult.normalY = tempResult.normalY;
                finalResult.normalZ = tempResult.normalZ;
                finalResult.collidedShapePosition = shapePos;
                finalResult.collidedShape = this;
                finalResult.blockType = block.get().block();
            }
            hitBlock = true;
        }
        return hitBlock;
    }
}
