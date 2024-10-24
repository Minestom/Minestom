package net.minestom.server.command.builder.arguments.minecraft;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.TagStringIOExt;
import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Argument used to retrieve a {@link BinaryTag} based object, can be any kind of tag like
 * {@link net.kyori.adventure.nbt.CompoundBinaryTag}, {@link net.kyori.adventure.nbt.ListBinaryTag},
 * {@link net.kyori.adventure.nbt.IntBinaryTag}, etc...
 * <p>
 * Example: {display:{Name:"{\"text\":\"Sword of Power\"}"}} or [{display:{Name:"{\"text\":\"Sword of Power\"}"}}]
 */
public class ArgumentNbtTag extends Argument<BinaryTag> {

    public static final int INVALID_NBT = 1;

    public ArgumentNbtTag(String id) {
        super(id, true);
    }

    @NotNull
    @Override
    public BinaryTag parse(@NotNull CommandSender sender, @NotNull String input) throws ArgumentSyntaxException {
        try {
            return TagStringIOExt.readTag(input);
        } catch (IOException e) {
            throw new ArgumentSyntaxException("Invalid NBT", input, INVALID_NBT);
        }
    }

    @Override
    public ArgumentParserType parser() {
        return ArgumentParserType.NBT_TAG;
    }

    @Override
    public String toString() {
        return String.format("NBT<%s>", getId());
    }
}
