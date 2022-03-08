package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ShapeImpl implements Shape {
    private final List<? extends Collidable> blockSections;
    private final Supplier<Material> block;

    ShapeImpl(List<? extends Collidable> boundingBoxes, Supplier<Material> block) {
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

        List<BlockSection> boundingBoxes = new ArrayList<>();
        final int count = vals.size() / 6;
        for (int i = 0; i < count; ++i) {
            final double boundXSize = vals.get(3 + 6 * i) - vals.get(0 + 6 * i);
            final double boundYSize = vals.get(4 + 6 * i) - vals.get(1 + 6 * i);
            final double boundZSize = vals.get(5 + 6 * i) - vals.get(2 + 6 * i);

            final double minX, minY, minZ;
            minX = vals.get(0 + 6 * i);
            minY = vals.get(1 + 6 * i);
            minZ = vals.get(2 + 6 * i);

            boundingBoxes.add(new BlockSection(minX, minY, minZ, boundXSize, boundYSize, boundZSize));
        }

        return new ShapeImpl(boundingBoxes, block);
    }

    @Override
    public boolean intersectEntity(Point position, BoundingBox boundingBox, Point blockPosition) {
        return blockSections.stream().anyMatch(section -> boundingBox.intersectCollidable(position, section, blockPosition));
    }

    @Override
    public boolean intersectEntitySwept(Point rayStart, Point rayDirection, Point blockPos, BoundingBox moving, Point entityPosition, SweepResult tempResult, SweepResult finalResult) {
        List<? extends Collidable> collidables = blockSections.stream().filter(blockSection -> {
            // Fast check to see if a collision happens
            // Uses minkowski sum
            return RayUtils.BoundingBoxIntersectionCheck(
                    moving, rayStart, rayDirection,
                    blockSection,
                    blockPos
            );
        }).toList();

        boolean hitBlock = false;

        for (Collidable bb : collidables) {
            // Longer check to get result of collision
            RayUtils.SweptAABB(moving, entityPosition, rayDirection, bb, blockPos, tempResult);

            // Update final result if the temp result collision is sooner than the current final result
            if (tempResult.res < finalResult.res) {
                finalResult.res = tempResult.res;
                finalResult.normalx = tempResult.normalx;
                finalResult.normaly = tempResult.normaly;
                finalResult.normalz = tempResult.normalz;
                finalResult.collisionBlock = blockPos;
                finalResult.blockType = block.get().block();
            }

            hitBlock = true;
        }

        return hitBlock;
    }

    record BlockSection(double minX, double minY, double minZ, double width, double height,
                        double depth) implements Collidable {
        @Override
        public double maxX() {
            return minX + width;
        }

        @Override
        public double maxY() {
            return minY + height;
        }

        @Override
        public double maxZ() {
            return minZ + depth;
        }
    }
}
