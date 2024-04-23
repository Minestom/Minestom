package net.minestom.server.instance.anvil;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implements a thread-safe reader and writer for Minecraft region files.
 *
 * @see <a href="https://minecraft.wiki/w/Region_file_format">Region file format</a>
 * @see <a href="https://github.com/Minestom/Hephaistos/blob/master/common/src/main/kotlin/org/jglrxavpok/hephaistos/mca/RegionFile.kt">Hephaistos implementation</a>
 */
final class RegionFile implements AutoCloseable {

    private static final int MAX_ENTRY_COUNT = 1024;
    private static final int SECTOR_SIZE = 4096;
    private static final int SECTOR_1MB = 1024 * 1024 / SECTOR_SIZE;
    private static final int HEADER_LENGTH = MAX_ENTRY_COUNT * 2 * 4; // 2 4-byte fields per entry

    private static final BinaryTagIO.Reader TAG_READER = BinaryTagIO.unlimitedReader();

    public static @NotNull String getFileName(int regionX, int regionZ) {
        return "r." + regionX + "." + regionZ + ".mca";
    }

    private final ReentrantLock lock = new ReentrantLock();
    private final RandomAccessFile file;

    private final int[] locations = new int[MAX_ENTRY_COUNT];
    private final int[] timestamps = new int[MAX_ENTRY_COUNT];
    private final BooleanList freeSectors = new BooleanArrayList(2);

    public RegionFile(@NotNull Path path, int regionX, int regionZ, @NotNull DimensionType dimensionType) throws IOException {
        this.file = new RandomAccessFile(path.toFile(), "rw");

        readHeader();
    }

    public boolean hasChunkData(int chunkX, int chunkZ) {
        lock.lock();
        try {
            return locations[getChunkIndex(chunkX, chunkZ)] != 0;
        } finally {
            lock.unlock();
        }
    }

    public @Nullable CompoundBinaryTag getChunk(int chunkX, int chunkZ) throws IOException {
        lock.lock();
        try {
            if (!hasChunkData(chunkX, chunkZ)) return null;

            int location = locations[getChunkIndex(chunkX, chunkZ)];
            file.seek((long) (location >> 8) * SECTOR_SIZE); // Move to start of first sector
            int length = file.readInt();
            int compressionType = file.readByte();
            BinaryTagIO.Compression compression = switch (compressionType) {
                case 1 -> BinaryTagIO.Compression.GZIP;
                case 2 -> BinaryTagIO.Compression.ZLIB;
                case 3 -> BinaryTagIO.Compression.NONE;
                default -> throw new IOException("Unsupported compression type: " + compressionType);
            };

            // Read the raw content
            byte[] data = new byte[length - 1];
            file.read(data);

            // Parse it as a compound tag
            return TAG_READER.read(new ByteArrayInputStream(data), compression);
        } finally {
            lock.unlock();
        }
    }


    @Override
    public void close() throws Exception {
        file.close();
    }

    private void readHeader() throws IOException {
        file.seek(0);
        if (file.length() < HEADER_LENGTH) {
            // new file, fill in data
            file.write(new byte[HEADER_LENGTH]);
        }

        //todo: addPadding()

        final long totalSectors = file.length() / SECTOR_SIZE;
        for (int i = 0; i < totalSectors; i++) freeSectors.add(true);

        // Read locations
        file.seek(0);
        for (int i = 0; i < MAX_ENTRY_COUNT; i++) {
            int location = locations[i] = file.readInt();
            int offset = location >> 8;
            int length = location & 0xFF;

            if (location != 0 && offset + length <= freeSectors.size()) {
                for (int sectorIndex = 0; sectorIndex < length; sectorIndex++) {
                    freeSectors.set(sectorIndex + offset, false);
                }
            }
        }

        // Read timestamps
        for (int i = 0; i < MAX_ENTRY_COUNT; i++) {
            timestamps[i] = file.readInt();
        }
    }

    private int getChunkIndex(int chunkX, int chunkZ) {
        return (ChunkUtils.toRegionLocal(chunkZ) << 5) | ChunkUtils.toRegionLocal(chunkX);
    }
}
