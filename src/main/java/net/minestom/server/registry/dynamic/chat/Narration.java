package net.minestom.server.registry.dynamic.chat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.util.Locale;

import static net.minestom.server.registry.dynamic.chat.NBTCompoundWriteable.writeIfPresent;


/**
 * Used to define how the message can be narrated
 *
 * @param decoration can be used to customize the sentence e.g. "playerName says: message"
 * @param priority priority of the narration
 */
public record Narration(@Nullable ChatDecoration decoration, @NotNull Priority priority) implements NBTCompoundWriteable {

    public static Narration system() {
        return system(null);
    }

    public static Narration system(ChatDecoration decoration) {
        return new Narration(decoration, Priority.SYSTEM);
    }

    public static Narration chat() {
        return chat(null);
    }

    public static Narration chat(ChatDecoration decoration) {
        return new Narration(decoration, Priority.CHAT);
    }

    @Override
    public void write(MutableNBTCompound compound) {
        writeIfPresent("decoration", decoration, compound);
        compound.set("priority", NBT.String(priority().name().toLowerCase(Locale.ROOT)));
    }

    public static Narration fromNBT(NBTCompound compound) {
        if (compound == null) return null;
        return new Narration(ChatDecoration.fromNBT(compound.getCompound("decoration")),
                Priority.fromString(compound.getString("priority")));
    }

    public enum Priority {
        CHAT, SYSTEM;

        public static Priority fromString(String string) {
            return switch (string) {
                case "chat" -> Priority.CHAT;
                case "system" -> Priority.SYSTEM;
                default -> null;
            };
        }
    }
}
