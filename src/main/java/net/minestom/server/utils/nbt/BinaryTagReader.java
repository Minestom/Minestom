package net.minestom.server.utils.nbt;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagType;
import net.kyori.adventure.nbt.BinaryTagTypes;

import java.io.DataInput;
import java.io.IOException;
import java.util.Map;

// Based on net.kyori.adventure.nbt.BinaryTagReaderImpl licensed under the MIT license.
// https://github.com/KyoriPowered/adventure/blob/main/4/nbt/src/main/java/net/kyori/adventure/nbt/BinaryTagReaderImpl.java
public record BinaryTagReader(DataInput input) {

    static {
        BinaryTagTypes.COMPOUND.id(); // Force initialization
    }

    public BinaryTag readNameless() throws IOException {
        BinaryTagType<? extends BinaryTag> type = BinaryTagUtil.nbtTypeFromId(input.readByte());
        return type.read(input);
    }

    public Map.Entry<String, BinaryTag> readNamed() throws IOException {
        BinaryTagType<? extends BinaryTag> type = BinaryTagUtil.nbtTypeFromId(input.readByte());
        String name = input.readUTF();
        return Map.entry(name, type.read(input));
    }
}
