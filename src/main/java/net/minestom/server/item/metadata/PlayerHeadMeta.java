package net.minestom.server.item.metadata;

import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemMetaBuilder;
import net.minestom.server.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTTypes;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class PlayerHeadMeta extends ItemMeta implements ItemMetaBuilder.Provider<PlayerHeadMeta.Builder> {

    private final UUID skullOwner;
    private final PlayerSkin playerSkin;

    protected PlayerHeadMeta(@NotNull ItemMetaBuilder metaBuilder, UUID skullOwner,
                             @Nullable PlayerSkin playerSkin) {
        super(metaBuilder);
        this.skullOwner = skullOwner;
        this.playerSkin = playerSkin;
    }

    public UUID getSkullOwner() {
        return skullOwner;
    }

    public @Nullable PlayerSkin getPlayerSkin() {
        return playerSkin;
    }

    public static class Builder extends ItemMetaBuilder {

        private UUID skullOwner;
        private PlayerSkin playerSkin;

        public Builder skullOwner(@Nullable UUID skullOwner) {
            this.skullOwner = skullOwner;
            handleCompound("SkullOwner", nbtCompound ->
                    nbtCompound.setIntArray("Id", Utils.uuidToIntArray(this.skullOwner)));
            return this;
        }

        public Builder playerSkin(@Nullable PlayerSkin playerSkin) {
            this.playerSkin = playerSkin;
            handleCompound("SkullOwner", nbtCompound -> {
                if (playerSkin == null) {
                    nbtCompound.removeTag("Properties");
                    return;
                }

                NBTList<NBTCompound> textures = new NBTList<>(NBTTypes.TAG_Compound);
                final String value = Objects.requireNonNullElse(this.playerSkin.textures(), "");
                final String signature = Objects.requireNonNullElse(this.playerSkin.signature(), "");
                textures.add(new NBTCompound().setString("Value", value).setString("Signature", signature));
                nbtCompound.set("Properties", new NBTCompound().set("textures", textures));
            });
            return this;
        }

        @Override
        public @NotNull PlayerHeadMeta build() {
            return new PlayerHeadMeta(this, skullOwner, playerSkin);
        }

        @Override
        public void read(@NotNull NBTCompound nbtCompound) {
            if (nbtCompound.containsKey("SkullOwner")) {
                NBTCompound skullOwnerCompound = nbtCompound.getCompound("SkullOwner");

                if (skullOwnerCompound.containsKey("Id")) {
                    skullOwner(Utils.intArrayToUuid(skullOwnerCompound.getIntArray("Id")));
                }

                if (skullOwnerCompound.containsKey("Properties")) {
                    NBTCompound propertyCompound = skullOwnerCompound.getCompound("Properties");

                    if (propertyCompound.containsKey("textures")) {
                        NBTList<NBTCompound> textures = propertyCompound.getList("textures");
                        if (textures != null) {
                            NBTCompound nbt = textures.get(0);
                            playerSkin(new PlayerSkin(nbt.getString("Value"), nbt.getString("Signature")));
                        }
                    }

                }
            }
        }

        @Override
        protected @NotNull Supplier<ItemMetaBuilder> getSupplier() {
            return Builder::new;
        }
    }
}
