package net.minestom.server.network.packet.server.play.data;

import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;
import java.util.List;

public final class LightPacketData implements Writeable {
    private final boolean trustEdges;
    private final BitSet skyMask;
    private final BitSet blockMask;
    private final BitSet emptySkyMask;
    private final BitSet emptyBlockMask;
    private final List<byte[]> skyLight;
    private final List<byte[]> blockLight;

    public LightPacketData(boolean trustEdges,
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
