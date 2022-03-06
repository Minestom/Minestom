package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockShape implements Collidable {
    private final List<BlockShapeUnit> blockUnits;

    public BlockShape(List<BlockShapeUnit> blockUnits) {
        this.blockUnits = blockUnits;
    }

    public static BlockShape parseRegistry(String blockRegistry) {
        final String regex = "\\d.\\d{1,3}";
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(blockRegistry);

        ArrayList<Double> vals = new ArrayList<>();
        while (matcher.find()) {
            double newVal = Double.parseDouble(matcher.group());
            vals.add(newVal);
        }

        List<BlockShapeUnit> blockUnits = new ArrayList<>();
        final int count = vals.size() / 6;
        for (int i = 0; i < count; ++i) {
            final double boundXSize = vals.get(3 + 6 * i) - vals.get(0 + 6 * i);
            final double boundYSize = vals.get(4 + 6 * i) - vals.get(1 + 6 * i);
            final double boundZSize = vals.get(5 + 6 * i) - vals.get(2 + 6 * i);

            final double minX, minY, minZ;
            minX = vals.get(0 + 6 * i);
            minY = vals.get(1 + 6 * i);
            minZ = vals.get(2 + 6 * i);

            blockUnits.add(new BlockShapeUnit(new Pos(boundXSize, boundYSize, boundZSize), new Pos(minX, minY, minZ)));
        }

        return new BlockShape(blockUnits);
    }

    public boolean relativeCollision(Collidable blockShape, Point point) {
        return false;
    }
}
