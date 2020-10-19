package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.builder.arguments.Argument;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.nbt.SNBTParser;

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
        super(id, true);
    }

    @Override
    public int getCorrectionResult(String value) {
        try {
            NBT nbt = new SNBTParser(new StringReader(value)).parse();
            return nbt != null ? SUCCESS : INVALID_NBT;
        } catch (NBTException e) {
            return INVALID_NBT;
        }
    }

    @Override
    public NBT parse(String value) {
        try {
            NBT nbt = new SNBTParser(new StringReader(value)).parse();
            return nbt;
        } catch (NBTException e) {
            return null;
        }
    }

    @Override
    public int getConditionResult(NBT value) {
        return SUCCESS;
    }
}
