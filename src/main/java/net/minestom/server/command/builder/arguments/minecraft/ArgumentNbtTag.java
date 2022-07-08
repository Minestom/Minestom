package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.CommandReader;
import net.minestom.server.command.builder.arguments.Argument;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.parser.SNBTParser;

import java.io.StringReader;

/**
 * Argument used to retrieve a {@link NBT} based object, can be any kind of tag like
 * {@link org.jglrxavpok.hephaistos.nbt.NBTCompound}, {@link org.jglrxavpok.hephaistos.nbt.NBTList},
 * {@link org.jglrxavpok.hephaistos.nbt.NBTInt}, etc...
 * <p>
 * Example: {display:{Name:"{\"text\":\"Sword of Power\"}"}} or [{display:{Name:"{\"text\":\"Sword of Power\"}"}}]
 */
public class ArgumentNbtTag extends Argument<NBT> {

    public static final int INVALID_NBT = 1;

    public ArgumentNbtTag(String id) {
        super(id);
    }

    @Override
    public @NotNull Result<NBT> parse(CommandReader reader) {
        int end = reader.getClosingIndexOfJsonObject(0);
        if (end == -1) {
            end = reader.getClosingIndexOfJsonArray(0);
        }
        final String input;
        if (end == -1) {
            input = reader.readWord();
        } else {
            input = reader.read(end + 1);
        }
        try {
            return Result.success(new SNBTParser(new StringReader(input)).parse());
        } catch (NBTException e) {
            return Result.syntaxError("Invalid NBT", input, INVALID_NBT);
        }
    }

    @Override
    public String parser() {
        return "minecraft:nbt_tag";
    }

    @Override
    public String toString() {
        return String.format("NBT<%s>", getId());
    }
}
