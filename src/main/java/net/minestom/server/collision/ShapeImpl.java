package net.minestom.server.collision;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ShapeImpl implements Shape {
    private static final Pattern PATTERN = Pattern.compile("\\d.\\d{1,3}", Pattern.MULTILINE);
    private final BoundingBox[] blockSections;
    private final Point relativeStart, relativeEnd;

    private final Registry.BlockEntry blockEntry;
    private Block block;

    private ShapeImpl(BoundingBox[] boundingBoxes, Registry.BlockEntry blockEntry) {
        this.blockSections = boundingBoxes;
        this.blockEntry = blockEntry;
        // Find bounds
        {
            double minX = 1, minY = 1, minZ = 1;
            double maxX = 0, maxY = 0, maxZ = 0;
            for (BoundingBox blockSection : blockSections) {
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
    }

    static ShapeImpl parseBlockFromRegistry(String str, Registry.BlockEntry blockEntry) {
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
        return new ShapeImpl(boundingBoxes, blockEntry);
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
    public boolean intersectBox(@NotNull Point position, @NotNull BoundingBox boundingBox) {
        for (BoundingBox blockSection : blockSections) {
            if (boundingBox.intersectBox(position, blockSection)) return true;
        }
        return false;
    }

    @Override
    public boolean intersectBoxSwept(@NotNull Point rayStart, @NotNull Point rayDirection,
                                     @NotNull Point shapePos, @NotNull BoundingBox moving, @NotNull SweepResult finalResult) {
        boolean hitBlock = false;
        for (BoundingBox blockSection : blockSections) {
            // Update final result if the temp result collision is sooner than the current final result
            if (RayUtils.BoundingBoxIntersectionCheck(moving, rayStart, rayDirection, blockSection, shapePos, finalResult)) {
                finalResult.collidedShapePosition = shapePos;
                finalResult.collidedShape = this;
                finalResult.blockType = block();
            }
            hitBlock = true;
        }
        return hitBlock;
    }

    private Block block() {
        Block block = this.block;
        if (block == null) this.block = block = Block.fromStateId((short) blockEntry.stateId());
        return block;
    }
}
