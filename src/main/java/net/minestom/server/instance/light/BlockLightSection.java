package net.minestom.server.instance.light;

import it.unimi.dsi.fastutil.shorts.ShortArrayFIFOQueue;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.light.LightSection.InternalBlockLight;
import net.minestom.server.instance.light.LightSection.LightData;
import net.minestom.server.instance.light.LightSection.LightUpdateResult;
import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.coordinate.CoordConversion.SECTION_BLOCK_COUNT;

final class BlockLightSection {
    static final BlockFace[] FACES = BlockFace.values();
    private final LightSection section;

    public BlockLightSection(LightSection section) {
        this.section = section;
    }

    LightUpdateResult<byte[]> relightBlockLightExternal() {
        var version = section.getNextBlockLightExternalVersion();
        // We must always query all our data after acquiring the version ID.
        // This ensures all data is at least as recent as the acquired version.
        var neighborSnapshot = section.chunk.createNeighborSnapshot();
        var posXC = neighborSnapshot.east();
        var posZC = neighborSnapshot.south();
        var negXC = neighborSnapshot.west();
        var negZC = neighborSnapshot.north();
        var posX = posXC == null ? null : posXC.getLightSection(section.sectionY());
        var negX = negXC == null ? null : negXC.getLightSection(section.sectionY());
        var posZ = posZC == null ? null : posZC.getLightSection(section.sectionY());
        var negZ = negZC == null ? null : negZC.getLightSection(section.sectionY());
        var posY = section.up;
        var negY = section.down;
        var selfLight = section.getBlockLightInternal().data();
        var content = selfLight.data();
        var blockPalette = selfLight.palette();
        var neighbors = new @Nullable LightSection[]{
                negY, posY, negZ, posZ, negX, posX // Order must be same as order in BlockFace enum
        };
        var lightSources = new ShortArrayFIFOQueue(0);
        for (var i = 0; i < neighbors.length; i++) {
            var neighbor = neighbors[i];
            if (neighbor == null) continue;

            // Light+palette can be out of sync, but that is okay.
            var otherLight = neighbor.getBlockLight().data();
            var otherPalette = neighbor.getBlockLightInternal().data().palette();

            final BlockFace face = FACES[i];
            final int k = switch (face) {
                case WEST, BOTTOM, NORTH -> 0;
                case EAST, TOP, SOUTH -> 15;
            };
            for (int bx = 0; bx < 16; bx++) {
                for (int by = 0; by < 16; by++) {

                    final byte lightEmission = (byte) Math.max(switch (face) {
                        case NORTH, SOUTH -> (byte) getLight(otherLight, bx, by, 15 - k);
                        case WEST, EAST -> (byte) getLight(otherLight, 15 - k, bx, by);
                        default -> (byte) getLight(otherLight, bx, 15 - k, by);
                    } - 1, 0);
                    if (lightEmission <= 0) continue;

                    final int posTo = switch (face) {
                        case NORTH, SOUTH -> bx | (k << 4) | (by << 8);
                        case WEST, EAST -> k | (by << 4) | (bx << 8);
                        default -> bx | (by << 4) | (k << 8);
                    };

                    if (content != LightCompute.EMPTY_CONTENT) {
                        final int internalEmission = (byte) (Math.max(getLight(content, posTo) - 1, 0));
                        if (lightEmission <= internalEmission) continue;
                    }

                    final @Nullable Block blockTo = switch (face) {
                        case NORTH, SOUTH -> getBlock(blockPalette, bx, by, k);
                        case WEST, EAST -> getBlock(blockPalette, k, bx, by);
                        default -> getBlock(blockPalette, bx, k, by);
                    };

                    final @Nullable Block blockFrom = switch (face) {
                        case NORTH, SOUTH -> getBlock(otherPalette, bx, by, 15 - k);
                        case WEST, EAST -> getBlock(otherPalette, 15 - k, bx, by);
                        default -> getBlock(otherPalette, bx, 15 - k, by);
                    };

                    if (blockTo == null && blockFrom != null) {
                        if (blockFrom.registry().occlusionShape().isOccluded(Block.AIR.registry().occlusionShape(), face.getOppositeFace()))
                            continue;
                    } else if (blockTo != null && blockFrom == null) {
                        if (Block.AIR.registry().occlusionShape().isOccluded(blockTo.registry().occlusionShape(), face))
                            continue;
                    } else if (blockTo != null && blockFrom != null) {
                        if (blockFrom.registry().occlusionShape().isOccluded(blockTo.registry().occlusionShape(), face.getOppositeFace()))
                            continue;
                    }
                    if (lightEmission >= 14){
                        System.out.println(face);
                        System.out.println(blockFrom);
                        System.out.println(blockTo);
                    }

                    final int index = posTo | (lightEmission << 12);
                    lightSources.enqueue((short) index);
                }
            }
        }

        var externalLight = LightCompute.compute(blockPalette, lightSources);

        var newData = new LightData<>(externalLight, version);

        return section.updateBlockLightExternal(newData);
    }

    static int getLight(byte[] light, int x, int y, int z) {
        return getLight(light, x | (z << 4) | (y << 8));
    }

    static int getLight(byte[] light, int index) {
        if (index >>> 1 >= light.length) return 0;
        final int value = light[index >>> 1];
        return ((value >>> ((index & 1) << 2)) & 0xF);
    }

    public static @Nullable Block getBlock(Palette palette, int x, int y, int z) {
        return Block.fromStateId(palette.get(x, y, z));
    }

    private LightData<InternalBlockLight> prepareBlockLightInternal(int version) {
        try {
            // We do not have internal sources, we are a bridge section
            if (section.chunkSection == null)
                return new LightData<>(new InternalBlockLight(LightCompute.EMPTY_CONTENT, Palette.blocks()), version);
            Palette blockPalette;
            synchronized (section.chunk) {
                // We need to clone the palette, because palettes are not thread-safe
                // We clone for a read-only copy, that is tread-safe.
                // This assumes reading from a palette does no internal modification (which could be unsafe)
                blockPalette = section.chunkSection.blockPalette().clone();
            }
            var internalLightSources = getBlockLightInternalSources(blockPalette);
            var array = LightCompute.compute(blockPalette, internalLightSources);
            return new LightData<>(new InternalBlockLight(array, blockPalette), version);
        } catch (Throwable t) {
            MinecraftServer.getExceptionManager().handleException(t);
            return new LightData<>(new InternalBlockLight(LightCompute.EMPTY_CONTENT, Palette.blocks()), version);
        }
    }

    LightUpdateResult<InternalBlockLight> relightBlockLightInternal() {
        var version = section.getNextBlockLightInternalVersion();
        var lightData = prepareBlockLightInternal(version);
        return section.updateBlockLightInternal(lightData);
    }

    /**
     * Collect all the block light sources from the given palette into a FIFO queue.
     */
    private static ShortArrayFIFOQueue getBlockLightInternalSources(Palette blockPalette) {
        if (blockPalette.isEmpty()) return new ShortArrayFIFOQueue(0); // Avoid state id lookup for air

        int singleValue = blockPalette.singleValue();
        if (singleValue != -1) {
            Block block = Block.fromStateId(singleValue);
            assert block != null;
            int lightEmission = block.registry().lightEmission();
            if (lightEmission <= 0) return new ShortArrayFIFOQueue(0);
            ShortArrayFIFOQueue lightSources = new ShortArrayFIFOQueue(SECTION_BLOCK_COUNT);
            final int prefix = lightEmission << 12;
            for (int index = 0; index < SECTION_BLOCK_COUNT; index++) {
                lightSources.enqueue((short) (index | prefix));
            }
            return lightSources;
        } else {
            ShortArrayFIFOQueue lightSources = new ShortArrayFIFOQueue();
            // Apply section light
            blockPalette.getAllPresent((x, y, z, stateId) -> {
                final Block block = Block.fromStateId(stateId);
                assert block != null;
                final int lightEmission = block.registry().lightEmission();
                if (lightEmission <= 0) return;
                final int index = x | (z << 4) | (y << 8);
                lightSources.enqueue((short) (index | (lightEmission << 12)));
            });
            return lightSources;
        }
    }
}
