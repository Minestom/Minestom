package net.minestom.server.item.metadata;

import net.minestom.server.entity.PlayerSkin;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public class PlayerHeadMeta implements ItemMeta {

    private String playerName;
    private PlayerSkin playerSkin;

    @Override
    public boolean hasNbt() {
        return playerSkin != null;
    }

    @Override
    public boolean isSimilar(@NotNull ItemMeta itemMeta) {
        if (!(itemMeta instanceof PlayerHeadMeta))
            return false;
        final PlayerHeadMeta playerHeadMeta = (PlayerHeadMeta) itemMeta;
        return playerHeadMeta.playerSkin == playerSkin;
    }

    @Override
    public void read(@NotNull NBTCompound compound) {

    }

    @Override
    public void write(@NotNull NBTCompound compound) {

    }

    @NotNull
    @Override
    public ItemMeta clone() {
        return null;
    }
}
