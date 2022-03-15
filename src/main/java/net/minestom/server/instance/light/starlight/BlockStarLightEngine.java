package net.minestom.server.instance.light.starlight;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.ChunkStatus;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.chunk.ChunkUtils;

import java.util.*;

public final class BlockStarLightEngine extends StarLightEngine {

    public BlockStarLightEngine(final Instance world) {
        super(false, world);
    }

    @Override
    protected boolean[] getEmptinessMap(final Chunk chunk) {
        return chunk.getLightData().getBlockEmptinessMap();
    }

    @Override
    protected void setEmptinessMap(final Chunk chunk, final boolean[] to) {
        chunk.getLightData().setBlockEmptinessMap(to);
    }

    @Override
    protected SWMRNibbleArray[] getNibblesOnChunk(final Chunk chunk) {
        return chunk.getLightData().getBlockNibbles();
    }

    @Override
    protected void setNibbles(final Chunk chunk, final SWMRNibbleArray[] to) {
        chunk.getLightData().setBlockNibbles(to);
    }

    @Override
    protected boolean canUseChunk(final Chunk chunk) {
        return chunk.getStatus().isOrAfter(ChunkStatus.LIGHTING) && chunk.getLightData().isLightCorrect();
    }

    @Override
    protected void setNibbleNull(final int chunkX, final int chunkY, final int chunkZ) {
        final SWMRNibbleArray nibble = this.getNibbleFromCache(chunkX, chunkY, chunkZ);
        if (nibble != null) {
            // de-initialisation is not as straightforward as with sky data, since deinit of block light is typically
            // because a block was removed - which can decrease light. with sky data, block breaking can only result
            // in increases, and thus the existing sky block check will actually correctly propagate light through
            // a null section. so in order to propagate decreases correctly, we can do a couple of things: not remove
            // the data section, or do edge checks on ALL axis (x, y, z). however I do not want edge checks running
            // for clients at all, as they are expensive. so we don't remove the section, but to maintain the appearence
            // of vanilla data management we "hide" them.
            nibble.setHidden();
        }
    }

    @Override
    protected void initNibble(final int chunkX, final int chunkY, final int chunkZ, final boolean extrude, final boolean initRemovedNibbles) {
        if (chunkY < this.minLightSection || chunkY > this.maxLightSection || this.getChunkInCache(chunkX, chunkZ) == null) {
            return;
        }

        final SWMRNibbleArray nibble = this.getNibbleFromCache(chunkX, chunkY, chunkZ);
        if (nibble == null) {
            if (!initRemovedNibbles) {
                throw new IllegalStateException();
            } else {
                this.setNibbleInCache(chunkX, chunkY, chunkZ, new SWMRNibbleArray());
            }
        } else {
            nibble.setNonNull();
        }
    }

    @Override
    protected final void checkBlock(final Instance lightAccess, final int worldX, final int worldY, final int worldZ) {
        // blocks can change opacity
        // blocks can change emitted light
        // blocks can change direction of propagation

        final int encodeOffset = this.coordinateOffset;
        final int emittedMask = this.emittedLightMask;

        final int currentLevel = this.getLightLevel(worldX, worldY, worldZ);
        final Block blockState = this.getBlockState(worldX, worldY, worldZ);
        final int emittedLevel = blockState.registry().lightEmission() & emittedMask;

        this.setLightLevel(worldX, worldY, worldZ, emittedLevel);
        // this accounts for change in emitted light that would cause an increase
        if (emittedLevel != 0) {
            this.appendToIncreaseQueue(
                    ((worldX + ((long) worldZ << 6) + ((long) worldY << (6 + 6)) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                            | (emittedLevel & 0xFL) << (6 + 6 + 16)
                            | (((long)ALL_DIRECTIONS_BITSET) << (6 + 6 + 16 + 4))
                            | (blockState.registry().isConditionallyFullOpaque() ? FLAG_HAS_SIDED_TRANSPARENT_BLOCKS : 0)
            );
        }
        // this also accounts for a change in emitted light that would cause a decrease
        // this also accounts for the change of direction of propagation (i.e old block was full transparent, new block is full opaque or vice versa)
        // as it checks all neighbours (even if current level is 0)
        this.appendToDecreaseQueue(
                ((worldX + ((long) worldZ << 6) + ((long) worldY << (6 + 6)) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                        | (currentLevel & 0xFL) << (6 + 6 + 16)
                        | (((long)ALL_DIRECTIONS_BITSET) << (6 + 6 + 16 + 4))
                        // always keep sided transparent false here, new block might be conditionally transparent which would
                        // prevent us from decreasing sources in the directions where the new block is opaque
                        // if it turns out we were wrong to de-propagate the source, the re-propagate logic WILL always
                        // catch that and fix it.
        );
        // re-propagating neighbours (done by the decrease queue) will also account for opacity changes in this block
    }

//    protected final BlockPos.MutableBlockPos recalcCenterPos = new BlockPos.MutableBlockPos();
//    protected final BlockPos.MutableBlockPos recalcNeighbourPos = new BlockPos.MutableBlockPos();

    @Override
    protected int calculateLightValue(final Instance lightAccess, final int worldX, final int worldY, final int worldZ,
                                      final int expect) {
        final Block centerState = this.getBlockState(worldX, worldY, worldZ);
        int level = centerState.registry().lightEmission() & 0xF;

        if (level >= (15 - 1) || level > expect) {
            return level;
        }

        final int sectionOffset = this.chunkSectionIndexOffset;
//        final Block conditionallyOpaqueState;
        int opacity = centerState.registry().opacity();

        if (opacity == -1) {
//            this.recalcCenterPos.set(worldX, worldY, worldZ);
//            opacity = centerState.getLightBlock(lightAccess.getLevel(), this.recalcCenterPos);
//            if (((ExtendedAbstractBlockState)centerState).isConditionallyFullOpaque()) {
//                conditionallyOpaqueState = centerState;
//            } else {
//                conditionallyOpaqueState = null;
//            }
            return level;
        } else if (opacity >= 15) {
            return level;
        } else {
//            conditionallyOpaqueState = null;
        }
        opacity = Math.max(1, opacity);

        for (final AxisDirection direction : AXIS_DIRECTIONS) {
            final int offX = worldX + direction.x;
            final int offY = worldY + direction.y;
            final int offZ = worldZ + direction.z;

            final int sectionIndex = (offX >> 4) + 5 * (offZ >> 4) + (5 * 5) * (offY >> 4) + sectionOffset;

            final int neighbourLevel = this.getLightLevel(sectionIndex, (offX & 15) | ((offZ & 15) << 4) | ((offY & 15) << 8));

            if ((neighbourLevel - 1) <= level) {
                // don't need to test transparency, we know it wont affect the result.
                continue;
            }

            final Block neighbourState = this.getBlockState(offX, offY, offZ);
            if (neighbourState.registry().isConditionallyFullOpaque()) {
//                // here the block can be conditionally opaque (i.e light cannot propagate from it), so we need to test that
//                // we don't read the blockstate because most of the time this is false, so using the faster
//                // known transparency lookup results in a net win
//                this.recalcNeighbourPos.set(offX, offY, offZ);
//                final VoxelShape neighbourFace = neighbourState.getFaceOcclusionShape(lightAccess.getLevel(), this.recalcNeighbourPos, direction.opposite.nms);
//                final VoxelShape thisFace = conditionallyOpaqueState == null ? Shapes.empty() : conditionallyOpaqueState.getFaceOcclusionShape(lightAccess.getLevel(), this.recalcCenterPos, direction.nms);
//                if (Shapes.faceShapeOccludes(thisFace, neighbourFace)) {
//                    // not allowed to propagate
//                    continue;
//                }
                continue;
            }

            // passed transparency,

            final int calculated = neighbourLevel - opacity;
            level = Math.max(calculated, level);
            if (level > expect) {
                return level;
            }
        }

        return level;
    }

    @Override
    protected void propagateBlockChanges(final Instance lightAccess, final Chunk atChunk, final IntSet positions) {
        final int startX = atChunk.getChunkX() << 4;
        final int startZ = atChunk.getChunkZ() << 4;
        for (final int pos : positions) {
            this.checkBlock(
                    lightAccess,
                    startX + ChunkUtils.blockIndexToChunkPositionX(pos),
                    ChunkUtils.blockIndexToChunkPositionY(pos),
                    startZ + ChunkUtils.blockIndexToChunkPositionZ(pos)
            );
        }

        this.performLightDecrease(lightAccess);
    }

    private IntIterator getSources(final Chunk chunk) {
        final IntList sources = new IntArrayList();

        final List<Section> sections = chunk.getSections();
        for (int sectionY = this.minSection; sectionY <= this.maxSection; ++sectionY) {
            final Section section = sections.get(sectionY - this.minSection);
            if (section == null || section.hasOnlyAir()) {
                // no sources in empty sections
                continue;
            }
            final Palette palette = section.blockPalette();
            final int offY = sectionY << 4;

            for (int x = 0; x < Chunk.CHUNK_SIZE_X; ++x) {
                for (int y = 0; y < Chunk.CHUNK_SECTION_SIZE; ++y) {
                    for (int z = 0; z < Chunk.CHUNK_SIZE_Z; ++z) {
                        final short blockStateId = (short) palette.get(x, y, z);
                        final Block state = Objects.requireNonNullElse(Block.fromStateId(blockStateId), StarLightEngine.AIR_BLOCK_STATE);
                        if (state.registry().lightEmission() <= 0) {
                            continue;
                        }
                        sources.add(ChunkUtils.getBlockIndex(x, offY | y, z));
                    }
                }
            }
        }

        return sources.iterator();
    }

    @Override
    public void lightChunk(final Instance lightAccess, final Chunk chunk, final boolean needsEdgeChecks) {
        // setup sources
        final int emittedMask = this.emittedLightMask;
        for (final IntIterator positions = this.getSources(chunk); positions.hasNext();) {
            final int pos = positions.nextInt();
            final int x = (chunk.getChunkX() << 4) | ChunkUtils.blockIndexToChunkPositionX(pos);
            final int y = ChunkUtils.blockIndexToChunkPositionY(pos);
            final int z = (chunk.getChunkZ() << 4) | ChunkUtils.blockIndexToChunkPositionZ(pos);
            final Block blockState = this.getBlockState(x, y, z);
            final int emittedLight = blockState.registry().lightEmission() & emittedMask;

            if (emittedLight <= this.getLightLevel(x, y, z)) {
                // some other source is brighter
                continue;
            }

            this.appendToIncreaseQueue(
                    ((x + ((long) z << 6) + ((long) y << (6 + 6)) + this.coordinateOffset) & ((1L << (6 + 6 + 16)) - 1))
                            | (emittedLight & 0xFL) << (6 + 6 + 16)
                            | (((long)ALL_DIRECTIONS_BITSET) << (6 + 6 + 16 + 4))
                            | (blockState.registry().isConditionallyFullOpaque() ? FLAG_HAS_SIDED_TRANSPARENT_BLOCKS : 0)
            );


            // propagation wont set this for us
            this.setLightLevel(x, y, z, emittedLight);
        }

        if (needsEdgeChecks) {
            // not required to propagate here, but this will reduce the hit of the edge checks
            this.performLightIncrease(lightAccess);

            // verify neighbour edges
            this.checkChunkEdges(lightAccess, chunk, this.minLightSection, this.maxLightSection);
        } else {
            this.propagateNeighbourLevels(lightAccess, chunk, this.minLightSection, this.maxLightSection);

            this.performLightIncrease(lightAccess);
        }
    }
}
