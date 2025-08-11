package net.minestom.server.instance.anvil;

import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.BitSet;
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

    public static String getFileName(int regionX, int regionZ) {
        return "r." + regionX + "." + regionZ + ".mca";
    }

    private final ReentrantLock lock = new ReentrantLock();
    private final RandomAccessFile file;

    private final int[] locations = new int[MAX_ENTRY_COUNT];
    private final int[] timestamps = new int[MAX_ENTRY_COUNT];
    private final BitSet freeSectors = new BitSet(2);

    // Cache header data to avoid repeated file I/O
    private final ByteBuffer headerBuffer = ByteBuffer.allocate(HEADER_LENGTH);
    private boolean headerDirty = false;

    public RegionFile(Path path) throws IOException {
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

    public void writeChunkData(int chunkX, int chunkZ, CompoundBinaryTag data) throws IOException {
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
            // store timestamps in seconds since epoch
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

        final long totalSectors = ((file.length() - 1) / SECTOR_SIZE) + 1; // Round up, last sector does not need to be full size
        freeSectors.set(0, (int) totalSectors); // Set all sectors as free initially
        freeSectors.clear(0); // First sector is locations
        freeSectors.clear(1); // Second sector is timestamps

        // Read entire header in one operation
        file.seek(0);
        byte[] headerData = new byte[HEADER_LENGTH];
        file.readFully(headerData);
        headerBuffer.clear();
        headerBuffer.put(headerData);
        headerBuffer.flip();

        // Parse locations from buffer
        for (int i = 0; i < MAX_ENTRY_COUNT; i++) {
            int location = locations[i] = headerBuffer.getInt();
            if (location != 0) {
                markLocationInBitSet(location, false);
            }
        }

        // Parse timestamps from buffer
        for (int i = 0; i < MAX_ENTRY_COUNT; i++) {
            timestamps[i] = headerBuffer.getInt();
        }

        headerDirty = false;
    }

    private void writeHeader() throws IOException {
        if (!headerDirty) return; // Skip if header hasn't changed

        headerBuffer.clear();

        // Write locations to buffer
        for (int location : locations) {
            headerBuffer.putInt(location);
        }

        // Write timestamps to buffer
        for (int timestamp : timestamps) {
            headerBuffer.putInt(timestamp);
        }

        // Write entire header in one operation
        file.seek(0);
        file.write(headerBuffer.array());
        headerDirty = false;
    }

    private int findFreeSectors(int length) {
        int start = freeSectors.nextSetBit(0);
        while (start != -1 && start + length <= freeSectors.size()) {
            // Check if we have 'length' consecutive free sectors starting at 'start'
            int nextClear = freeSectors.nextClearBit(start);
            if (nextClear >= start + length) {
                return start;
            }
            start = freeSectors.nextSetBit(nextClear);
        }
        return -1;
    }

    private int allocSectors(int count) throws IOException {
        var eof = file.length();
        file.seek(eof);

        byte[] emptySector = new byte[SECTOR_SIZE];
        int startSector = (int) (eof / SECTOR_SIZE);
        for (int i = 0; i < count; i++) {
            freeSectors.set(startSector + i, true);
            file.write(emptySector);
        }
        return startSector;
    }

    private void markLocation(int location, boolean free) {
        markLocationInBitSet(location, free);
        headerDirty = true;
    }

    private void markLocationInBitSet(int location, boolean free) {
        int sectorCount = location & 0xFF;
        int sectorStart = location >> 8;
        Check.stateCondition(sectorStart + sectorCount > freeSectors.size(), "Invalid sector count");
        freeSectors.set(sectorStart, sectorStart + sectorCount, free);
    }
}
