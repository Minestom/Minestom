package net.minestom.server.command;

import net.kyori.adventure.audience.Audience;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.permission.Permission;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.util.Set;

/**
 * Sender used in {@link CommandManager#executeServerCommand(StringReader)}.<br>
 * Although this class implemented {@link CommandSender} and thus {@link Audience}, no data can be sent to this sender
 * because its purpose is to process the data of {@link CommandContext#getData()}.
 */
public class ServerSender implements CommandSender {

    private final Set<Permission> permissions = Set.of();
    private final MutableNBTCompound nbtCompound = new MutableNBTCompound();

    // Cache a CommandOrigin instance for this sender because they are immutable
    private final @NotNull CommandOrigin origin = new CommandOrigin(this);

    public @NotNull CommandOrigin getOrigin() {
        return origin;
    }

    @NotNull
    @Override
    public Set<Permission> getAllPermissions() {
        return permissions;
    }

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return tag.read(nbtCompound);
    }

    @Override
    public <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
        tag.write(nbtCompound, value);
    }
}
