package net.minestom.server.item.metadata;

import net.minestom.server.entity.EntityType;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

// TODO for which item
public class SpawnEggMeta implements ItemMeta {

    private EntityType entityType;

    @Override
    public boolean hasNbt() {
        return entityType != null;
    }

    @Override
    public boolean isSimilar(ItemMeta itemMeta) {
        if (!(itemMeta instanceof SpawnEggMeta))
            return false;
        final SpawnEggMeta spawnEggMeta = (SpawnEggMeta) itemMeta;
        return spawnEggMeta.entityType == entityType;
    }

    @Override
    public void read(NBTCompound compound) {
        if (compound.containsKey("EntityTag")) {
            // TODO
        }
    }

    @Override
    public void write(NBTCompound compound) {
        if (!hasNbt())
            return;
        NBTCompound entityCompound = new NBTCompound();
        if (entityType != null) {
            entityCompound.setString("id", entityType.getNamespaceID());
        }

    }

    @Override
    public ItemMeta clone() {
        SpawnEggMeta spawnEggMeta = new SpawnEggMeta();
        spawnEggMeta.entityType = entityType;
        return spawnEggMeta;
    }
}
