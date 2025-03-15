package net.minestom.server.registry;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.TagStringIOExt;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public final class RegistryHelper {

    public static <T> void registerNbt(@NotNull DynamicRegistry<T> registry, @NotNull String namespace, @NotNull String snbt) {
        if (!(registry instanceof DynamicRegistryImpl<T> dynamicRegistry)) return;

        try {
            final BinaryTag tag = TagStringIOExt.readTag(snbt);
            final T value = dynamicRegistry.nbtType().read(BinaryTagSerializer.Context.EMPTY, tag);
            dynamicRegistry.register(namespace, value);
        } catch (IOException e) {
            throw new RuntimeException("failed to load registry entry '" + namespace + "'", e);
        }
    }

}
