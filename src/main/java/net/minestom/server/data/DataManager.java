package net.minestom.server.data;

import net.minestom.server.data.type.CharacterData;
import net.minestom.server.data.type.*;
import net.minestom.server.data.type.array.ItemStackArrayData;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.PrimitiveConversion;

import java.util.HashMap;
import java.util.Map;

public class DataManager {

    private Map<Class, DataType> dataTypeMap = new HashMap<>();

    {
        registerType(Byte.class, new ByteData());
        registerType(Boolean.class, new BooleanData());
        registerType(Character.class, new CharacterData());
        registerType(Short.class, new ShortData());
        registerType(Integer.class, new IntegerData());
        registerType(Long.class, new LongData());
        registerType(Float.class, new FloatData());
        registerType(Double.class, new DoubleData());

        registerType(String.class, new StringData());

        registerType(SerializableData.class, new SerializableDataData());

        registerType(ItemStack.class, new ItemStackData());
        registerType(ItemStack[].class, new ItemStackArrayData());
    }

    public <T> void registerType(Class<T> clazz, DataType<T> dataType) {
        clazz = PrimitiveConversion.getObjectClass(clazz);
        if (dataTypeMap.containsKey(clazz))
            throw new UnsupportedOperationException("Type " + clazz.getName() + " has already been registed");

        this.dataTypeMap.put(clazz, dataType);
    }

    public <T> DataType<T> getDataType(Class<T> clazz) {
        return dataTypeMap.get(PrimitiveConversion.getObjectClass(clazz));
    }

}
