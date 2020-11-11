package net.minestom.server.data;

import net.minestom.server.utils.NBTUtils;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Map;

/**
 * A data implementation backed by a {@link org.jglrxavpok.hephaistos.nbt.NBTCompound}.
 */
public class NbtDataImpl extends DataImpl {

    // Used to know if a nbt key is from a Data object, should NOT be changed
    public static final String KEY_PREFIX = "nbtdata_";

    @NotNull
    @Override
    public Data copy() {
        DataImpl data = new NbtDataImpl();
        data.data.putAll(this.data);
        data.dataType.putAll(this.dataType);
        return data;
    }

    /**
     * Writes all the data into a {@link NBTCompound}.
     *
     * @param nbtCompound the nbt compound to write to
     * @throws NullPointerException if the type of a data is not a primitive nbt type and therefore not supported
     *                              (you can use {@link DataType#encode(BinaryWriter, Object)} to use byte array instead)
     */
    public void writeToNbt(@NotNull NBTCompound nbtCompound) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();

            final Class type = dataType.get(key);

            final NBT nbt = NBTUtils.toNBT(value, type, false);

            Check.notNull(nbt,
                    "The type '" + type + "' is not supported within NbtDataImpl, if you wish to use a custom type you can encode the value into a byte array using a DataType");

            final String finalKey = KEY_PREFIX + key;
            nbtCompound.set(finalKey, nbt);
        }
    }

}
