package net.minestom.server.instance.light;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static net.minestom.server.instance.light.LightCompute.SECTION_SIZE;
import static net.minestom.server.instance.light.LightCompute.getLight;

public interface Light {
    static Light sky(@NotNull Palette blockPalette) {
        return new SkyLight(blockPalette);
    }

    static Light block(@NotNull Palette blockPalette) {
        return new BlockLight(blockPalette);
    }

    boolean requiresSend();

    @ApiStatus.Internal
    byte[] array();

    Set<Point> flip();

    @ApiStatus.Internal
    Light calculateExternal(Instance instance, Chunk chunk, int sectionY);

    int getLevel(int x, int y, int z);

    @ApiStatus.Internal
    Light calculateInternal(Instance instance, int chunkX, int chunkY, int chunkZ);

    void invalidate();

    boolean requiresUpdate();

    void set(byte[] copyArray);

    @ApiStatus.Internal
    static Point[] getNeighbors(Chunk chunk, int sectionY) {
        int chunkX = chunk.getChunkX();
        int chunkZ = chunk.getChunkZ();

        Point[] links = new Vec[BlockFace.values().length];

        for (BlockFace face : BlockFace.values()) {
            Direction direction = face.toDirection();
            int x = chunkX + direction.normalX();
            int z = chunkZ + direction.normalZ();
            int y = sectionY + direction.normalY();

            Chunk foundChunk = chunk.getInstance().getChunk(x, z);

            if (foundChunk == null) continue;
            if (y - foundChunk.getMinSection() > foundChunk.getMaxSection() || y - foundChunk.getMinSection() < 0) continue;

            links[face.ordinal()] = new Vec(foundChunk.getChunkX(), y, foundChunk.getChunkZ());
        }

        return links;
    }

    @ApiStatus.Internal
    static boolean compareBorders(byte[] content, byte[] contentPropagation, byte[] contentPropagationTemp, BlockFace face) {
        if (content == null && contentPropagation == null && contentPropagationTemp == null) return true;

        final int k = switch (face) {
            case WEST, BOTTOM, NORTH -> 0;
            case EAST, TOP, SOUTH -> 15;
        };

        for (int bx = 0; bx < SECTION_SIZE; bx++) {
            for (int by = 0; by < SECTION_SIZE; by++) {
                final int posFrom = switch (face) {
                    case NORTH, SOUTH -> bx | (k << 4) | (by << 8);
                    case WEST, EAST -> k | (by << 4) | (bx << 8);
                    default -> bx | (by << 4) | (k << 8);
                };

                int valueFrom;

                if (content == null && contentPropagation == null) valueFrom = 0;
                else if (content != null && contentPropagation == null) valueFrom = getLight(content, posFrom);
                else if (content == null && contentPropagation != null) valueFrom = getLight(contentPropagation, posFrom);
                else valueFrom = Math.max(getLight(content, posFrom), getLight(contentPropagation, posFrom));

                int valueTo = getLight(contentPropagationTemp, posFrom);

                if (valueFrom < valueTo) return false;
            }
        }
        return true;
    }
}
