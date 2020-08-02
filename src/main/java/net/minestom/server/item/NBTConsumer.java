package net.minestom.server.item;

import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.function.Consumer;

@FunctionalInterface
public interface NBTConsumer extends Consumer<NBTCompound> {
}
