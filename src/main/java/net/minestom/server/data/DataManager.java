package net.minestom.server.data;

import net.minestom.server.data.type.CharacterData;
import net.minestom.server.data.type.*;
import net.minestom.server.data.type.array.*;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.PrimitiveConversion;

import java.util.HashMap;
import java.util.Map;

public class DataManager {

    private Map<Class, DataType> dataTypeMap = new HashMap<>();

    {
        registerType(Byte.class, new ByteData());
        registerType(byte[].class, new ByteArrayData());

        registerType(Boolean.class, new BooleanData());
        registerType(boolean[].class, new BooleanArrayData());

        registerType(Character.class, new CharacterData());
        registerType(char[].class, new CharacterArrayData());

        registerType(Short.class, new ShortData());
        registerType(short[].class, new ShortArrayData());

        registerType(Integer.class, new IntegerData());
        registerType(int[].class, new IntegerArrayData());

        registerType(Long.class, new LongData());
        registerType(long[].class, new LongArrayData());

        registerType(Float.class, new FloatData());
        registerType(float[].class, new FloatArrayData());

        registerType(Double.class, new DoubleData());
        registerType(double[].class, new DoubleArrayData());

        registerType(String.class, new StringData());
        registerType(String[].class, new StringArrayData());

        registerType(SerializableData.class, new SerializableDataData());

        registerType(ItemStack.class, new ItemStackData());
        registerType(ItemStack[].class, new ItemStackArrayData());

        registerType(Inventory.class, new InventoryData());
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
