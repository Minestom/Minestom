package net.minestom.server.permission;

import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

/**
 * Describes a permission string.
 */
public class PermissionDescriptor implements TagHandler {

    private final @NotNull String name;
    private final @NotNull String description;

    private final NBTCompound nbt = new NBTCompound();

    public PermissionDescriptor(@NotNull String name) {
        this(name, "");
    }

    public PermissionDescriptor(@NotNull String name, @NotNull String description) {
        this.name = name;
        this.description = description;
    }

    public @NotNull String getDescription() {
        return this.description;
    }

    public @NotNull String getName() {
        return this.name;
    }

    @Override
    public <T> @Nullable T getTag(@NotNull Tag<T> tag) {
        return tag.read(nbt);
    }

    @Override
    public <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
        tag.write(nbt, value);
    }
}
