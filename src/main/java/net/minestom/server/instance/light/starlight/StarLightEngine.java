package net.minestom.server.instance.light.starlight;

import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortCollection;
import it.unimi.dsi.fastutil.shorts.ShortIterator;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.ChunkStatus;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public abstract class StarLightEngine {

    protected static final Block AIR_BLOCK_STATE = Block.AIR;

    protected static final AxisDirection[] DIRECTIONS = AxisDirection.values();
    protected static final AxisDirection[] AXIS_DIRECTIONS = DIRECTIONS;
    protected static final AxisDirection[] ONLY_HORIZONTAL_DIRECTIONS = new AxisDirection[] {
            AxisDirection.POSITIVE_X, AxisDirection.NEGATIVE_X,
            AxisDirection.POSITIVE_Z, AxisDirection.NEGATIVE_Z
    };

    protected enum AxisDirection {

        // Declaration order is important and relied upon. Do not change without modifying propagation code.
        POSITIVE_X(1, 0, 0), NEGATIVE_X(-1, 0, 0),
        POSITIVE_Z(0, 0, 1), NEGATIVE_Z(0, 0, -1),
        POSITIVE_Y(0, 1, 0), NEGATIVE_Y(0, -1, 0);

        static {
            POSITIVE_X.opposite = NEGATIVE_X; NEGATIVE_X.opposite = POSITIVE_X;
            POSITIVE_Z.opposite = NEGATIVE_Z; NEGATIVE_Z.opposite = POSITIVE_Z;
            POSITIVE_Y.opposite = NEGATIVE_Y; NEGATIVE_Y.opposite = POSITIVE_Y;
        }

        public AxisDirection opposite;

        public final int x;
        public final int y;
        public final int z;
        public final long everythingButThisDirection;
        public final long everythingButTheOppositeDirection;

        AxisDirection(final int x, final int y, final int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.everythingButThisDirection = (long)(ALL_DIRECTIONS_BITSET ^ (1 << this.ordinal()));
            // positive is always even, negative is always odd. Flip the 1 bit to get the negative direction.
            this.everythingButTheOppositeDirection = (long)(ALL_DIRECTIONS_BITSET ^ (1 << (this.ordinal() ^ 1)));
        }

        public AxisDirection getOpposite() {
            return this.opposite;
        }
    }

    // I'd like to thank https://www.seedofandromeda.com/blogs/29-fast-flood-fill-lighting-in-a-blocky-voxel-game-pt-1
    // for explaining how light propagates via breadth-first search

    // While the above is a good start to understanding the general idea of what the general principles are, it's not
    // exactly how the vanilla light engine should behave for minecraft.

    // similar to the above, except the chunk section indices vary from [-1, 1], or [0, 2]
    // for the y chunk section it's from [minLightSection, maxLightSection] or [0, maxLightSection - minLightSection]
    // index = x + (z * 5) + (y * 25)
    // null index indicates the chunk section doesn't exist (empty or out of bounds)
    protected final Section[] sectionCache;

    // the exact same as above, except for storing fast access to SWMRNibbleArray
    // for the y chunk section it's from [minLightSection, maxLightSection] or [0, maxLightSection - minLightSection]
    // index = x + (z * 5) + (y * 25)
    protected final SWMRNibbleArray[] nibbleCache;

    // always initialsed during start of lighting.
    // index = x + (z * 5)
    protected final Chunk[] chunkCache = new Chunk[5 * 5];

    // index = x + (z * 5)
    protected final boolean[][] emptinessMapCache = new boolean[5 * 5][];

//    protected final BlockPos.MutableBlockPos mutablePos1 = new BlockPos.MutableBlockPos();
//    protected final BlockPos.MutableBlockPos mutablePos2 = new BlockPos.MutableBlockPos();
//    protected final BlockPos.MutableBlockPos mutablePos3 = new BlockPos.MutableBlockPos();

    protected int encodeOffsetX;
    protected int encodeOffsetY;
    protected int encodeOffsetZ;

    protected int coordinateOffset;

    protected int chunkOffsetX;
    protected int chunkOffsetY;
    protected int chunkOffsetZ;

    protected int chunkIndexOffset;
    protected int chunkSectionIndexOffset;

    protected final boolean skylightPropagator;
    protected final int emittedLightMask;

    protected final Instance world;
    protected final int minLightSection;
    protected final int maxLightSection;
    protected final int minSection;
    protected final int maxSection;

    protected StarLightEngine(final boolean skylightPropagator, final Instance world) {
        this.skylightPropagator = skylightPropagator;
        this.emittedLightMask = skylightPropagator ? 0 : 0xF;
        this.world = world;
        this.minLightSection = LightWorldUtil.getMinLightSection(world);
        this.maxLightSection = LightWorldUtil.getMaxLightSection(world);
        this.minSection = LightWorldUtil.getMinSection(world);
        this.maxSection = LightWorldUtil.getMaxSection(world);

        this.sectionCache = new Section[5 * 5 * ((this.maxLightSection - this.minLightSection + 1) + 2)]; // add two extra sections for buffer
        this.nibbleCache = new SWMRNibbleArray[5 * 5 * ((this.maxLightSection - this.minLightSection + 1) + 2)]; // add two extra sections for buffer
    }

    protected final void setupEncodeOffset(final int centerX, final int centerZ) {
        // 31 = center + encodeOffset
        this.encodeOffsetX = 31 - centerX;
        this.encodeOffsetY = (-(this.minLightSection - 1) << 4); // we want 0 to be the smallest encoded value
        this.encodeOffsetZ = 31 - centerZ;

        // coordinateIndex = x | (z << 6) | (y << 12)
        this.coordinateOffset = this.encodeOffsetX + (this.encodeOffsetZ << 6) + (this.encodeOffsetY << 12);

        // 2 = (centerX >> 4) + chunkOffset
        this.chunkOffsetX = 2 - (centerX >> 4);
        this.chunkOffsetY = -(this.minLightSection - 1); // lowest should be 0
        this.chunkOffsetZ = 2 - (centerZ >> 4);

        // chunk index = x + (5 * z)
        this.chunkIndexOffset = this.chunkOffsetX + (5 * this.chunkOffsetZ);

        // chunk section index = x + (5 * z) + ((5*5) * y)
        this.chunkSectionIndexOffset = this.chunkIndexOffset + ((5 * 5) * this.chunkOffsetY);
    }

    protected final void setupCaches(final Instance chunkProvider, final int centerX, final int centerZ,
                                     final boolean relaxed, final boolean tryToLoadChunksFor2Radius) {
        final int centerChunkX = centerX >> 4;
        final int centerChunkZ = centerZ >> 4;

        this.setupEncodeOffset(centerChunkX * 16 + 7, centerChunkZ * 16 + 7);

        final int radius = tryToLoadChunksFor2Radius ? 2 : 1;

        for (int dz = -radius; dz <= radius; ++dz) {
            for (int dx = -radius; dx <= radius; ++dx) {
                final int cx = centerChunkX + dx;
                final int cz = centerChunkZ + dz;
                final boolean isTwoRadius = Math.max(IntegerUtil.branchlessAbs(dx), IntegerUtil.branchlessAbs(dz)) == 2;
                final Chunk chunk = chunkProvider.getChunk(cx, cz, ChunkStatus.LIGHTING);

                if (chunk == null) {
                    if (relaxed | isTwoRadius) {
                        continue;
                    }
                    throw new IllegalArgumentException("Trying to propagate light update before 1 radius neighbours ready");
                }

                if (!this.canUseChunk(chunk)) {
                    continue;
                }

                this.setChunkInCache(cx, cz, chunk);
                this.setEmptinessMapCache(cx, cz, this.getEmptinessMap(chunk));
                if (!isTwoRadius) {
                    this.setBlocksForChunkInCache(cx, cz, chunk.getSections());
                    this.setNibblesForChunkInCache(cx, cz, this.getNibblesOnChunk(chunk));
                }
            }
        }
    }

    protected final Chunk getChunkInCache(final int chunkX, final int chunkZ) {
        return this.chunkCache[chunkX + 5*chunkZ + this.chunkIndexOffset];
    }

    protected final void setChunkInCache(final int chunkX, final int chunkZ, final Chunk chunk) {
        this.chunkCache[chunkX + 5*chunkZ + this.chunkIndexOffset] = chunk;
    }

    protected final Section getChunkSection(final int chunkX, final int chunkY, final int chunkZ) {
        return this.sectionCache[chunkX + 5*chunkZ + (5 * 5) * chunkY + this.chunkSectionIndexOffset];
    }

    protected final void setChunkSectionInCache(final int chunkX, final int chunkY, final int chunkZ, final Section section) {
        this.sectionCache[chunkX + 5*chunkZ + 5*5*chunkY + this.chunkSectionIndexOffset] = section;
    }

    protected final void setBlocksForChunkInCache(final int chunkX, final int chunkZ, final List<Section> sections) {
        for (int cy = this.minLightSection; cy <= this.maxLightSection; ++cy) {
            this.setChunkSectionInCache(chunkX, cy, chunkZ,
                    sections == null ? null : (cy >= this.minSection && cy <= this.maxSection ? sections.get(cy - this.minSection) : null));
        }
    }

    protected final SWMRNibbleArray getNibbleFromCache(final int chunkX, final int chunkY, final int chunkZ) {
        return this.nibbleCache[chunkX + 5*chunkZ + (5 * 5) * chunkY + this.chunkSectionIndexOffset];
    }

    protected final SWMRNibbleArray[] getNibblesForChunkFromCache(final int chunkX, final int chunkZ) {
        final SWMRNibbleArray[] ret = new SWMRNibbleArray[this.maxLightSection - this.minLightSection + 1];

        for (int cy = this.minLightSection; cy <= this.maxLightSection; ++cy) {
            ret[cy - this.minLightSection] = this.nibbleCache[chunkX + 5*chunkZ + (cy * (5 * 5)) + this.chunkSectionIndexOffset];
        }

        return ret;
    }

    protected final void setNibbleInCache(final int chunkX, final int chunkY, final int chunkZ, final SWMRNibbleArray nibble) {
        this.nibbleCache[chunkX + 5*chunkZ + (5 * 5) * chunkY + this.chunkSectionIndexOffset] = nibble;
    }

    protected final void setNibblesForChunkInCache(final int chunkX, final int chunkZ, final SWMRNibbleArray[] nibbles) {
        for (int cy = this.minLightSection; cy <= this.maxLightSection; ++cy) {
            this.setNibbleInCache(chunkX, cy, chunkZ, nibbles == null ? null : nibbles[cy - this.minLightSection]);
        }
    }

    protected final void updateVisible(final Instance lightAccess) {
        for (int index = 0, max = this.nibbleCache.length; index < max; ++index) {
            final SWMRNibbleArray nibble = this.nibbleCache[index];
            if (nibble == null || !nibble.isDirty() || !nibble.updateVisible()) {
                continue;
            }

            final int chunkX = (index % 5) - this.chunkOffsetX;
            final int chunkZ = ((index / 5) % 5) - this.chunkOffsetZ;
            final int ySections = this.maxSection - this.minSection + 1;
            final int chunkY = ((index / (5*5)) % (ySections + 2 + 2)) - this.chunkOffsetY;
            lightAccess.onLightUpdate(chunkX, chunkZ, chunkY, this.skylightPropagator);
        }
    }

    protected final void destroyCaches() {
        Arrays.fill(this.sectionCache, null);
        Arrays.fill(this.nibbleCache, null);
        Arrays.fill(this.chunkCache, null);
        Arrays.fill(this.emptinessMapCache, null);
    }

    protected final Block getBlockState(final int worldX, final int worldY, final int worldZ) {
        final Section section = this.sectionCache[(worldX >> 4) + 5 * (worldZ >> 4) + (5 * 5) * (worldY >> 4) + this.chunkSectionIndexOffset];

        if (section != null && !section.hasOnlyAir()) {
            final short blockStateId = (short) section.blockPalette().get(worldX & 15, worldY & 15, worldZ & 15);
            return Objects.requireNonNullElse(Block.fromStateId(blockStateId), AIR_BLOCK_STATE);
        }

        return AIR_BLOCK_STATE;
    }

    protected final Block getBlockState(final int sectionIndex, final int localIndex) {
        final Section section = this.sectionCache[sectionIndex];
        if (section != null && !section.hasOnlyAir()) {
            final int x = localIndex & 15;
            final int z = (localIndex >> 4) & 15;
            final int y = (localIndex >> 8) & 15;
            final short blockStateId = (short) section.blockPalette().get(x, y, z);
            return Objects.requireNonNullElse(Block.fromStateId(blockStateId), AIR_BLOCK_STATE);
        }

        return AIR_BLOCK_STATE;
    }

    protected final int getLightLevel(final int worldX, final int worldY, final int worldZ) {
        final SWMRNibbleArray nibble = this.nibbleCache[(worldX >> 4) + 5 * (worldZ >> 4) + (5 * 5) * (worldY >> 4) + this.chunkSectionIndexOffset];

        return nibble == null ? 0 : nibble.getUpdating((worldX & 15) | ((worldZ & 15) << 4) | ((worldY & 15) << 8));
    }

    protected final int getLightLevel(final int sectionIndex, final int localIndex) {
        final SWMRNibbleArray nibble = this.nibbleCache[sectionIndex];

        return nibble == null ? 0 : nibble.getUpdating(localIndex);
    }

    protected final void setLightLevel(final int worldX, final int worldY, final int worldZ, final int level) {
        final int sectionIndex = (worldX >> 4) + 5 * (worldZ >> 4) + (5 * 5) * (worldY >> 4) + this.chunkSectionIndexOffset;
        final SWMRNibbleArray nibble = this.nibbleCache[sectionIndex];

        if (nibble != null) {
            nibble.set((worldX & 15) | ((worldZ & 15) << 4) | ((worldY & 15) << 8), level);
        }
    }

    protected final void postLightUpdate(final int worldX, final int worldY, final int worldZ) {

    }

    protected final void setLightLevel(final int sectionIndex, final int localIndex, final int worldX, final int worldY, final int worldZ, final int level) {
        final SWMRNibbleArray nibble = this.nibbleCache[sectionIndex];

        if (nibble != null) {
            nibble.set(localIndex, level);
        }
    }

    protected final boolean[] getEmptinessMap(final int chunkX, final int chunkZ) {
        return this.emptinessMapCache[chunkX + 5*chunkZ + this.chunkIndexOffset];
    }

    protected final void setEmptinessMapCache(final int chunkX, final int chunkZ, final boolean[] emptinessMap) {
        this.emptinessMapCache[chunkX + 5*chunkZ + this.chunkIndexOffset] = emptinessMap;
    }

    public static SWMRNibbleArray[] getFilledEmptyLight(final Instance world) {
        return getFilledEmptyLight(LightWorldUtil.getTotalLightSections(world));
    }

    private static SWMRNibbleArray[] getFilledEmptyLight(final int totalLightSections) {
        final SWMRNibbleArray[] ret = new SWMRNibbleArray[totalLightSections];

        for (int i = 0, len = ret.length; i < len; ++i) {
            ret[i] = new SWMRNibbleArray(null, true);
        }

        return ret;
    }

    protected abstract boolean[] getEmptinessMap(final Chunk chunk);

    protected abstract void setEmptinessMap(final Chunk chunk, final boolean[] to);

    protected abstract SWMRNibbleArray[] getNibblesOnChunk(final Chunk chunk);

    protected abstract void setNibbles(final Chunk chunk, final SWMRNibbleArray[] to);

    protected abstract boolean canUseChunk(final Chunk chunk);

    public final void blocksChangedInChunk(final Instance lightAccess, final int chunkX, final int chunkZ,
                                           final IntSet positions, final Boolean[] changedSections) {
        this.setupCaches(lightAccess, chunkX * 16 + 7, chunkZ * 16 + 7, true, true);
        try {
            final Chunk chunk = this.getChunkInCache(chunkX, chunkZ);
            if (chunk == null) {
                return;
            }
            if (changedSections != null) {
                final boolean[] ret = this.handleEmptySectionChanges(lightAccess, chunk, changedSections, false);
                if (ret != null) {
                    this.setEmptinessMap(chunk, ret);
                }
            }
            if (!positions.isEmpty()) {
                this.propagateBlockChanges(lightAccess, chunk, positions);
            }
            this.updateVisible(lightAccess);
        } finally {
            this.destroyCaches();
        }
    }

    // subclasses should not initialise caches, as this will always be done by the super call
    // subclasses should not invoke updateVisible, as this will always be done by the super call
    protected abstract void propagateBlockChanges(final Instance lightAccess, final Chunk atChunk, final IntSet positions);

    protected abstract void checkBlock(final Instance lightAccess, final int worldX, final int worldY, final int worldZ);

    // if ret > expect, then the real value is at least ret (early returns if ret > expect, rather than calculating actual)
    // if ret == expect, then expect is the correct light value for pos
    // if ret < expect, then ret is the real light value
    protected abstract int calculateLightValue(final Instance lightAccess, final int worldX, final int worldY, final int worldZ,
                                               final int expect);

    protected final int[] chunkCheckDelayedUpdatesCenter = new int[16 * 16];
    protected final int[] chunkCheckDelayedUpdatesNeighbour = new int[16 * 16];

    protected void checkChunkEdge(final Instance lightAccess, final Chunk chunk,
                                  final int chunkX, final int chunkY, final int chunkZ) {
        final SWMRNibbleArray currNibble = this.getNibbleFromCache(chunkX, chunkY, chunkZ);
        if (currNibble == null) {
            return;
        }

        for (final AxisDirection direction : ONLY_HORIZONTAL_DIRECTIONS) {
            final int neighbourOffX = direction.x;
            final int neighbourOffZ = direction.z;

            final SWMRNibbleArray neighbourNibble = this.getNibbleFromCache(chunkX + neighbourOffX,
                    chunkY, chunkZ + neighbourOffZ);

            if (neighbourNibble == null) {
                continue;
            }

            if (!currNibble.isInitialisedUpdating() && !neighbourNibble.isInitialisedUpdating()) {
                // both are zero, nothing to check.
                continue;
            }

            // this chunk
            final int incX;
            final int incZ;
            final int startX;
            final int startZ;

            if (neighbourOffX != 0) {
                // x direction
                incX = 0;
                incZ = 1;

                if (direction.x < 0) {
                    // negative
                    startX = chunkX << 4;
                } else {
                    startX = chunkX << 4 | 15;
                }
                startZ = chunkZ << 4;
            } else {
                // z direction
                incX = 1;
                incZ = 0;

                if (neighbourOffZ < 0) {
                    // negative
                    startZ = chunkZ << 4;
                } else {
                    startZ = chunkZ << 4 | 15;
                }
                startX = chunkX << 4;
            }

            int centerDelayedChecks = 0;
            int neighbourDelayedChecks = 0;
            for (int currY = chunkY << 4, maxY = currY | 15; currY <= maxY; ++currY) {
                for (int i = 0, currX = startX, currZ = startZ; i < 16; ++i, currX += incX, currZ += incZ) {
                    final int neighbourX = currX + neighbourOffX;
                    final int neighbourZ = currZ + neighbourOffZ;

                    final int currentIndex = (currX & 15) |
                            ((currZ & 15)) << 4 |
                            ((currY & 15) << 8);
                    final int currentLevel = currNibble.getUpdating(currentIndex);

                    final int neighbourIndex =
                            (neighbourX & 15) |
                            ((neighbourZ & 15)) << 4 |
                            ((currY & 15) << 8);
                    final int neighbourLevel = neighbourNibble.getUpdating(neighbourIndex);

                    // the checks are delayed because the checkBlock method clobbers light values - which then
                    // affect later calculate light value operations. While they don't affect it in a behaviourly significant
                    // way, they do have a negative performance impact due to simply queueing more values

                    if (this.calculateLightValue(lightAccess, currX, currY, currZ, currentLevel) != currentLevel) {
                        this.chunkCheckDelayedUpdatesCenter[centerDelayedChecks++] = currentIndex;
                    }

                    if (this.calculateLightValue(lightAccess, neighbourX, currY, neighbourZ, neighbourLevel) != neighbourLevel) {
                        this.chunkCheckDelayedUpdatesNeighbour[neighbourDelayedChecks++] = neighbourIndex;
                    }
                }
            }

            final int currentChunkOffX = chunkX << 4;
            final int currentChunkOffZ = chunkZ << 4;
            final int neighbourChunkOffX = (chunkX + direction.x) << 4;
            final int neighbourChunkOffZ = (chunkZ + direction.z) << 4;
            final int chunkOffY = chunkY << 4;
            for (int i = 0, len = Math.max(centerDelayedChecks, neighbourDelayedChecks); i < len; ++i) {
                // try to queue neighbouring data together
                // index = x | (z << 4) | (y << 8)
                if (i < centerDelayedChecks) {
                    final int value = this.chunkCheckDelayedUpdatesCenter[i];
                    this.checkBlock(lightAccess, currentChunkOffX | (value & 15),
                            chunkOffY | (value >>> 8),
                            currentChunkOffZ | ((value >>> 4) & 0xF));
                }
                if (i < neighbourDelayedChecks) {
                    final int value = this.chunkCheckDelayedUpdatesNeighbour[i];
                    this.checkBlock(lightAccess, neighbourChunkOffX | (value & 15),
                            chunkOffY | (value >>> 8),
                            neighbourChunkOffZ | ((value >>> 4) & 0xF));
                }
            }
        }
    }

    protected void checkChunkEdges(final Instance lightAccess, final Chunk chunk, final ShortCollection sections) {
        final int chunkX = chunk.getChunkX();
        final int chunkZ = chunk.getChunkZ();

        for (final ShortIterator iterator = sections.iterator(); iterator.hasNext();) {
            this.checkChunkEdge(lightAccess, chunk, chunkX, iterator.nextShort(), chunkZ);
        }

        this.performLightDecrease(lightAccess);
    }

    // subclasses should not initialise caches, as this will always be done by the super call
    // subclasses should not invoke updateVisible, as this will always be done by the super call
    // verifies that light levels on this chunks edges are consistent with this chunk's neighbours
    // edges. if they are not, they are decreased (effectively performing the logic in checkBlock).
    // This does not resolve skylight source problems.
    protected void checkChunkEdges(final Instance lightAccess, final Chunk chunk, final int fromSection, final int toSection) {
        final int chunkX = chunk.getChunkX();
        final int chunkZ = chunk.getChunkZ();

        for (int currSectionY = toSection; currSectionY >= fromSection; --currSectionY) {
            this.checkChunkEdge(lightAccess, chunk, chunkX, currSectionY, chunkZ);
        }

        this.performLightDecrease(lightAccess);
    }

    // pulls light from neighbours, and adds them into the increase queue. does not actually propagate.
    protected final void propagateNeighbourLevels(final Instance lightAccess, final Chunk chunk, final int fromSection, final int toSection) {
        final int chunkX = chunk.getChunkX();
        final int chunkZ = chunk.getChunkZ();

        for (int currSectionY = toSection; currSectionY >= fromSection; --currSectionY) {
            final SWMRNibbleArray currNibble = this.getNibbleFromCache(chunkX, currSectionY, chunkZ);
            if (currNibble == null) {
                continue;
            }
            for (final AxisDirection direction : ONLY_HORIZONTAL_DIRECTIONS) {
                final int neighbourOffX = direction.x;
                final int neighbourOffZ = direction.z;

                final SWMRNibbleArray neighbourNibble = this.getNibbleFromCache(chunkX + neighbourOffX,
                        currSectionY, chunkZ + neighbourOffZ);

                if (neighbourNibble == null || !neighbourNibble.isInitialisedUpdating()) {
                    // can't pull from 0
                    continue;
                }

                // neighbour chunk
                final int incX;
                final int incZ;
                final int startX;
                final int startZ;

                if (neighbourOffX != 0) {
                    // x direction
                    incX = 0;
                    incZ = 1;

                    if (direction.x < 0) {
                        // negative
                        startX = (chunkX << 4) - 1;
                    } else {
                        startX = (chunkX << 4) + 16;
                    }
                    startZ = chunkZ << 4;
                } else {
                    // z direction
                    incX = 1;
                    incZ = 0;

                    if (neighbourOffZ < 0) {
                        // negative
                        startZ = (chunkZ << 4) - 1;
                    } else {
                        startZ = (chunkZ << 4) + 16;
                    }
                    startX = chunkX << 4;
                }

                final long propagateDirection = 1L << direction.getOpposite().ordinal(); // we only want to check in this direction towards this chunk
                final int encodeOffset = this.coordinateOffset;

                for (int currY = currSectionY << 4, maxY = currY | 15; currY <= maxY; ++currY) {
                    for (int i = 0, currX = startX, currZ = startZ; i < 16; ++i, currX += incX, currZ += incZ) {
                        final int level = neighbourNibble.getUpdating(
                                (currX & 15)
                                        | ((currZ & 15) << 4)
                                        | ((currY & 15) << 8)
                        );

                        if (level <= 1) {
                            // nothing to propagate
                            continue;
                        }

                        this.appendToIncreaseQueue(
                                ((currX + (currZ << 6) + (currY << (6 + 6)) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                                        | ((level & 0xFL) << (6 + 6 + 16))
                                        | (propagateDirection << (6 + 6 + 16 + 4))
                                        | FLAG_HAS_SIDED_TRANSPARENT_BLOCKS // don't know if the current block is transparent, must check.
                        );
                    }
                }
            }
        }
    }

    public static Boolean[] getEmptySectionsForChunk(final Chunk chunk) {
        final List<Section> sections = chunk.getSections();
        final Boolean[] ret = new Boolean[sections.size()];

        for (int i = 0; i < sections.size(); ++i) {
            final Section section = sections.get(i);
            if (section == null || section.hasOnlyAir()) {
                ret[i] = Boolean.TRUE;
            } else {
                ret[i] = Boolean.FALSE;
            }
        }

        return ret;
    }

    public final void forceHandleEmptySectionChanges(final Instance lightAccess, final Chunk chunk, final Boolean[] emptinessChanges) {
        final int chunkX = chunk.getChunkX();
        final int chunkZ = chunk.getChunkZ();
        this.setupCaches(lightAccess, chunkX * 16 + 7, chunkZ * 16 + 7, true, true);
        try {
            // force current chunk into cache
            this.setChunkInCache(chunkX, chunkZ, chunk);
            this.setBlocksForChunkInCache(chunkX, chunkZ, chunk.getSections());
            this.setNibblesForChunkInCache(chunkX, chunkZ, this.getNibblesOnChunk(chunk));
            this.setEmptinessMapCache(chunkX, chunkZ, this.getEmptinessMap(chunk));

            final boolean[] ret = this.handleEmptySectionChanges(lightAccess, chunk, emptinessChanges, false);
            if (ret != null) {
                this.setEmptinessMap(chunk, ret);
            }
            this.updateVisible(lightAccess);
        } finally {
            this.destroyCaches();
        }
    }

    public final void handleEmptySectionChanges(final Instance lightAccess, final int chunkX, final int chunkZ,
                                                final Boolean[] emptinessChanges) {
        this.setupCaches(lightAccess, chunkX * 16 + 7, chunkZ * 16 + 7, true, true);
        try {
            final Chunk chunk = this.getChunkInCache(chunkX, chunkZ);
            if (chunk == null) {
                return;
            }
            final boolean[] ret = this.handleEmptySectionChanges(lightAccess, chunk, emptinessChanges, false);
            if (ret != null) {
                this.setEmptinessMap(chunk, ret);
            }
            this.updateVisible(lightAccess);
        } finally {
            this.destroyCaches();
        }
    }

    protected abstract void initNibble(final int chunkX, final int chunkY, final int chunkZ, final boolean extrude, final boolean initRemovedNibbles);

    protected abstract void setNibbleNull(final int chunkX, final int chunkY, final int chunkZ);

    // subclasses should not initialise caches, as this will always be done by the super call
    // subclasses should not invoke updateVisible, as this will always be done by the super call
    // subclasses are guaranteed that this is always called before a changed block set
    // newChunk specifies whether the changes describe a "first load" of a chunk or changes to existing, already loaded chunks
    // rets non-null when the emptiness map changed and needs to be updated
    protected final boolean[] handleEmptySectionChanges(final Instance lightAccess, final Chunk chunk,
                                                        final Boolean[] emptinessChanges, final boolean unlit) {
        final int chunkX = chunk.getChunkX();
        final int chunkZ = chunk.getChunkZ();

        boolean[] chunkEmptinessMap = this.getEmptinessMap(chunkX, chunkZ);
        boolean[] ret = null;
        final boolean needsInit = unlit || chunkEmptinessMap == null;
        if (needsInit) {
            this.setEmptinessMapCache(chunkX, chunkZ, ret = chunkEmptinessMap = new boolean[LightWorldUtil.getTotalSections(world)]);
        }

        // update emptiness map
        for (int sectionIndex = (emptinessChanges.length - 1); sectionIndex >= 0; --sectionIndex) {
            Boolean valueBoxed = emptinessChanges[sectionIndex];
            if (valueBoxed == null) {
                if (!needsInit) {
                    continue;
                }
                final Section section = this.getChunkSection(chunkX, sectionIndex + this.minSection, chunkZ);
                emptinessChanges[sectionIndex] = valueBoxed = section == null || section.hasOnlyAir() ? Boolean.TRUE : Boolean.FALSE;
            }
            chunkEmptinessMap[sectionIndex] = valueBoxed;
        }

        // now init neighbour nibbles
        for (int sectionIndex = (emptinessChanges.length - 1); sectionIndex >= 0; --sectionIndex) {
            final Boolean valueBoxed = emptinessChanges[sectionIndex];
            final int sectionY = sectionIndex + this.minSection;
            if (valueBoxed == null) {
                continue;
            }

            final boolean empty = valueBoxed;

            if (empty) {
                continue;
            }

            for (int dz = -1; dz <= 1; ++dz) {
                for (int dx = -1; dx <= 1; ++dx) {
                    // if we're not empty, we also need to initialise nibbles
                    // note: if we're unlit, we absolutely do not want to extrude, as light data isn't set up
                    final boolean extrude = (dx | dz) != 0 || !unlit;
                    for (int dy = 1; dy >= -1; --dy) {
                        this.initNibble(dx + chunkX, dy + sectionY, dz + chunkZ, extrude, false);
                    }
                }
            }
        }

        // check for de-init and lazy-init
        // lazy init is when chunks are being lit, so at the time they weren't loaded when their neighbours were running
        // init checks.
        for (int dz = -1; dz <= 1; ++dz) {
            for (int dx = -1; dx <= 1; ++dx) {
                // does this neighbour have 1 radius loaded?
                boolean neighboursLoaded = true;
                neighbour_loaded_search:
                for (int dz2 = -1; dz2 <= 1; ++dz2) {
                    for (int dx2 = -1; dx2 <= 1; ++dx2) {
                        if (this.getEmptinessMap(dx + dx2 + chunkX, dz + dz2 + chunkZ) == null) {
                            neighboursLoaded = false;
                            break neighbour_loaded_search;
                        }
                    }
                }

                for (int sectionY = this.maxLightSection; sectionY >= this.minLightSection; --sectionY) {
                    // check neighbours to see if we need to de-init this one
                    boolean allEmpty = true;
                    neighbour_search:
                    for (int dy2 = -1; dy2 <= 1; ++dy2) {
                        final int y = sectionY + dy2;
                        if (y < this.minSection || y > this.maxSection) {
                            // empty
                            continue;
                        }
                        for (int dz2 = -1; dz2 <= 1; ++dz2) {
                            for (int dx2 = -1; dx2 <= 1; ++dx2) {
                                final boolean[] emptinessMap = this.getEmptinessMap(dx + dx2 + chunkX, dz + dz2 + chunkZ);
                                if (emptinessMap != null) {
                                    if (!emptinessMap[y - this.minSection]) {
                                        allEmpty = false;
                                        break neighbour_search;
                                    }
                                } else {
                                    final Section section = this.getChunkSection(dx + dx2 + chunkX, y, dz + dz2 + chunkZ);
                                    if (section != null && !section.hasOnlyAir()) {
                                        allEmpty = false;
                                        break neighbour_search;
                                    }
                                }
                            }
                        }
                    }

                    if (allEmpty & neighboursLoaded) {
                        // can only de-init when neighbours are loaded
                        // de-init is fine to delay, as de-init is just an optimisation - it's not required for lighting
                        // to be correct

                        // all were empty, so de-init
                        this.setNibbleNull(dx + chunkX, sectionY, dz + chunkZ);
                    } else if (!allEmpty) {
                        // must init
                        final boolean extrude = (dx | dz) != 0 || !unlit;
                        this.initNibble(dx + chunkX, sectionY, dz + chunkZ, extrude, false);
                    }
                }
            }
        }

        return ret;
    }

    public final void checkChunkEdges(final Instance lightAccess, final int chunkX, final int chunkZ) {
        this.setupCaches(lightAccess, chunkX * 16 + 7, chunkZ * 16 + 7, true, false);
        try {
            final Chunk chunk = this.getChunkInCache(chunkX, chunkZ);
            if (chunk == null) {
                return;
            }
            this.checkChunkEdges(lightAccess, chunk, this.minLightSection, this.maxLightSection);
            this.updateVisible(lightAccess);
        } finally {
            this.destroyCaches();
        }
    }

    public final void checkChunkEdges(final Instance lightAccess, final int chunkX, final int chunkZ, final ShortCollection sections) {
        this.setupCaches(lightAccess, chunkX * 16 + 7, chunkZ * 16 + 7, true, false);
        try {
            final Chunk chunk = this.getChunkInCache(chunkX, chunkZ);
            if (chunk == null) {
                return;
            }
            this.checkChunkEdges(lightAccess, chunk, sections);
            this.updateVisible(lightAccess);
        } finally {
            this.destroyCaches();
        }
    }

    // subclasses should not initialise caches, as this will always be done by the super call
    // subclasses should not invoke updateVisible, as this will always be done by the super call
    // needsEdgeChecks applies when possibly loading vanilla data, which means we need to validate the current
    // chunks light values with respect to neighbours
    // subclasses should note that the emptiness changes are propagated BEFORE this is called, so this function
    // does not need to detect empty chunks itself (and it should do no handling for them either!)
    protected abstract void lightChunk(final Instance lightAccess, final Chunk chunk, final boolean needsEdgeChecks);

    public final void light(final Instance lightAccess, final Chunk chunk, final Boolean[] emptySections) {
        final int chunkX = chunk.getChunkX();
        final int chunkZ = chunk.getChunkZ();
        this.setupCaches(lightAccess, chunkX * 16 + 7, chunkZ * 16 + 7, true, true);

        try {
            final SWMRNibbleArray[] nibbles = getFilledEmptyLight(this.maxLightSection - this.minLightSection + 1);
            // force current chunk into cache
            this.setChunkInCache(chunkX, chunkZ, chunk);
            this.setBlocksForChunkInCache(chunkX, chunkZ, chunk.getSections());
            this.setNibblesForChunkInCache(chunkX, chunkZ, nibbles);
            this.setEmptinessMapCache(chunkX, chunkZ, this.getEmptinessMap(chunk));

            final boolean[] ret = this.handleEmptySectionChanges(lightAccess, chunk, emptySections, true);
            if (ret != null) {
                this.setEmptinessMap(chunk, ret);
            }
            this.lightChunk(lightAccess, chunk, true);
            this.setNibbles(chunk, nibbles);
            this.updateVisible(lightAccess);
        } finally {
            this.destroyCaches();
        }
    }

    public final void relightChunks(final Instance lightAccess, final LongSet chunks,
                                    final Consumer<Long> chunkLightCallback, final IntConsumer onComplete) {
        // it's recommended for maximum performance that the set is ordered according to a BFS from the center of
        // the region of chunks to relight
        // it's required that tickets are added for each chunk to keep them loaded
        final Long2ObjectOpenHashMap<SWMRNibbleArray[]> nibblesByChunk = new Long2ObjectOpenHashMap<>();
        final Long2ObjectOpenHashMap<boolean[]> emptinessMapByChunk = new Long2ObjectOpenHashMap<>();

        final int[] neighbourLightOrder = new int[] {
                // d = 0
                0, 0,
                // d = 1
                -1, 0,
                0, -1,
                1, 0,
                0, 1,
                // d = 2
                -1, 1,
                1, 1,
                -1, -1,
                1, -1,
        };

        int lightCalls = 0;

        for (final long chunkPos : chunks) {
            final int chunkX = ChunkUtils.getChunkCoordX(chunkPos);
            final int chunkZ = ChunkUtils.getChunkCoordZ(chunkPos);
            final Chunk chunk = lightAccess.getChunk(chunkX, chunkZ, ChunkStatus.LIGHTING);
            if (chunk == null || !this.canUseChunk(chunk)) {
                throw new IllegalStateException();
            }

            for (int i = 0, len = neighbourLightOrder.length; i < len; i += 2) {
                final int dx = neighbourLightOrder[i];
                final int dz = neighbourLightOrder[i + 1];
                final int neighbourX = dx + chunkX;
                final int neighbourZ = dz + chunkZ;

                final Chunk neighbour = lightAccess.getChunk(neighbourX, neighbourZ, ChunkStatus.LIGHTING);
                if (neighbour == null || !this.canUseChunk(neighbour)) {
                    continue;
                }

                if (nibblesByChunk.get(ChunkUtils.getChunkIndex(neighbourX, neighbourZ)) != null) {
                    // lit already called for neighbour, no need to light it now
                    continue;
                }

                // light neighbour chunk
                this.setupEncodeOffset(neighbourX * 16 + 7, neighbourZ * 16 + 7);
                try {
                    // insert all neighbouring chunks for this neighbour that we have data for
                    for (int dz2 = -1; dz2 <= 1; ++dz2) {
                        for (int dx2 = -1; dx2 <= 1; ++dx2) {
                            final int neighbourX2 = neighbourX + dx2;
                            final int neighbourZ2 = neighbourZ + dz2;
                            final long key = ChunkUtils.getChunkIndex(neighbourX2, neighbourZ2);
                            final Chunk neighbour2 = lightAccess.getChunk(neighbourX2, neighbourZ2, ChunkStatus.LIGHTING);
                            if (neighbour2 == null || !this.canUseChunk(neighbour2)) {
                                continue;
                            }

                            final SWMRNibbleArray[] nibbles = nibblesByChunk.get(key);
                            if (nibbles == null) {
                                // we haven't lit this chunk
                                continue;
                            }

                            this.setChunkInCache(neighbourX2, neighbourZ2, neighbour2);
                            this.setBlocksForChunkInCache(neighbourX2, neighbourZ2, neighbour2.getSections());
                            this.setNibblesForChunkInCache(neighbourX2, neighbourZ2, nibbles);
                            this.setEmptinessMapCache(neighbourX2, neighbourZ2, emptinessMapByChunk.get(key));
                        }
                    }

                    final long key = ChunkUtils.getChunkIndex(neighbourX, neighbourZ);

                    // now insert the neighbour chunk and light it
                    final SWMRNibbleArray[] nibbles = getFilledEmptyLight(this.world);
                    nibblesByChunk.put(key, nibbles);

                    this.setChunkInCache(neighbourX, neighbourZ, neighbour);
                    this.setBlocksForChunkInCache(neighbourX, neighbourZ, neighbour.getSections());
                    this.setNibblesForChunkInCache(neighbourX, neighbourZ, nibbles);

                    final boolean[] neighbourEmptiness = this.handleEmptySectionChanges(lightAccess, neighbour, getEmptySectionsForChunk(neighbour), true);
                    emptinessMapByChunk.put(key, neighbourEmptiness);
                    if (chunks.contains(ChunkUtils.getChunkIndex(neighbourX, neighbourZ))) {
                        this.setEmptinessMap(neighbour, neighbourEmptiness);
                    }

                    this.lightChunk(lightAccess, neighbour, false);
                } finally {
                    this.destroyCaches();
                }
            }

            // done lighting all neighbours, so the chunk is now fully lit

            // make sure nibbles are fully updated before calling back
            final SWMRNibbleArray[] nibbles = nibblesByChunk.get(ChunkUtils.getChunkIndex(chunkX, chunkZ));
            for (final SWMRNibbleArray nibble : nibbles) {
                nibble.updateVisible();
            }

            this.setNibbles(chunk, nibbles);

            for (int y = this.minLightSection; y <= this.maxLightSection; ++y) {
                lightAccess.onLightUpdate(chunkX, chunkZ, y, this.skylightPropagator);
            }

            // now do callback
            if (chunkLightCallback != null) {
                chunkLightCallback.accept(chunkPos);
            }
            ++lightCalls;
        }

        if (onComplete != null) {
            onComplete.accept(lightCalls);
        }
    }

    // contains:
    // lower (6 + 6 + 16) = 28 bits: encoded coordinate position (x | (z << 6) | (y << (6 + 6))))
    // next 4 bits: propagated light level (0, 15]
    // next 6 bits: propagation direction bitset
    // next 24 bits: unused
    // last 3 bits: state flags
    // state flags:
    // whether the increase propagator needs to write the propagated level to the position, used to avoid cascading light
    // updates for block sources
    protected static final long FLAG_WRITE_LEVEL = Long.MIN_VALUE >>> 2;
    // whether the propagation needs to check if its current level is equal to the expected level
    // used only in increase propagation
    protected static final long FLAG_RECHECK_LEVEL = Long.MIN_VALUE >>> 1;
    // whether the propagation needs to consider if its block is conditionally transparent
    protected static final long FLAG_HAS_SIDED_TRANSPARENT_BLOCKS = Long.MIN_VALUE;

    protected long[] increaseQueue = new long[16 * 16 * 16];
    protected int increaseQueueInitialLength;
    protected long[] decreaseQueue = new long[16 * 16 * 16];
    protected int decreaseQueueInitialLength;

    protected final long[] resizeIncreaseQueue() {
        return this.increaseQueue = Arrays.copyOf(this.increaseQueue, this.increaseQueue.length * 2);
    }

    protected final long[] resizeDecreaseQueue() {
        return this.decreaseQueue = Arrays.copyOf(this.decreaseQueue, this.decreaseQueue.length * 2);
    }

    protected final void appendToIncreaseQueue(final long value) {
        final int idx = this.increaseQueueInitialLength++;
        long[] queue = this.increaseQueue;
        if (idx >= queue.length) {
            queue = this.resizeIncreaseQueue();
            queue[idx] = value;
        } else {
            queue[idx] = value;
        }
    }

    protected final void appendToDecreaseQueue(final long value) {
        final int idx = this.decreaseQueueInitialLength++;
        long[] queue = this.decreaseQueue;
        if (idx >= queue.length) {
            queue = this.resizeDecreaseQueue();
            queue[idx] = value;
        } else {
            queue[idx] = value;
        }
    }

    protected static final AxisDirection[][] OLD_CHECK_DIRECTIONS = new AxisDirection[1 << 6][];
    protected static final int ALL_DIRECTIONS_BITSET = (1 << 6) - 1;
    static {
        for (int i = 0; i < OLD_CHECK_DIRECTIONS.length; ++i) {
            final List<AxisDirection> directions = new ArrayList<>();
            for (int bitset = i, len = Integer.bitCount(i), index = 0; index < len; ++index, bitset ^= IntegerUtil.getTrailingBit(bitset)) {
                directions.add(AXIS_DIRECTIONS[IntegerUtil.trailingZeros(bitset)]);
            }
            OLD_CHECK_DIRECTIONS[i] = directions.toArray(new AxisDirection[0]);
        }
    }

    protected final void performLightIncrease(final Instance world) {
        long[] queue = this.increaseQueue;
        int queueReadIndex = 0;
        int queueLength = this.increaseQueueInitialLength;
        this.increaseQueueInitialLength = 0;
        final int decodeOffsetX = -this.encodeOffsetX;
        final int decodeOffsetY = -this.encodeOffsetY;
        final int decodeOffsetZ = -this.encodeOffsetZ;
        final int encodeOffset = this.coordinateOffset;
        final int sectionOffset = this.chunkSectionIndexOffset;

        while (queueReadIndex < queueLength) {
            final long queueValue = queue[queueReadIndex++];

            final int posX = ((int)queueValue & 63) + decodeOffsetX;
            final int posZ = (((int)queueValue >>> 6) & 63) + decodeOffsetZ;
            final int posY = (((int)queueValue >>> 12) & ((1 << 16) - 1)) + decodeOffsetY;
            final int propagatedLightLevel = (int)((queueValue >>> (6 + 6 + 16)) & 0xFL);
            final AxisDirection[] checkDirections = OLD_CHECK_DIRECTIONS[(int)((queueValue >>> (6 + 6 + 16 + 4)) & 63L)];

            if ((queueValue & FLAG_RECHECK_LEVEL) != 0L) {
                if (this.getLightLevel(posX, posY, posZ) != propagatedLightLevel) {
                    // not at the level we expect, so something changed.
                    continue;
                }
            } else if ((queueValue & FLAG_WRITE_LEVEL) != 0L) {
                // these are used to restore block sources after a propagation decrease
                this.setLightLevel(posX, posY, posZ, propagatedLightLevel);
            }

            if ((queueValue & FLAG_HAS_SIDED_TRANSPARENT_BLOCKS) == 0L) {
                // we don't need to worry about our state here.
                for (final AxisDirection propagate : checkDirections) {
                    final int offX = posX + propagate.x;
                    final int offY = posY + propagate.y;
                    final int offZ = posZ + propagate.z;

                    final int sectionIndex = (offX >> 4) + 5 * (offZ >> 4) + (5 * 5) * (offY >> 4) + sectionOffset;
                    final int localIndex = (offX & 15) | ((offZ & 15) << 4) | ((offY & 15) << 8);

                    final SWMRNibbleArray currentNibble = this.nibbleCache[sectionIndex];
                    final int currentLevel;
                    if (currentNibble == null || (currentLevel = currentNibble.getUpdating(localIndex)) >= (propagatedLightLevel - 1)) {
                        continue; // already at the level we want or unloaded
                    }

                    final Block blockState = this.getBlockState(sectionIndex, localIndex);
                    if (blockState == null) {
                        continue;
                    }
                    final int opacityCached = blockState.registry().opacity();
                    //noinspection StatementWithEmptyBody
                    if (opacityCached != -1) {
                        final int targetLevel = propagatedLightLevel - Math.max(1, opacityCached);
                        if (targetLevel > currentLevel) {
                            currentNibble.set(localIndex, targetLevel);
                            this.postLightUpdate(offX, offY, offZ);

                            if (targetLevel > 1) {
                                if (queueLength >= queue.length) {
                                    queue = this.resizeIncreaseQueue();
                                }
                                queue[queueLength++] =
                                        ((offX + ((long) offZ << 6) + ((long) offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                                                | ((targetLevel & 0xFL) << (6 + 6 + 16))
                                                | (propagate.everythingButTheOppositeDirection << (6 + 6 + 16 + 4));
                            }
                        }
                    } else {
//                        this.mutablePos1.set(offX, offY, offZ);
//                        long flags = 0;
//                        if (((ExtendedAbstractBlockState)blockState).isConditionallyFullOpaque()) {
//                            final VoxelShape cullingFace = blockState.getFaceOcclusionShape(world, this.mutablePos1, propagate.getOpposite().nms);
//
//                            if (Shapes.faceShapeOccludes(Shapes.empty(), cullingFace)) {
//                                continue;
//                            }
//                            flags |= FLAG_HAS_SIDED_TRANSPARENT_BLOCKS;
//                        }
//
//                        final int opacity = blockState.getLightBlock(world, this.mutablePos1);
//                        final int targetLevel = propagatedLightLevel - Math.max(1, opacity);
//                        if (targetLevel <= currentLevel) {
//                            continue;
//                        }
//
//                        currentNibble.set(localIndex, targetLevel);
//                        this.postLightUpdate(offX, offY, offZ);
//
//                        if (targetLevel > 1) {
//                            if (queueLength >= queue.length) {
//                                queue = this.resizeIncreaseQueue();
//                            }
//                            queue[queueLength++] =
//                                    ((offX + (offZ << 6) + (offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
//                                            | ((targetLevel & 0xFL) << (6 + 6 + 16))
//                                            | (propagate.everythingButTheOppositeDirection << (6 + 6 + 16 + 4))
//                                            | (flags);
//                        }
                    }
                }
            } else {
                // we actually need to worry about our state here
//                final Block fromBlock = this.getBlockState(posX, posY, posZ);
//                this.mutablePos2.set(posX, posY, posZ);
                for (final AxisDirection propagate : checkDirections) {
                    final int offX = posX + propagate.x;
                    final int offY = posY + propagate.y;
                    final int offZ = posZ + propagate.z;

//                    final VoxelShape fromShape = (((ExtendedAbstractBlockState)fromBlock).isConditionallyFullOpaque()) ? fromBlock.getFaceOcclusionShape(world, this.mutablePos2, propagate.nms) : Shapes.empty();
//
//                    if (fromShape != Shapes.empty() && Shapes.faceShapeOccludes(Shapes.empty(), fromShape)) {
//                        continue;
//                    }

                    final int sectionIndex = (offX >> 4) + 5 * (offZ >> 4) + (5 * 5) * (offY >> 4) + sectionOffset;
                    final int localIndex = (offX & 15) | ((offZ & 15) << 4) | ((offY & 15) << 8);

                    final SWMRNibbleArray currentNibble = this.nibbleCache[sectionIndex];
                    final int currentLevel;

                    if (currentNibble == null || (currentLevel = currentNibble.getUpdating(localIndex)) >= (propagatedLightLevel - 1)) {
                        continue; // already at the level we want
                    }

                    final Block blockState = this.getBlockState(sectionIndex, localIndex);
                    if (blockState == null) {
                        continue;
                    }
                    final int opacityCached = blockState.registry().opacity();
                    //noinspection StatementWithEmptyBody
                    if (opacityCached != -1) {
                        final int targetLevel = propagatedLightLevel - Math.max(1, opacityCached);
                        if (targetLevel > currentLevel) {
                            currentNibble.set(localIndex, targetLevel);
                            this.postLightUpdate(offX, offY, offZ);

                            if (targetLevel > 1) {
                                if (queueLength >= queue.length) {
                                    queue = this.resizeIncreaseQueue();
                                }
                                queue[queueLength++] =
                                        ((offX + ((long) offZ << 6) + ((long) offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                                                | ((targetLevel & 0xFL) << (6 + 6 + 16))
                                                | (propagate.everythingButTheOppositeDirection << (6 + 6 + 16 + 4));
                            }
                        }
                    } else {
//                        this.mutablePos1.set(offX, offY, offZ);
//                        long flags = 0;
//                        if (((ExtendedAbstractBlockState)blockState).isConditionallyFullOpaque()) {
//                            final VoxelShape cullingFace = blockState.getFaceOcclusionShape(world, this.mutablePos1, propagate.getOpposite().nms);
//
//                            if (Shapes.faceShapeOccludes(fromShape, cullingFace)) {
//                                continue;
//                            }
//                            flags |= FLAG_HAS_SIDED_TRANSPARENT_BLOCKS;
//                        }
//
//                        final int opacity = blockState.getLightBlock(world, this.mutablePos1);
//                        final int targetLevel = propagatedLightLevel - Math.max(1, opacity);
//                        if (targetLevel <= currentLevel) {
//                            continue;
//                        }
//
//                        currentNibble.set(localIndex, targetLevel);
//                        this.postLightUpdate(offX, offY, offZ);
//
//                        if (targetLevel > 1) {
//                            if (queueLength >= queue.length) {
//                                queue = this.resizeIncreaseQueue();
//                            }
//                            queue[queueLength++] =
//                                    ((offX + (offZ << 6) + (offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
//                                            | ((targetLevel & 0xFL) << (6 + 6 + 16))
//                                            | (propagate.everythingButTheOppositeDirection << (6 + 6 + 16 + 4))
//                                            | (flags);
//                        }
                    }
                }
            }
        }
    }

    protected final void performLightDecrease(final Instance world) {
        long[] queue = this.decreaseQueue;
        long[] increaseQueue = this.increaseQueue;
        int queueReadIndex = 0;
        int queueLength = this.decreaseQueueInitialLength;
        this.decreaseQueueInitialLength = 0;
        int increaseQueueLength = this.increaseQueueInitialLength;
        final int decodeOffsetX = -this.encodeOffsetX;
        final int decodeOffsetY = -this.encodeOffsetY;
        final int decodeOffsetZ = -this.encodeOffsetZ;
        final int encodeOffset = this.coordinateOffset;
        final int sectionOffset = this.chunkSectionIndexOffset;
        final int emittedMask = this.emittedLightMask;

        while (queueReadIndex < queueLength) {
            final long queueValue = queue[queueReadIndex++];

            final int posX = ((int)queueValue & 63) + decodeOffsetX;
            final int posZ = (((int)queueValue >>> 6) & 63) + decodeOffsetZ;
            final int posY = (((int)queueValue >>> 12) & ((1 << 16) - 1)) + decodeOffsetY;
            final int propagatedLightLevel = (int)((queueValue >>> (6 + 6 + 16)) & 0xF);
            final AxisDirection[] checkDirections = OLD_CHECK_DIRECTIONS[(int)((queueValue >>> (6 + 6 + 16 + 4)) & 63)];

            if ((queueValue & FLAG_HAS_SIDED_TRANSPARENT_BLOCKS) == 0L) {
                // we don't need to worry about our state here.
                for (final AxisDirection propagate : checkDirections) {
                    final int offX = posX + propagate.x;
                    final int offY = posY + propagate.y;
                    final int offZ = posZ + propagate.z;

                    final int sectionIndex = (offX >> 4) + 5 * (offZ >> 4) + (5 * 5) * (offY >> 4) + sectionOffset;
                    final int localIndex = (offX & 15) | ((offZ & 15) << 4) | ((offY & 15) << 8);

                    final SWMRNibbleArray currentNibble = this.nibbleCache[sectionIndex];
                    final int lightLevel;

                    if (currentNibble == null || (lightLevel = currentNibble.getUpdating(localIndex)) == 0) {
                        // already at lowest (or unloaded), nothing we can do
                        continue;
                    }

                    final Block blockState = this.getBlockState(sectionIndex, localIndex);
                    if (blockState == null) {
                        continue;
                    }
                    final int opacityCached = blockState.registry().opacity();
                    //noinspection StatementWithEmptyBody
                    if (opacityCached != -1) {
                        final int targetLevel = Math.max(0, propagatedLightLevel - Math.max(1, opacityCached));
                        if (lightLevel > targetLevel) {
                            // it looks like another source propagated here, so re-propagate it
                            if (increaseQueueLength >= increaseQueue.length) {
                                increaseQueue = this.resizeIncreaseQueue();
                            }
                            increaseQueue[increaseQueueLength++] =
                                    ((offX + ((long) offZ << 6) + ((long) offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                                            | ((lightLevel & 0xFL) << (6 + 6 + 16))
                                            | (((long)ALL_DIRECTIONS_BITSET) << (6 + 6 + 16 + 4))
                                            | FLAG_RECHECK_LEVEL;
                            continue;
                        }
                        final int emittedLight = blockState.registry().lightEmission() & emittedMask;
                        if (emittedLight != 0) {
                            // re-propagate source
                            // note: do not set recheck level, or else the propagation will fail
                            if (increaseQueueLength >= increaseQueue.length) {
                                increaseQueue = this.resizeIncreaseQueue();
                            }
                            increaseQueue[increaseQueueLength++] =
                                    ((offX + ((long) offZ << 6) + ((long) offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                                            | ((emittedLight & 0xFL) << (6 + 6 + 16))
                                            | (((long)ALL_DIRECTIONS_BITSET) << (6 + 6 + 16 + 4))
                                            | (blockState.registry().isConditionallyFullOpaque() ? (FLAG_WRITE_LEVEL | FLAG_HAS_SIDED_TRANSPARENT_BLOCKS) : FLAG_WRITE_LEVEL);
                        }

                        currentNibble.set(localIndex, 0);
                        this.postLightUpdate(offX, offY, offZ);

                        if (targetLevel > 0) { // we actually need to propagate 0 just in case we find a neighbour...
                            if (queueLength >= queue.length) {
                                queue = this.resizeDecreaseQueue();
                            }
                            queue[queueLength++] =
                                    ((offX + ((long) offZ << 6) + ((long) offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                                            | ((targetLevel & 0xFL) << (6 + 6 + 16))
                                            | ((propagate.everythingButTheOppositeDirection) << (6 + 6 + 16 + 4));
                        }
                    } else {
//                        this.mutablePos1.set(offX, offY, offZ);
//                        long flags = 0;
//                        if (((ExtendedAbstractBlockState)blockState).isConditionallyFullOpaque()) {
//                            final VoxelShape cullingFace = blockState.getFaceOcclusionShape(world, this.mutablePos1, propagate.getOpposite().nms);
//
//                            if (Shapes.faceShapeOccludes(Shapes.empty(), cullingFace)) {
//                                continue;
//                            }
//                            flags |= FLAG_HAS_SIDED_TRANSPARENT_BLOCKS;
//                        }
//
//                        final int opacity = blockState.getLightBlock(world, this.mutablePos1);
//                        final int targetLevel = Math.max(0, propagatedLightLevel - Math.max(1, opacity));
//                        if (lightLevel > targetLevel) {
//                            // it looks like another source propagated here, so re-propagate it
//                            if (increaseQueueLength >= increaseQueue.length) {
//                                increaseQueue = this.resizeIncreaseQueue();
//                            }
//                            increaseQueue[increaseQueueLength++] =
//                                    ((offX + (offZ << 6) + (offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
//                                            | ((lightLevel & 0xFL) << (6 + 6 + 16))
//                                            | (((long)ALL_DIRECTIONS_BITSET) << (6 + 6 + 16 + 4))
//                                            | (FLAG_RECHECK_LEVEL | flags);
//                            continue;
//                        }
//                        final int emittedLight = blockState.getLightEmission() & emittedMask;
//                        if (emittedLight != 0) {
//                            // re-propagate source
//                            // note: do not set recheck level, or else the propagation will fail
//                            if (increaseQueueLength >= increaseQueue.length) {
//                                increaseQueue = this.resizeIncreaseQueue();
//                            }
//                            increaseQueue[increaseQueueLength++] =
//                                    ((offX + (offZ << 6) + (offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
//                                            | ((emittedLight & 0xFL) << (6 + 6 + 16))
//                                            | (((long)ALL_DIRECTIONS_BITSET) << (6 + 6 + 16 + 4))
//                                            | (flags | FLAG_WRITE_LEVEL);
//                        }
//
//                        currentNibble.set(localIndex, 0);
//                        this.postLightUpdate(offX, offY, offZ);
//
//                        if (targetLevel > 0) {
//                            if (queueLength >= queue.length) {
//                                queue = this.resizeDecreaseQueue();
//                            }
//                            queue[queueLength++] =
//                                    ((offX + (offZ << 6) + (offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
//                                            | ((targetLevel & 0xFL) << (6 + 6 + 16))
//                                            | ((propagate.everythingButTheOppositeDirection) << (6 + 6 + 16 + 4))
//                                            | flags;
//                        }
                    }
                }
            } else {
                // we actually need to worry about our state here
//                final Block fromBlock = this.getBlockState(posX, posY, posZ);
//                this.mutablePos2.set(posX, posY, posZ);
                for (final AxisDirection propagate : checkDirections) {
                    final int offX = posX + propagate.x;
                    final int offY = posY + propagate.y;
                    final int offZ = posZ + propagate.z;

                    final int sectionIndex = (offX >> 4) + 5 * (offZ >> 4) + (5 * 5) * (offY >> 4) + sectionOffset;
                    final int localIndex = (offX & 15) | ((offZ & 15) << 4) | ((offY & 15) << 8);

//                    final VoxelShape fromShape = (((ExtendedAbstractBlockState)fromBlock).isConditionallyFullOpaque()) ? fromBlock.getFaceOcclusionShape(world, this.mutablePos2, propagate.nms) : Shapes.empty();
//
//                    if (fromShape != Shapes.empty() && Shapes.faceShapeOccludes(Shapes.empty(), fromShape)) {
//                        continue;
//                    }

                    final SWMRNibbleArray currentNibble = this.nibbleCache[sectionIndex];
                    final int lightLevel;

                    if (currentNibble == null || (lightLevel = currentNibble.getUpdating(localIndex)) == 0) {
                        // already at lowest (or unloaded), nothing we can do
                        continue;
                    }

                    final Block blockState = this.getBlockState(sectionIndex, localIndex);
                    if (blockState == null) {
                        continue;
                    }
                    final int opacityCached = blockState.registry().opacity();
                    //noinspection StatementWithEmptyBody
                    if (opacityCached != -1) {
                        final int targetLevel = Math.max(0, propagatedLightLevel - Math.max(1, opacityCached));
                        if (lightLevel > targetLevel) {
                            // it looks like another source propagated here, so re-propagate it
                            if (increaseQueueLength >= increaseQueue.length) {
                                increaseQueue = this.resizeIncreaseQueue();
                            }
                            increaseQueue[increaseQueueLength++] =
                                    ((offX + ((long) offZ << 6) + ((long) offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                                            | ((lightLevel & 0xFL) << (6 + 6 + 16))
                                            | (((long)ALL_DIRECTIONS_BITSET) << (6 + 6 + 16 + 4))
                                            | FLAG_RECHECK_LEVEL;
                            continue;
                        }
                        final int emittedLight = blockState.registry().lightEmission() & emittedMask;
                        if (emittedLight != 0) {
                            // re-propagate source
                            // note: do not set recheck level, or else the propagation will fail
                            if (increaseQueueLength >= increaseQueue.length) {
                                increaseQueue = this.resizeIncreaseQueue();
                            }
                            increaseQueue[increaseQueueLength++] =
                                    ((offX + ((long) offZ << 6) + ((long) offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                                            | ((emittedLight & 0xFL) << (6 + 6 + 16))
                                            | (((long)ALL_DIRECTIONS_BITSET) << (6 + 6 + 16 + 4))
                                            | (blockState.registry().isConditionallyFullOpaque() ? (FLAG_WRITE_LEVEL | FLAG_HAS_SIDED_TRANSPARENT_BLOCKS) : FLAG_WRITE_LEVEL);
                        }

                        currentNibble.set(localIndex, 0);
                        this.postLightUpdate(offX, offY, offZ);

                        if (targetLevel > 0) { // we actually need to propagate 0 just in case we find a neighbour...
                            if (queueLength >= queue.length) {
                                queue = this.resizeDecreaseQueue();
                            }
                            queue[queueLength++] =
                                    ((offX + ((long) offZ << 6) + ((long) offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                                            | ((targetLevel & 0xFL) << (6 + 6 + 16))
                                            | ((propagate.everythingButTheOppositeDirection) << (6 + 6 + 16 + 4));
                        }
                    } else {
//                        this.mutablePos1.set(offX, offY, offZ);
//                        long flags = 0;
//                        if (((ExtendedAbstractBlockState)blockState).isConditionallyFullOpaque()) {
//                            final VoxelShape cullingFace = blockState.getFaceOcclusionShape(world, this.mutablePos1, propagate.getOpposite().nms);
//
//                            if (Shapes.faceShapeOccludes(fromShape, cullingFace)) {
//                                continue;
//                            }
//                            flags |= FLAG_HAS_SIDED_TRANSPARENT_BLOCKS;
//                        }
//
//                        final int opacity = blockState.getLightBlock(world, this.mutablePos1);
//                        final int targetLevel = Math.max(0, propagatedLightLevel - Math.max(1, opacity));
//                        if (lightLevel > targetLevel) {
//                            // it looks like another source propagated here, so re-propagate it
//                            if (increaseQueueLength >= increaseQueue.length) {
//                                increaseQueue = this.resizeIncreaseQueue();
//                            }
//                            increaseQueue[increaseQueueLength++] =
//                                    ((offX + (offZ << 6) + (offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
//                                            | ((lightLevel & 0xFL) << (6 + 6 + 16))
//                                            | (((long)ALL_DIRECTIONS_BITSET) << (6 + 6 + 16 + 4))
//                                            | (FLAG_RECHECK_LEVEL | flags);
//                            continue;
//                        }
//                        final int emittedLight = blockState.getLightEmission() & emittedMask;
//                        if (emittedLight != 0) {
//                            // re-propagate source
//                            // note: do not set recheck level, or else the propagation will fail
//                            if (increaseQueueLength >= increaseQueue.length) {
//                                increaseQueue = this.resizeIncreaseQueue();
//                            }
//                            increaseQueue[increaseQueueLength++] =
//                                    ((offX + (offZ << 6) + (offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
//                                            | ((emittedLight & 0xFL) << (6 + 6 + 16))
//                                            | (((long)ALL_DIRECTIONS_BITSET) << (6 + 6 + 16 + 4))
//                                            | (flags | FLAG_WRITE_LEVEL);
//                        }
//
//                        currentNibble.set(localIndex, 0);
//                        this.postLightUpdate(offX, offY, offZ);
//
//                        if (targetLevel > 0) { // we actually need to propagate 0 just in case we find a neighbour...
//                            if (queueLength >= queue.length) {
//                                queue = this.resizeDecreaseQueue();
//                            }
//                            queue[queueLength++] =
//                                    ((offX + (offZ << 6) + (offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
//                                            | ((targetLevel & 0xFL) << (6 + 6 + 16))
//                                            | ((propagate.everythingButTheOppositeDirection) << (6 + 6 + 16 + 4))
//                                            | flags;
//                        }
                    }
                }
            }
        }

        // propagate sources we clobbered
        this.increaseQueueInitialLength = increaseQueueLength;
        this.performLightIncrease(world);
    }
}
