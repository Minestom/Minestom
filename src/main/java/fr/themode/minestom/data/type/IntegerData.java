package fr.themode.minestom.data.type;

import fr.themode.minestom.data.DataType;
import fr.themode.minestom.utils.SerializerUtils;

public class IntegerData extends DataType<Integer> {

    @Override
    public byte[] encode(Integer value) {
        return SerializerUtils.intToBytes(value);
    }

    @Override
    public Integer decode(byte[] value) {
        return SerializerUtils.bytesToInt(value);
    }
}