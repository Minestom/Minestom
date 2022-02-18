package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
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
        super(id, true);
    }

    @NotNull
    @Override
    public ItemStack parse(@NotNull String input) throws ArgumentSyntaxException {
        return staticParse(input);
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:item_stack";

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }

    /**
     * @deprecated use {@link Argument#parse(Argument)}
     */
    @Deprecated
    public static ItemStack staticParse(@NotNull String input) throws ArgumentSyntaxException {
        final int nbtIndex = input.indexOf("{");

        if (nbtIndex == 0)
            throw new ArgumentSyntaxException("The item needs a material", input, NO_MATERIAL);

        if (nbtIndex == -1) {
            // Only material name
            final Material material = Material.fromNamespaceId(input);
            if (material == null)
                throw new ArgumentSyntaxException("Material is invalid", input, INVALID_MATERIAL);
            return ItemStack.of(material);
        } else {
            // Material plus additional NBT
            final String materialName = input.substring(0, nbtIndex);
            final Material material = Material.fromNamespaceId(materialName);
            if (material == null)
                throw new ArgumentSyntaxException("Material is invalid", input, INVALID_MATERIAL);

            final String sNBT = input.substring(nbtIndex).replace("\\\"", "\"");

            NBTCompound compound;
            try {
                compound = (NBTCompound) new SNBTParser(new StringReader(sNBT)).parse();
            } catch (NBTException e) {
                throw new ArgumentSyntaxException("Item NBT is invalid", input, INVALID_NBT);
            }

            return ItemStack.fromNBT(material, compound);
        }
    }

    @Override
    public String toString() {
        return String.format("ItemStack<%s>", getId());
    }
}
