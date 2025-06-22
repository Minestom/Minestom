package net.minestom.server.dialog;

import net.kyori.adventure.dialog.DialogLike;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;

record RegistryKeyDialog(@NotNull RegistryKey<Dialog> key) implements DialogLike {
}
