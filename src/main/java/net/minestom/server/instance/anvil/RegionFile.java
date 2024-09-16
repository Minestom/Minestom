package net.minestom.server.instance.anvil;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.Map;
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
    private static final int CHUNK_HEADER_LENGTH = 4 + 1; // Length + Compression type (todo non constant to support custom compression)

    private static final int COMPRESSION_ZLIB = 2;

    private static final BinaryTagIO.Reader TAG_READER = BinaryTagIO.unlimitedReader();
    private static final BinaryTagIO.Writer TAG_WRITER = BinaryTagIO.writer();

    public static @NotNull String getFileName(int regionX, int regionZ) {
        return "r." + regionX + "." + regionZ + ".mca";
    }

    private final ReentrantLock lock = new ReentrantLock();
    private final RandomAccessFile file;

    private final int[] locations = new int[MAX_ENTRY_COUNT];
    private final int[] timestamps = new int[MAX_ENTRY_COUNT];
    private final BooleanList freeSectors = new BooleanArrayList(2);

    public RegionFile(@NotNull Path path) throws IOException {
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

    public @Nullable CompoundBinaryTag readChunkData(int chunkX, int chunkZ) throws IOException {
        lock.lock();
        try {
            if (!hasChunkData(chunkX, chunkZ)) return null;

            int location = locations[getChunkIndex(chunkX, chunkZ)];
            file.seek((long) (location >> 8) * SECTOR_SIZE); // Move to start of first sector
            int length = file.readInt();
            int compressionType = file.readByte();
            BinaryTagIO.Compression compression = switch (compressionType) {
                case 1 -> BinaryTagIO.Compression.GZIP;
                case COMPRESSION_ZLIB -> BinaryTagIO.Compression.ZLIB;
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

    public void writeChunkData(int chunkX, int chunkZ, @NotNull CompoundBinaryTag data) throws IOException {
        // Write the data (compressed)
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TAG_WRITER.writeNamed(Map.entry("", data), out, BinaryTagIO.Compression.ZLIB);
        byte[] dataBytes = out.toByteArray();
        int chunkLength = CHUNK_HEADER_LENGTH + dataBytes.length;

        int sectorCount = (int) Math.ceil(chunkLength / (double) SECTOR_SIZE);
        Check.stateCondition(sectorCount >= SECTOR_1MB, "Chunk data is too large to fit in a region file");

        lock.lock();
        try {
            // We don't attempt to reuse the current allocation, just write it to a new position and free the old one.
            int chunkIndex = getChunkIndex(chunkX, chunkZ);
            int oldLocation = locations[chunkIndex];

            // Find a new location
            int firstSector = findFreeSectors(sectorCount);
            if (firstSector == -1) {
                firstSector = allocSectors(sectorCount);
            }
            int newLocation = (firstSector << 8) | sectorCount;

            // Mark the sectors as used & free the old sectors
            markLocation(oldLocation, true);
            markLocation(newLocation, false);

            // Write the chunk data
            file.seek((long) firstSector * SECTOR_SIZE);
            file.writeInt(chunkLength);
            file.writeByte(COMPRESSION_ZLIB);
            file.write(dataBytes);

            // Update the header and write it
            locations[chunkIndex] = newLocation;
            timestamps[chunkIndex] = (int) (System.currentTimeMillis() / 1000);
            writeHeader();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    private int getChunkIndex(int chunkX, int chunkZ) {
        return (CoordConversion.chunkToRegionLocal(chunkZ) << 5) | CoordConversion.chunkToRegionLocal(chunkX);
    }

    private void readHeader() throws IOException {
        file.seek(0);
        if (file.length() < HEADER_LENGTH) {
            // new file, fill in data
            file.write(new byte[HEADER_LENGTH]);
        }

        //todo: addPadding()

        final long totalSectors = ((file.length() - 1) / SECTOR_SIZE) + 1; // Round up, last sector does not need to be full size
        for (int i = 0; i < totalSectors; i++) freeSectors.add(true);
        freeSectors.set(0, false); // First sector is locations
        freeSectors.set(1, false); // Second sector is timestamps

        // Read locations
        file.seek(0);
        for (int i = 0; i < MAX_ENTRY_COUNT; i++) {
            int location = locations[i] = file.readInt();
            if (location != 0) {
                markLocation(location, false);
            }
        }

        // Read timestamps
        for (int i = 0; i < MAX_ENTRY_COUNT; i++) {
            timestamps[i] = file.readInt();
        }
    }

    private void writeHeader() throws IOException {
        file.seek(0);
        for (int location : locations) {
            file.writeInt(location);
        }
        for (int timestamp : timestamps) {
            file.writeInt(timestamp);
        }
    }

    private int findFreeSectors(int length) {
        for (int start = 0; start < freeSectors.size() - length; start++) {
            boolean found = true;
            for (int i = 0; i < length; i++) {
                if (!freeSectors.getBoolean(start++)) {
                    found = false;
                    break;
                }
            }
            if (found) return start - length;
        }
        return -1;
    }

    private int allocSectors(int count) throws IOException {
        var eof = file.length();
        file.seek(eof);

        byte[] emptySector = new byte[SECTOR_SIZE];
        for (int i = 0; i < count; i++) {
            freeSectors.add(true);
            file.write(emptySector);
        }

        return (int) (eof / SECTOR_SIZE);
    }

    private void markLocation(int location, boolean free) {
        int sectorCount = location & 0xFF;
        int sectorStart = location >> 8;
        Check.stateCondition(sectorStart + sectorCount > freeSectors.size(), "Invalid sector count");
        for (int i = sectorStart; i < sectorStart + sectorCount; i++) {
            freeSectors.set(i, free);
        }
    }
}
