package fr.themode.minestom.data;

import fr.themode.minestom.data.type.IntegerData;

public abstract class DataType<T> {

    public static final DataType INTEGER = new IntegerData();

    public abstract byte[] encode(T value);

    public abstract T decode(byte[] value);

    // TODO get object type class ?

}