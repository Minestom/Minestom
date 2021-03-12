package net.minestom.server.item.metadata;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTTypes;

import java.util.UUID;

/**
 * Represents a skull that can have an owner.
 */
public class PlayerHeadMeta extends ItemMeta {

    private UUID skullOwner;
    private PlayerSkin playerSkin;

    /**
     * Sets the owner of the skull.
     *
     * @param player The new owner of the skull.
     * @return {@code true} if the owner was successfully set, otherwise {@code false}.
     */
    public boolean setOwningPlayer(@NotNull Player player) {
        if (player.getSkin() != null) {
            this.skullOwner = player.getUuid();
            this.playerSkin = player.getSkin();
            return true;
        }
        return false;
    }

    /**
     * Retrieves the owner of the head.
     *
     * @return The head's owner.
     */
    @Nullable
    public UUID getSkullOwner() {
        return skullOwner;
    }

    /**
     * Changes the owner of the head.
     *
     * @param skullOwner The new head owner.
     */
    public void setSkullOwner(@NotNull UUID skullOwner) {
        this.skullOwner = skullOwner;
    }

    /**
     * Retrieves the skin of the head.
     *
     * @return The head's skin.
     */
    @Nullable
    public PlayerSkin getPlayerSkin() {
        return playerSkin;
    }

    /**
     * Changes the skin of the head.
     *
     * @param playerSkin The new skin for the head.
     */
    public void setPlayerSkin(@NotNull PlayerSkin playerSkin) {
        this.playerSkin = playerSkin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNbt() {
        return this.skullOwner != null || playerSkin != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSimilar(@NotNull ItemMeta itemMeta) {
        if (!(itemMeta instanceof PlayerHeadMeta))
            return false;
        final PlayerHeadMeta playerHeadMeta = (PlayerHeadMeta) itemMeta;
        return playerHeadMeta.playerSkin == playerSkin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void read(@NotNull NBTCompound compound) {
        if (compound.containsKey("SkullOwner")) {
            NBTCompound skullOwnerCompound = compound.getCompound("SkullOwner");

            if (skullOwnerCompound.containsKey("Id")) {
                this.skullOwner = Utils.intArrayToUuid(skullOwnerCompound.getIntArray("Id"));
            }

            if (skullOwnerCompound.containsKey("Properties")) {
                NBTCompound propertyCompound = skullOwnerCompound.getCompound("Properties");

                if (propertyCompound.containsKey("textures")) {
                    NBTList<NBTCompound> textures = propertyCompound.getList("textures");
                    if (textures != null) {
                        NBTCompound nbt = textures.get(0);
                        this.playerSkin = new PlayerSkin(nbt.getString("Value"), nbt.getString("Signature"));
                    }
                }

            }

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(@NotNull NBTCompound compound) {
        NBTCompound skullOwnerCompound = new NBTCompound();
        // Sets the identifier for the skull
        if (this.skullOwner != null)
            skullOwnerCompound.setIntArray("Id", Utils.uuidToIntArray(this.skullOwner));

        if (this.playerSkin == null && this.skullOwner != null) {
            this.playerSkin = PlayerSkin.fromUuid(this.skullOwner.toString());
        }

        if (this.playerSkin != null) {
            NBTList<NBTCompound> textures = new NBTList<>(NBTTypes.TAG_Compound);
            String value = this.playerSkin.getTextures() == null ? "" : this.playerSkin.getTextures();
            String signature = this.playerSkin.getSignature() == null ? "" : this.playerSkin.getSignature();
            textures.add(new NBTCompound().setString("Value", value).setString("Signature", signature));
            skullOwnerCompound.set("Properties", new NBTCompound().set("textures", textures));
        }

        compound.set("SkullOwner", skullOwnerCompound);

    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public ItemMeta clone() {
        PlayerHeadMeta playerHeadMeta = (PlayerHeadMeta) super.clone();
        playerHeadMeta.skullOwner = this.skullOwner;
        playerHeadMeta.playerSkin = this.playerSkin;
        return playerHeadMeta;
    }


}
