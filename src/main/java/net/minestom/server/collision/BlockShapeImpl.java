package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockShapeImpl implements BlockShape {
    private final List<BlockSection> blockSections;

    public BlockShapeImpl(List<BlockSection> boundingBoxes) {
        this.blockSections = boundingBoxes;
    }

    public static BlockShapeImpl parseBlockFromRegistry(String str) {
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

        return new BlockShapeImpl(boundingBoxes);
    }

    @Override
    public boolean intersectEntity(Point position, EntityBoundingBox boundingBox, Point blockPosition) {
        return blockSections.stream().anyMatch(section -> boundingBox.intersectCollidable(position, section, blockPosition));
    }

    @Override
    public List<? extends Collidable> intersectEntitySwept(Point rayStart, Point rayDirection, Point blockPos, EntityBoundingBox moving) {
        return blockSections.stream().filter(blockSection -> {
            // Fast check to see if a collision happens
            // Uses minkowski sum
            return RayUtils.RayBoundingBoxIntersectCheck(
                    rayDirection,
                    blockSection,
                    rayStart,
                    blockPos,
                    moving);
        }).toList();
    }
}
