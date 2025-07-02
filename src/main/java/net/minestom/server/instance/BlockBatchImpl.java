package net.minestom.server.instance;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import static net.minestom.server.coordinate.CoordConversion.*;

record BlockBatchImpl(
        long flags,
        Long2ObjectMap<SectionState> sectionStates
) implements BlockBatch {
    @Override
    public @UnknownNullability Block getBlock(int x, int y, int z, @NotNull Condition condition) {
        final long sectionIndex = sectionIndexGlobal(x, y, z);
        final SectionState sectionState = sectionStates.get(sectionIndex);
        if (sectionState == null) return Block.AIR;
        final int localX = globalToSectionRelative(x);
        final int localY = globalToSectionRelative(y);
        final int localZ = globalToSectionRelative(z);
        if (condition != Condition.TYPE && !ignoreData()) {
            final int sectionBlockIndex = sectionBlockIndex(localX, localY, localZ);
            Block block = sectionState.blockStates.get(sectionBlockIndex);
            if (block != null) return block;
        }
        // Stateless block
        final Palette palette = sectionState.palette;
        int index = palette.get(localX, localY, localZ);
        if (!aligned() && --index < 0) return Block.AIR;
        return Block.fromStateId(index);
    }

    @Override
    public void getAll(@NotNull EntryConsumer consumer) {
        for (Long2ObjectMap.Entry<SectionState> entry : sectionStates.long2ObjectEntrySet()) {
            final long sectionIndex = entry.getLongKey();
            final SectionState sectionState = entry.getValue();
            final Palette palette = sectionState.palette;
            final int sectionX = sectionIndexGetX(sectionIndex);
            final int sectionY = sectionIndexGetY(sectionIndex);
            final int sectionZ = sectionIndexGetZ(sectionIndex);
            if (aligned()) {
                // Direct palette copies
                palette.getAll((x, y, z, value) -> {
                    final int globalX = sectionX * 16 + x;
                    final int globalY = sectionY * 16 + y;
                    final int globalZ = sectionZ * 16 + z;
                    if (!ignoreData()) {
                        final int sectionBlockIndex = sectionBlockIndex(x, y, z);
                        Block block = sectionState.blockStates.get(sectionBlockIndex);
                        if (block != null) {
                            consumer.accept(globalX, globalY, globalZ, block);
                            return;
                        }
                    }
                    final Block block = Block.fromStateId(value);
                    assert block != null;
                    consumer.accept(globalX, globalY, globalZ, block);
                });
            } else {
                // Palette values are +1
                palette.getAllPresent((x, y, z, value) -> {
                    final int globalX = sectionX * 16 + x;
                    final int globalY = sectionY * 16 + y;
                    final int globalZ = sectionZ * 16 + z;
                    if (!ignoreData()) {
                        final int sectionBlockIndex = sectionBlockIndex(x, y, z);
                        Block block = sectionState.blockStates.get(sectionBlockIndex);
                        if (block != null) {
                            consumer.accept(globalX, globalY, globalZ, block);
                            return;
                        }
                    }
                    final Block block = Block.fromStateId(value - 1);
                    assert block != null;
                    consumer.accept(globalX, globalY, globalZ, block);
                });
            }
        }
    }

    @Override
    public int count() {
        if (aligned()) {
            return sectionStates.size() * SECTION_BLOCK_COUNT;
        } else {
            int count = 0;
            for (SectionState sectionState : sectionStates.values()) {
                count += sectionState.palette.count();
            }
            return count;
        }
    }

    final static class BuilderImpl implements Builder {
        private final long flags;
        private final Long2ObjectMap<SectionState> sectionStates = new Long2ObjectOpenHashMap<>();

        BuilderImpl(long flags) {
            this.flags = flags;
        }

        SectionState sectionState(long sectionIndex) {
            return sectionStates.computeIfAbsent(
                    sectionIndex,
                    k -> new SectionState(Palette.blocks(), ignoreData() ? null : new Int2ObjectOpenHashMap<>())
            );
        }

        private boolean ignoreData() {
            return (flags & BlockBatch.IGNORE_DATA_FLAG) != 0;
        }

        private boolean aligned() {
            return (flags & BlockBatch.ALIGNED_FLAG) != 0;
        }

        @Override
        public void setBlock(int x, int y, int z, @NotNull Block block) {
            final long sectionIndex = sectionIndexGlobal(x, y, z);
            SectionState sectionState = sectionState(sectionIndex);
            final Palette palette = sectionState.palette;
            final int localX = globalToSectionRelative(x);
            final int localY = globalToSectionRelative(y);
            final int localZ = globalToSectionRelative(z);
            int index = block.stateId();
            if (!aligned()) index++;
            palette.set(localX, localY, localZ, index);

            if (!ignoreData()) {
                final CompoundBinaryTag compound = block.nbt();
                final BlockHandler handler = block.handler();
                final int sectionBlockIndex = sectionBlockIndex(localX, localY, localZ);
                if (compound != null || handler != null) {
                    sectionState.blockStates.put(sectionBlockIndex, block);
                } else {
                    // Necessary overhead to support overwriting blocks
                    sectionState.blockStates.remove(sectionBlockIndex);
                }
            }
        }

        @Override
        public void copyPalette(int sectionX, int sectionY, int sectionZ, @NotNull Palette palette) {
            final long sectionIndex = sectionIndex(sectionX, sectionY, sectionZ);
            if (!aligned()) {
                // Copy the palette with +1 for each value
                Palette adjustedPalette = Palette.blocks();
                palette.getAll((x, y, z, value) -> adjustedPalette.set(x, y, z, value + 1));
                palette = adjustedPalette;
            } else {
                palette = palette.clone();
            }
            sectionStates.put(sectionIndex, new SectionState(palette, ignoreData() ? null : new Int2ObjectOpenHashMap<>()));
        }

        BlockBatchImpl build() {
            return new BlockBatchImpl(flags, sectionStates);
        }
    }

    record SectionState(Palette palette, Int2ObjectMap<Block> blockStates) {
    }
}
