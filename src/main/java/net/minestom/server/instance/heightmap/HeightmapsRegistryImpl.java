package net.minestom.server.instance.heightmap;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.MathUtils;
import org.jglrxavpok.hephaistos.collections.ImmutableLongArray;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import static net.minestom.server.instance.Chunk.CHUNK_SIZE_X;
import static net.minestom.server.instance.Chunk.CHUNK_SIZE_Z;

public class HeightmapsRegistryImpl implements HeightmapsRegistry {
    private final Heightmap[] heightmaps;
    private final BitSet skipRefresh = new BitSet();
    private boolean skipRefreshCompletely = false;

    private final Chunk attachedChunk;
    private final Instance instance;

    public HeightmapsRegistryImpl(Chunk attachedChunk) {
        this.attachedChunk = attachedChunk;
        this.instance = attachedChunk.getInstance();

        heightmaps = new Heightmap[] {
                new MotionBlockingHeightmap(attachedChunk),
                new WorldSurfaceHeightmap(attachedChunk),
        };
    }

    private void calculateInitialIfNeeded() {
        if (skipRefreshCompletely) return;

        int startY = Heightmap.getStartY(attachedChunk);

        for (int i = 0; i < heightmaps.length; i++) {
            if (skipRefresh.get(i)) continue;
            final Heightmap current = heightmaps[i];

            for (int x = 0; x < CHUNK_SIZE_X; x++) {
                for (int z = 0; z < CHUNK_SIZE_Z; z++) {
                    current.refresh(x, z, startY);
                }
            }
            skipRefresh.set(i);
        }

        skipRefreshCompletely = true;
    }

    @Override
    public void loadFromNBT(NBTCompound heightmapsNBT) {
        for (Heightmap heightmap : heightmaps) {
            if (!heightmapsNBT.contains(heightmap.NBTName())) continue;

            var currentHeights = heightmapsNBT.getLongArray(heightmap.NBTName());

            final int dimensionHeight = instance.getDimensionType().getHeight();
            final int bitsForHeight = MathUtils.bitsToRepresent(dimensionHeight);

            heightmap.loadFrom(currentHeights, bitsForHeight);
        }
    }

    @Override
    public void refreshAt(int x, int y, int z, Block block) {
        calculateInitialIfNeeded();
        for (Heightmap heightmap : heightmaps) {
            heightmap.refresh(x, y, z, block);
        }
    }

    @Override
    public Heightmap getMotionBlocking() {
        return heightmaps[0];
    }

    @Override
    public Heightmap getWorldSurface() {
        return heightmaps[1];
    }

    @Override
    public NBTCompound getNBT() {
        calculateInitialIfNeeded();
        final int dimensionHeight = instance.getDimensionType().getHeight();
        final int bitsForHeight = MathUtils.bitsToRepresent(dimensionHeight);

        Map<String, NBT> heightmapsNBTs = new HashMap<>(heightmaps.length);
        for (Heightmap heightmap : heightmaps) {
            heightmapsNBTs.put(heightmap.NBTName(), heightmap.getNBT(bitsForHeight));
        }
        return NBT.Compound(heightmapsNBTs);
    }
}
