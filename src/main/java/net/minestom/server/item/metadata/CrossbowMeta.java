package net.minestom.server.item.metadata;

import net.minestom.server.item.ItemMetaView;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.tag.TagReadable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;

public record CrossbowMeta(TagReadable readable) implements ItemMetaView<CrossbowMeta.Builder> {
    private static final Tag<List<ItemStack>> PROJECTILES = Tag.ItemStack("ChargedProjectiles").list();
    private static final Tag<Boolean> CHARGED = Tag.Boolean("Charged");

    public @NotNull List<ItemStack> getProjectiles() {
        return getTag(PROJECTILES);
    }

    public boolean isCharged() {
        return getTag(CHARGED);
    }

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return readable.getTag(tag);
    }

    public record Builder(TagHandler tagHandler) implements ItemMetaView.Builder {
        public Builder projectile(@NotNull ItemStack projectile) {
            setTag(PROJECTILES, List.of(projectile));
            return this;
        }

        public Builder projectiles(@NotNull ItemStack projectile1, @NotNull ItemStack projectile2, @NotNull ItemStack projectile3) {
            setTag(PROJECTILES, List.of(projectile1, projectile2, projectile3));
            return this;
        }

        public Builder charged(boolean charged) {
            setTag(CHARGED, charged);
            return this;
        }
    }
}
