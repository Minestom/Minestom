package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.builder.arguments.Argument;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.nbt.SNBTParser;

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

    @Override
    public int getCorrectionResult(@NotNull String value) {
        try {
            NBT nbt = new SNBTParser(new StringReader(value)).parse();
            return nbt instanceof NBTCompound ? SUCCESS : INVALID_NBT;
        } catch (NBTException e) {
            return INVALID_NBT;
        }
    }

    @NotNull
    @Override
    public NBTCompound parse(@NotNull String value) {
        try {
            NBT nbt = new SNBTParser(new StringReader(value)).parse();
            return (NBTCompound) nbt;
        } catch (NBTException e) {
            return null;
        }
    }

    @Override
    public int getConditionResult(@NotNull NBTCompound value) {
        return SUCCESS;
    }
}
