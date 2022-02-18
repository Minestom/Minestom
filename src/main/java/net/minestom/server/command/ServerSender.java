package net.minestom.server.command;

import net.kyori.adventure.audience.Audience;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.permission.Permission;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Sender used in {@link CommandManager#executeServerCommand(String)}.
 * <p>
 * Although this class implemented {@link CommandSender} and thus {@link Audience}, no
 * data can be sent to this sender because it's purpose is to process the data of
 * {@link CommandContext#getReturnData()}.
 */
public class ServerSender implements CommandSender {

    private final Set<Permission> permissions = Collections.unmodifiableSet(new HashSet<>());
    private final MutableNBTCompound nbtCompound = new MutableNBTCompound();

    @NotNull
    @Override
    public Set<Permission> getAllPermissions() {
        return permissions;
    }

    @Override
    public <T> @Nullable T getTag(@NotNull Tag<T> tag) {
        return tag.read(nbtCompound);
    }

    @Override
    public <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
        tag.write(nbtCompound, value);
    }
}
