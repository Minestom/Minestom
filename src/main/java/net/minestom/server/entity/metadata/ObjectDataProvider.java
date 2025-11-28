package net.minestom.server.entity.metadata;

// https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Object_Data
public interface ObjectDataProvider {

    int getObjectData();

    boolean requiresVelocityPacketAtSpawn();

}
