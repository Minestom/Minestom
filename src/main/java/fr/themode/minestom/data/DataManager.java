package fr.themode.minestom.data;

import fr.themode.minestom.data.type.IntegerData;
import fr.themode.minestom.utils.PrimitiveConversion;

import java.util.HashMap;
import java.util.Map;

public class DataManager {

    private Map<Class, DataType> dataTypeMap = new HashMap<>();

    {
        registerType(Integer.class, new IntegerData());
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
