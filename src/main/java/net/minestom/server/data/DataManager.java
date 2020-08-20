package net.minestom.server.data;

import net.minestom.server.data.type.CharacterData;
import net.minestom.server.data.type.*;
import net.minestom.server.data.type.array.*;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.PrimitiveConversion;
import net.minestom.server.utils.validate.Check;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DataManager {

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

        registerType(UUID.class, new UuidType());

        registerType(SerializableData.class, new SerializableDataData());

        registerType(ItemStack.class, new ItemStackData());
        registerType(ItemStack[].class, new ItemStackArrayData());

        registerType(Inventory.class, new InventoryData());
    }

    /**
     * Register a new data type
     *
     * @param clazz    the data class
     * @param dataType the data type associated
     * @param <T>      the data type
     * @throws IllegalStateException if the type {@code clazz} is already registered
     */
    public <T> void registerType(Class<T> clazz, DataType<T> dataType) {
        clazz = PrimitiveConversion.getObjectClass(clazz);
        Check.stateCondition(dataTypeMap.containsKey(clazz),
                "Type " + clazz.getName() + " has already been registered");

        this.dataTypeMap.put(clazz, dataType);
    }

    /**
     * Get the data type associated with a class
     *
     * @param clazz the data class
     * @param <T>   the data type
     * @return the {@link DataType} associated to the class
     * @throws NullPointerException if none is found
     */
    public <T> DataType<T> getDataType(Class<T> clazz) {
        clazz = PrimitiveConversion.getObjectClass(clazz);
        return dataTypeMap.get(clazz);
    }

}
