package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.CommandReader;
import net.minestom.server.command.builder.arguments.Argument;
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
        super(id);
    }

    @Override
    public @NotNull Result<NBTCompound> parse(CommandReader reader) {
        int end = reader.getClosingIndexOfJsonObject(0);
        if (end == -1) {
            return Result.syntaxError("Invalid NBT", "", INVALID_NBT);
        } else {
            final String input = reader.read(end);
            try {
                NBT nbt = new SNBTParser(new StringReader(input)).parse();

                if (nbt instanceof NBTCompound compound) {
                    return Result.success(compound);
                } else {
                    return Result.syntaxError("Not a compound", input, INVALID_NBT);
                }
            } catch (NBTException e) {
                return Result.syntaxError("NBTCompound is invalid", input, INVALID_NBT);
            }
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
