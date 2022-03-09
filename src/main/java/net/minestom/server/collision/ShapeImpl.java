package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ShapeImpl implements Shape {
    private final List<BoundingBox> blockSections;
    private final Supplier<Material> block;

    ShapeImpl(List<BoundingBox> boundingBoxes, Supplier<Material> block) {
        this.blockSections = boundingBoxes;
        this.block = block;
    }

    static ShapeImpl parseBlockFromRegistry(String str, Supplier<Material> block) {
        final String regex = "\\d.\\d{1,3}";
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(str);

        ArrayList<Double> vals = new ArrayList<>();
        while (matcher.find()) {
            double newVal = Double.parseDouble(matcher.group());
            vals.add(newVal);
        }

        List<BoundingBox> boundingBoxes = new ArrayList<>();
        final int count = vals.size() / 6;
        for (int i = 0; i < count; ++i) {
            final double boundXSize = vals.get(3 + 6 * i) - vals.get(0 + 6 * i);
            final double boundYSize = vals.get(4 + 6 * i) - vals.get(1 + 6 * i);
            final double boundZSize = vals.get(5 + 6 * i) - vals.get(2 + 6 * i);

            final double minX, minY, minZ;
            minX = vals.get(0 + 6 * i);
            minY = vals.get(1 + 6 * i);
            minZ = vals.get(2 + 6 * i);
            var bb = new BoundingBox(boundXSize, boundYSize, boundZSize);
            bb.offset = new Vec(minX, minY, minZ);
            assert bb.minX() == minX;
            assert bb.minY() == minY;
            assert bb.minZ() == minZ;
            boundingBoxes.add(bb);
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

        return new Pos(minX, minY, minZ);
    }

    @Override
    public @NotNull Point relativeEnd() {
        double maxX = 1, maxY = 1, maxZ = 1;

        for (BoundingBox blockSection : blockSections) {
            if (blockSection.maxX() < maxX) maxX = blockSection.maxX();
            if (blockSection.maxY() < maxY) maxY = blockSection.maxY();
            if (blockSection.maxZ() < maxZ) maxZ = blockSection.maxZ();
        }

        return new Pos(maxX, maxY, maxZ);
    }

    @Override
    public boolean intersectBox(Point position, BoundingBox boundingBox) {
        return blockSections.stream().anyMatch(section -> boundingBox.intersectBox(position, section));
    }

    @Override
    public boolean intersectBoxSwept(Point rayStart, Point rayDirection, Point blockPos, BoundingBox moving, SweepResult tempResult, SweepResult finalResult) {
        List<BoundingBox> collidables = blockSections.stream().filter(blockSection -> {
            // Fast check to see if a collision happens
            // Uses minkowski sum
            return RayUtils.BoundingBoxIntersectionCheck(
                    moving, rayStart, rayDirection,
                    blockSection,
                    blockPos
            );
        }).toList();

        boolean hitBlock = false;

        for (BoundingBox bb : collidables) {
            // Longer check to get result of collision
            RayUtils.SweptAABB(moving, rayStart, rayDirection, bb, blockPos, tempResult);

            // Update final result if the temp result collision is sooner than the current final result
            if (tempResult.res < finalResult.res) {
                finalResult.res = tempResult.res;
                finalResult.normalX = tempResult.normalX;
                finalResult.normalY = tempResult.normalY;
                finalResult.normalZ = tempResult.normalZ;
                finalResult.collisionBlock = blockPos;
                finalResult.blockType = block.get().block();
            }

            hitBlock = true;
        }

        return hitBlock;
    }
}
