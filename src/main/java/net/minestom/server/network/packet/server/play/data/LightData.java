package net.minestom.server.network.packet.server.play.data;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;
import java.util.List;

import static net.minestom.server.network.NetworkBuffer.*;

public record LightData(boolean trustEdges,
                        @NotNull BitSet skyMask, @NotNull BitSet blockMask,
                        @NotNull BitSet emptySkyMask, @NotNull BitSet emptyBlockMask,
                        @NotNull List<byte[]> skyLight,
                        @NotNull List<byte[]> blockLight) implements NetworkBuffer.Writer {
    public LightData(@NotNull NetworkBuffer reader) {
        this(reader.read(BOOLEAN),
                BitSet.valueOf(reader.read(LONG_ARRAY)), BitSet.valueOf(reader.read(LONG_ARRAY)),
                BitSet.valueOf(reader.read(LONG_ARRAY)), BitSet.valueOf(reader.read(LONG_ARRAY)),
                reader.readCollection(BYTE_ARRAY), reader.readCollection(BYTE_ARRAY));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(BOOLEAN, trustEdges);

        writer.write(LONG_ARRAY, skyMask.toLongArray());
        writer.write(LONG_ARRAY, blockMask.toLongArray());

        writer.write(LONG_ARRAY, emptySkyMask.toLongArray());
        writer.write(LONG_ARRAY, emptyBlockMask.toLongArray());

        writer.writeCollection(BYTE_ARRAY, skyLight);
        writer.writeCollection(BYTE_ARRAY, blockLight);
    }
}
