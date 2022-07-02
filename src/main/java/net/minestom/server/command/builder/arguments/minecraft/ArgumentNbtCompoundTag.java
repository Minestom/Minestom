package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.parser.SNBTParser;

import java.io.StringReader;

/**
 * Argument used to retrieve a {@link NBTCompound} if you need key-value data.
 * <p>
 * Example: {display:{Name:"{\"text\":\"Sword of Power\"}"}}
 */
public class ArgumentNbtCompoundTag extends Argument<NBTCompound> {

    public static final int INVALID_NBT = 1;

    public ArgumentNbtCompoundTag(String id) {
        super(id, true);
    }

    @NotNull
    @Override
    public NBTCompound parse(@NotNull String input) throws ArgumentSyntaxException {
        try {
            NBT nbt = new SNBTParser(new StringReader(input)).parse();

            if (!(nbt instanceof NBTCompound))
                throw new ArgumentSyntaxException("NBTCompound is invalid", input, INVALID_NBT);

            return (NBTCompound) nbt;
        } catch (NBTException e) {
            throw new ArgumentSyntaxException("NBTCompound is invalid", input, INVALID_NBT);
        }
    }

    @Override
    public String parser() {
        return "minecraft:nbt_compound_tag";
    }

    @Override
    public String toString() {
        return String.format("NbtCompound<%s>", getId());
    }
}
