package net.minestom.server.item.metadata;

import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemMetaBuilder;
import net.minestom.server.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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
                    nbtCompound.remove("Properties");
                    return;
                }

                final String value = Objects.requireNonNullElse(this.playerSkin.textures(), "");
                final String signature = Objects.requireNonNullElse(this.playerSkin.signature(), "");
                NBTList<NBTCompound> textures = new NBTList<>(NBTType.TAG_Compound,
                        List.of(NBT.Compound(Map.of(
                                "Value", NBT.String(value),
                                "Signature", NBT.String(signature)))));
                nbtCompound.set("Properties", NBT.Compound(Map.of("textures", textures)));
            });
            return this;
        }

        @Override
        public @NotNull PlayerHeadMeta build() {
            return new PlayerHeadMeta(this, skullOwner, playerSkin);
        }

        @Override
        public void read(@NotNull NBTCompound nbtCompound) {
            if (nbtCompound.get("SkullOwner") instanceof NBTCompound skullOwnerCompound) {
                if (skullOwnerCompound.get("Id") instanceof NBTIntArray id) {
                    this.skullOwner = Utils.intArrayToUuid(id.getValue().copyArray());
                }

                if (skullOwnerCompound.get("Properties") instanceof NBTCompound propertyCompound) {
                    if (propertyCompound.get("textures") instanceof NBTList<?> textures &&
                            textures.getSubtagType() == NBTType.TAG_Compound) {
                        NBTCompound nbt = (NBTCompound) textures.get(0);
                        this.playerSkin = new PlayerSkin(nbt.getString("Value"), nbt.getString("Signature"));
                    }
                }
            }
        }
    }
}
