package net.minestom.server.dialog;

import net.kyori.adventure.dialog.DialogLike;
import net.minestom.server.registry.RegistryKey;

record RegistryKeyDialog(RegistryKey<Dialog> key) implements DialogLike {
}
