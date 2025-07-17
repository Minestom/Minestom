package net.minestom.server.instance;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.*;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        if (condition != Condition.TYPE && !ignoreData() && !sectionState.blockStates.isEmpty()) {
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
                    if (!ignoreData() && !sectionState.blockStates.isEmpty()) {
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
                    if (!ignoreData() && !sectionState.blockStates.isEmpty()) {
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

    @Override
    public @NotNull List<BlockBatch> split() {
        List<BlockBatch> result = new ArrayList<>();
        for (Long2ObjectMap.Entry<SectionState> entry : sectionStates.long2ObjectEntrySet()) {
            final SectionState sectionState = entry.getValue();
            if (sectionState.palette.count() == 0 && sectionState.blockStates.isEmpty()) continue;
            BlockBatchImpl batch = new BlockBatchImpl(flags,
                    Long2ObjectMaps.singleton(entry.getLongKey(), sectionState)
            );
            result.add(batch);
        }
        return List.copyOf(result);
    }

    @Override
    public @NotNull Generator asGenerator() {
        LongSet sectionIndices = new LongOpenHashSet(sectionStates.keySet());
        return unit -> {
            synchronized (sectionIndices) {
                if (sectionIndices.isEmpty()) return;
            }
            final Set<Vec> sections = unit.sections();
            for (Vec section : sections) {
                final int sectionX = section.blockX(), sectionY = section.blockY(), sectionZ = section.blockZ();
                final long sectionIndex = sectionIndex(sectionX, sectionY, sectionZ);
                synchronized (sectionIndices) {
                    if (!sectionIndices.remove(sectionIndex)) continue;
                }
                final SectionState sectionState = sectionStates.get(sectionIndex);
                if (sectionState == null) continue;
                final Palette palette = sectionState.palette;
                final Int2ObjectMap<Block> blockStates = sectionState.blockStates;
                if (palette.count() == 0 && blockStates.isEmpty()) continue;
                palette.getAllPresent((x, y, z, value) -> {
                    final int globalX = x + sectionX * 16;
                    final int globalY = y + sectionY * 16;
                    final int globalZ = z + sectionZ * 16;
                    if (!aligned() && --value < 0) return;
                    final Block block = Block.fromStateId(value);
                    assert block != null;
                    unit.modifier().setBlock(globalX, globalY, globalZ, block);
                });
                if (!ignoreData() && blockStates.isEmpty()) {
                    for (Int2ObjectMap.Entry<Block> entry : blockStates.int2ObjectEntrySet()) {
                        final int sectionBlockIndex = entry.getIntKey();
                        final Block block = entry.getValue();
                        final int localX = sectionBlockIndexGetX(sectionBlockIndex);
                        final int localY = sectionBlockIndexGetY(sectionBlockIndex);
                        final int localZ = sectionBlockIndexGetZ(sectionBlockIndex);
                        final int globalX = localX + sectionX * 16;
                        final int globalY = localY + sectionY * 16;
                        final int globalZ = localZ + sectionZ * 16;
                        unit.modifier().setBlock(globalX, globalY, globalZ, block);
                    }
                }
            }
        };
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
            palette = palette.clone();
            if (!aligned()) palette.offset(1);
            sectionStates.put(sectionIndex, new SectionState(palette, ignoreData() ? null : new Int2ObjectOpenHashMap<>()));
        }

        BlockBatchImpl build() {
            return new BlockBatchImpl(flags, sectionStates);
        }
    }

    record SectionState(Palette palette, Int2ObjectMap<Block> blockStates) {
    }
}
