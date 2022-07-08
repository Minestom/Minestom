package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.CommandReader;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.parser.SNBTParser;

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
    public static final int INVALID_MATERIAL = 3;

    public ArgumentItemStack(String id) {
        super(id);
    }

    @Override
    public @NotNull Result<ItemStack> parse(CommandReader reader) {
        final int cursor = reader.cursor();
        final String input = reader.readWord();
        int nbtIndex = input.indexOf("{");

        if (nbtIndex == 0)
            return Result.syntaxError("The item needs a material", input, NO_MATERIAL);

        if (nbtIndex == -1) {
            // Only material name
            final Material material = Material.fromNamespaceId(input);
            if (material == null)
                return Result.incompatibleType();
            return Result.success(ItemStack.of(material));
        } else {
            // Material plus additional NBT
            final String materialName = input.substring(0, nbtIndex);
            final Material material = Material.fromNamespaceId(materialName);
            if (material == null)
                return Result.syntaxError("Material is invalid", input, INVALID_MATERIAL);

            // Move cursor to start of nbt data
            reader.setCursor(cursor+nbtIndex);
            nbtIndex = reader.getClosingIndexOfJsonObject(0);

            if (nbtIndex == -1) return Result.syntaxError("Item NBT is invalid", input, INVALID_NBT);

            final String sNBT = reader.read(nbtIndex+1).replace("\\\"", "\"");

            try {
                return Result.success(ItemStack.fromNBT(material, (NBTCompound) new SNBTParser(new StringReader(sNBT)).parse()));
            } catch (NBTException e) {
                return Result.syntaxError("Item NBT is invalid", input, INVALID_NBT);
            }
        }
    }

    @Override
    public String parser() {
        return "minecraft:item_stack";
    }

    @Override
    public String toString() {
        return String.format("ItemStack<%s>", getId());
    }
}
