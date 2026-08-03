package net.minestom.server.dialog;

import net.kyori.adventure.dialog.DialogLike;
import net.minestom.server.registry.RegistryKey;

value record RegistryKeyDialog(RegistryKey<Dialog> key) implements DialogLike {
}
