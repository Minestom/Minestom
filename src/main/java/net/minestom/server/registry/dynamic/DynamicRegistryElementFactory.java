package net.minestom.server.registry.dynamic;

import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.function.Function;

@FunctionalInterface
public interface DynamicRegistryElementFactory<T extends DynamicRegistryElement> extends Function<NBTCompound, T> {
}
