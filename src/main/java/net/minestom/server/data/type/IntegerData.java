package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class IntegerData extends DataType<Integer> {

    @Override
    public void encode(BinaryWriter writer, Integer value) {
        writer.writeVarInt(value);
    }

    @Override
    public Integer decode(BinaryReader reader) {
        return reader.readVarInt();
    }
}