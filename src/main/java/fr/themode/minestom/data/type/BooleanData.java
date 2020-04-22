package fr.themode.minestom.data.type;

import fr.themode.minestom.data.DataType;

public class BooleanData extends DataType<Boolean> {
    @Override
    public byte[] encode(Boolean value) {
        return new byte[]{(byte) (value.booleanValue() ? 1 : 0)};
    }

    @Override
    public Boolean decode(byte[] value) {
        return value[0] == 1;
    }
}
