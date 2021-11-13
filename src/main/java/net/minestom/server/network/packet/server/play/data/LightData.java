package net.minestom.server.network.packet.server.play.data;

import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;
import java.util.List;

public final class LightData implements Writeable {
    private final boolean trustEdges;
    private final BitSet skyMask;
    private final BitSet blockMask;
    private final BitSet emptySkyMask;
    private final BitSet emptyBlockMask;
    private final List<byte[]> skyLight;
    private final List<byte[]> blockLight;

    public LightData(boolean trustEdges,
                     BitSet skyMask, BitSet blockMask,
                     BitSet emptySkyMask, BitSet emptyBlockMask,
                     List<byte[]> skyLight, List<byte[]> blockLight) {
        this.trustEdges = trustEdges;
        this.skyMask = skyMask;
        this.blockMask = blockMask;
        this.emptySkyMask = emptySkyMask;
        this.emptyBlockMask = emptyBlockMask;
        this.skyLight = skyLight;
        this.blockLight = blockLight;
    }

    public LightData(BinaryReader reader) {
        this.trustEdges = reader.readBoolean();
        this.skyMask = BitSet.valueOf(reader.readLongArray());
        this.blockMask = BitSet.valueOf(reader.readLongArray());
        this.emptySkyMask = BitSet.valueOf(reader.readLongArray());
        this.emptyBlockMask = BitSet.valueOf(reader.readLongArray());
        this.skyLight = reader.readVarIntList(r -> r.readBytes(r.readVarInt()));
        this.blockLight = reader.readVarIntList(r -> r.readBytes(r.readVarInt()));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBoolean(trustEdges);

        writer.writeLongArray(skyMask.toLongArray());
        writer.writeLongArray(blockMask.toLongArray());

        writer.writeLongArray(emptySkyMask.toLongArray());
        writer.writeLongArray(emptyBlockMask.toLongArray());

        writer.writeVarInt(skyLight.size());
        for (byte[] bytes : skyLight) {
            writer.writeVarInt(2048); // Always 2048 length
            writer.writeBytes(bytes);
        }

        writer.writeVarInt(blockLight.size());
        for (byte[] bytes : blockLight) {
            writer.writeVarInt(2048); // Always 2048 length
            writer.writeBytes(bytes);
        }
    }
}
