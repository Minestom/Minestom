package net.minestom.server.registry;

import net.kyori.adventure.key.Keyed;

//todo needs a new name, and should not be here
public interface ShortKeyed extends Keyed {
    short getShortId();
}
