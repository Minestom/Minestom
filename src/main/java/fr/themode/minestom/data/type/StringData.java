package fr.themode.minestom.data.type;

import fr.themode.minestom.data.DataType;

public class StringData extends DataType<String> {

    @Override
    public byte[] encode(String value) {
        return value.getBytes();
    }

    @Override
    public String decode(byte[] value) {
        return new String(value);
    }
}
