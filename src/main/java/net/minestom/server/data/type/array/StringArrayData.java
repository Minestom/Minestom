package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class StringArrayData extends DataType<String[]> {

    @Override
    public void encode(@NotNull BinaryWriter writer, @NotNull String[] value) {
        writer.writeVarInt(value.length);
        for (String val : value) {
            writer.writeSizedString(val);
        }
    }

    @NotNull
    @Override
    public String[] decode(@NotNull BinaryReader reader) {
        String[] array = new String[reader.readVarInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = reader.readSizedString();
        }
        return array;
    }
}
