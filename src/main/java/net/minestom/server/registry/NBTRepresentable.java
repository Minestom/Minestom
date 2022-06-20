package net.minestom.server.registry;


import org.jglrxavpok.hephaistos.nbt.NBTCompound;

@FunctionalInterface
public interface NBTRepresentable {
    NBTCompound toNBT();
}
