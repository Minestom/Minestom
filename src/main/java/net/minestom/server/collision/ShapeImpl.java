package net.minestom.server.collision;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ShapeImpl implements Shape {
    private static final Pattern PATTERN = Pattern.compile("\\d.\\d+", Pattern.MULTILINE);
    private final BoundingBox[] collisionBoundingBoxes;
    private final Point relativeStart, relativeEnd;
    private final byte fullFaces;

    private final BoundingBox[] occlusionBoundingBoxes;
    private final byte blockOcclusion;
    private final byte airOcclusion;

    private final Registry.BlockEntry blockEntry;
    private Block block;

    private ShapeImpl(BoundingBox[] boundingBoxes, BoundingBox[] occlusionBoundingBoxes, Registry.BlockEntry blockEntry) {
        this.collisionBoundingBoxes = boundingBoxes;
        this.occlusionBoundingBoxes = occlusionBoundingBoxes;
        this.blockEntry = blockEntry;

        // Find bounds of collision
        if (collisionBoundingBoxes.length > 0) {
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
            this.relativeStart = new Vec(minX, minY, minZ);
            this.relativeEnd = new Vec(maxX, maxY, maxZ);
        } else {
            this.relativeStart = Vec.ZERO;
            this.relativeEnd = Vec.ZERO;
        }

        byte fullCollisionFaces = 0;
        for (BlockFace f : BlockFace.values()) {
            final byte res = isFaceCovered(computeOcclusionSet(f, collisionBoundingBoxes));
            fullCollisionFaces |= ((res == 2) ? 0b1 : 0b0) << (byte) f.ordinal();
        }
        this.fullFaces = fullCollisionFaces;

        byte airFaces = 0;
        byte fullFaces = 0;
        for (BlockFace f : BlockFace.values()) {
            final byte res = isFaceCovered(computeOcclusionSet(f, occlusionBoundingBoxes));
            airFaces |= ((res == 0) ? 0b1 : 0b0) << (byte) f.ordinal();
            fullFaces |= ((res == 2) ? 0b1 : 0b0) << (byte) f.ordinal();
        }

        this.airOcclusion = airFaces;
        this.blockOcclusion = fullFaces;
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

            final BoundingBox bb = new BoundingBox(boundXSize, boundYSize, boundZSize, new Vec(minX, minY, minZ));
            assert bb.minX() == minX;
            assert bb.minY() == minY;
            assert bb.minZ() == minZ;
            boundingBoxes[i] = bb;
        }
        return boundingBoxes;
    }

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

    static ShapeImpl parseBlockFromRegistry(String collision, String occlusion, Registry.BlockEntry blockEntry) {
        BoundingBox[] collisionBoundingBoxes = parseRegistryBoundingBoxString(collision);
        BoundingBox[] occlusionBoundingBoxes = blockEntry.occludes() ? parseRegistryBoundingBoxString(occlusion) : new BoundingBox[0];
        return new ShapeImpl(collisionBoundingBoxes, occlusionBoundingBoxes, blockEntry);
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
    public boolean isOccluded(@NotNull Shape shape, @NotNull BlockFace face) {
        final ShapeImpl shapeImpl = ((ShapeImpl) shape);
        final boolean hasBlockOcclusion = (((blockOcclusion >> face.ordinal()) & 1) == 1);
        final boolean hasBlockOcclusionOther = ((shapeImpl.blockOcclusion >> face.getOppositeFace().ordinal()) & 1) == 1;

        if (blockEntry.lightEmission() > 0) return hasBlockOcclusionOther;

        // If either face is full, return true
        if (hasBlockOcclusion || hasBlockOcclusionOther) return true;

        final boolean hasAirOcclusion = (((airOcclusion >> face.ordinal()) & 1) == 1);
        final boolean hasAirOcclusionOther = ((shapeImpl.airOcclusion >> face.getOppositeFace().ordinal()) & 1) == 1;

        // If a single face is air, return false
        if (hasAirOcclusion || hasAirOcclusionOther) return false;

        // Comparing two partial faces. Computation needed
        List<Rectangle> allRectangles = computeOcclusionSet(face.getOppositeFace(), shapeImpl.occlusionBoundingBoxes);
        allRectangles.addAll(computeOcclusionSet(face, occlusionBoundingBoxes));
        return isFaceCovered(allRectangles) == 2;
    }

    @Override
    public boolean isFaceFull(@NotNull BlockFace face) {
        return (((fullFaces >> face.ordinal()) & 1) == 1);
    }

    @Override
    public boolean intersectBox(@NotNull Point position, @NotNull BoundingBox boundingBox) {
        for (BoundingBox blockSection : collisionBoundingBoxes) {
            if (boundingBox.intersectBox(position, blockSection)) return true;
        }
        return false;
    }

    @Override
    public boolean intersectBoxSwept(@NotNull Point rayStart, @NotNull Point rayDirection,
                                     @NotNull Point shapePos, @NotNull BoundingBox moving, @NotNull SweepResult finalResult) {
        boolean hitBlock = false;
        for (BoundingBox blockSection : collisionBoundingBoxes) {
            // Update final result if the temp result collision is sooner than the current final result
            if (RayUtils.BoundingBoxIntersectionCheck(moving, rayStart, rayDirection, blockSection, shapePos, finalResult)) {
                finalResult.collidedPositionX = rayStart.x() + rayDirection.x() * finalResult.res;
                finalResult.collidedPositionY = rayStart.y() + rayDirection.y() * finalResult.res;
                finalResult.collidedPositionZ = rayStart.z() + rayDirection.z() * finalResult.res;

                finalResult.collidedShape = this;
                hitBlock = true;
            }
        }
        return hitBlock;
    }

    public Block block() {
        Block block = this.block;
        if (block == null) this.block = block = Block.fromStateId((short) blockEntry.stateId());
        return block;
    }

    private static @NotNull List<Rectangle> computeOcclusionSet(BlockFace face, BoundingBox[] boundingBoxes) {
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

    private record Rectangle(double x1, double y1, double x2, double y2) { }
}
