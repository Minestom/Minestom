package net.minestom.server.network.packet.server.play.data;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;
import java.util.List;

import static net.minestom.server.network.NetworkBuffer.BYTE_ARRAY;
import static net.minestom.server.network.NetworkBuffer.LONG_ARRAY;

public record LightData(
        @NotNull BitSet skyMask, @NotNull BitSet blockMask,
        @NotNull BitSet emptySkyMask, @NotNull BitSet emptyBlockMask,
        @NotNull List<byte[]> skyLight,
        @NotNull List<byte[]> blockLight
) implements NetworkBuffer.Writer {
    public static final int MAX_SECTIONS = 4096 / 16;

    public LightData(@NotNull NetworkBuffer reader) {
        this(
                BitSet.valueOf(reader.read(LONG_ARRAY)), BitSet.valueOf(reader.read(LONG_ARRAY)),
                BitSet.valueOf(reader.read(LONG_ARRAY)), BitSet.valueOf(reader.read(LONG_ARRAY)),
                reader.readCollection(BYTE_ARRAY, MAX_SECTIONS), reader.readCollection(BYTE_ARRAY, MAX_SECTIONS)
        );
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(LONG_ARRAY, skyMask.toLongArray());
        writer.write(LONG_ARRAY, blockMask.toLongArray());

        writer.write(LONG_ARRAY, emptySkyMask.toLongArray());
        writer.write(LONG_ARRAY, emptyBlockMask.toLongArray());

        writer.writeCollection(BYTE_ARRAY, skyLight);
        writer.writeCollection(BYTE_ARRAY, blockLight);
    }
}
