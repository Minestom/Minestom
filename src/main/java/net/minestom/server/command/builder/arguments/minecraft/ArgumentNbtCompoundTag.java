package net.minestom.server.command.builder.arguments.minecraft;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;

import java.io.IOException;

/**
 * Argument used to retrieve a {@link CompoundBinaryTag} if you need key-value data.
 * <p>
 * Example: {display:{Name:"{\"text\":\"Sword of Power\"}"}}
 */
public class ArgumentNbtCompoundTag extends Argument<CompoundBinaryTag> {

    public static final int INVALID_NBT = 1;

    public ArgumentNbtCompoundTag(String id) {
        super(id, true);
    }

    @Override
    public CompoundBinaryTag parse(CommandSender sender, String input) throws ArgumentSyntaxException {
        try {
            return MinestomAdventure.tagStringIO().asCompound(input);
        } catch (IOException e) {
            throw new ArgumentSyntaxException("NBTCompound is invalid", input, INVALID_NBT);
        }
    }

    @Override
    public ArgumentParserType parser() {
        return ArgumentParserType.NBT_COMPOUND_TAG;
    }

    @Override
    public String toString() {
        return String.format("NbtCompound<%s>", getId());
    }
}
