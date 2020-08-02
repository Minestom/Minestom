package net.minestom.server.item.metadata;

import net.minestom.server.entity.PlayerSkin;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public class PlayerHeadMeta implements ItemMeta {

    private String playerName;
    private PlayerSkin playerSkin;

    @Override
    public boolean hasNbt() {
        return playerSkin != null;
    }

    @Override
    public boolean isSimilar(ItemMeta itemMeta) {
        if (!(itemMeta instanceof PlayerHeadMeta))
            return false;
        final PlayerHeadMeta playerHeadMeta = (PlayerHeadMeta) itemMeta;
        return playerHeadMeta.playerSkin == playerSkin;
    }

    @Override
    public void read(NBTCompound compound) {

    }

    @Override
    public void write(NBTCompound compound) {

    }

    @Override
    public ItemMeta clone() {
        return null;
    }
}
