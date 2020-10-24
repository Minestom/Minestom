package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.NBTUtils;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.nbt.SNBTParser;

import java.io.StringReader;

/**
 * Argument which can be used to retrieve an {@link ItemStack} from its material and with NBT data.
 * <p>
 * It is the same type as the one used in the /give command.
 * <p>
 * Example: diamond_sword{display:{Name:"{\"text\":\"Sword of Power\"}"}}
 */
public class ArgumentItemStack extends Argument<ItemStack> {

    public static final int NO_MATERIAL = 1;
    public static final int INVALID_NBT = 2;

    public ArgumentItemStack(String id) {
        super(id, true);
    }

    @Override
    public int getCorrectionResult(@NotNull String value) {
        if (value.startsWith("{")) {
            return NO_MATERIAL;
        }

        final int nbtIndex = value.indexOf("{");

        if (nbtIndex == -1 && !value.contains(" ")) {
            // Only item name
            return SUCCESS;
        } else {
            // has nbt
            final String sNBT = value.substring(nbtIndex);
            try {
                NBT nbt = new SNBTParser(new StringReader(sNBT)).parse();
                return nbt instanceof NBTCompound ? SUCCESS : INVALID_NBT;
            } catch (NBTException e) {
                return INVALID_NBT;
            }
        }
    }

    @NotNull
    @Override
    public ItemStack parse(@NotNull String value) {
        final int nbtIndex = value.indexOf("{");

        if (nbtIndex == -1) {
            // Only item name
            final Material material = Registries.getMaterial(value);
            return new ItemStack(material, (byte) 1);
        } else {
            final String materialName = value.substring(0, nbtIndex);
            final Material material = Registries.getMaterial(materialName);

            ItemStack itemStack = new ItemStack(material, (byte) 1);

            final String sNBT = value.substring(nbtIndex).replace("\\\"", "\"");

            NBTCompound compound = null;
            try {
                compound = (NBTCompound) new SNBTParser(new StringReader(sNBT)).parse();
            } catch (NBTException e) {
                e.printStackTrace();
            }

            assert compound != null;

            NBTUtils.loadDataIntoItem(itemStack, compound);

            return itemStack;
        }
    }

    @Override
    public int getConditionResult(@NotNull ItemStack value) {
        return SUCCESS;
    }
}
