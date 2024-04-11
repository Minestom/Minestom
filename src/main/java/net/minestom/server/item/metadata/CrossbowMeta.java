package net.minestom.server.item.metadata;

import net.minestom.server.item.ItemMetaView;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.CustomData;
import net.minestom.server.item.component.ItemComponent;
import net.minestom.server.item.component.ItemComponentPatch;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;

@Deprecated
public record CrossbowMeta(@NotNull ItemComponentPatch components) implements ItemMetaView<CrossbowMeta.Builder> {

    public @NotNull List<ItemStack> getProjectiles() {
        return components.get(ItemComponent.CHARGED_PROJECTILES, List.of());
    }

    public boolean isCharged() {
        return components.has(ItemComponent.CHARGED_PROJECTILES);
    }

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return components.get(ItemComponent.CUSTOM_DATA, CustomData.EMPTY).getTag(tag);
    }

    @Deprecated
    public record Builder(@NotNull ItemComponentPatch.Builder components) implements ItemMetaView.Builder {

        public Builder projectile(@NotNull ItemStack projectile) {
            components.set(ItemComponent.CHARGED_PROJECTILES, List.of(projectile));
            return this;
        }

        public Builder projectiles(@NotNull ItemStack projectile1, @NotNull ItemStack projectile2, @NotNull ItemStack projectile3) {
            components.set(ItemComponent.CHARGED_PROJECTILES, List.of(projectile1, projectile2, projectile3));
            return this;
        }

        public Builder charged(boolean charged) {
            if (charged) {
                // Only reset to empty list if we dont have any projectiles yet, as to not overwrite the call to projectiles()
                if (!components.has(ItemComponent.CHARGED_PROJECTILES))
                    components.set(ItemComponent.CHARGED_PROJECTILES, List.of());
            } else {
                components.remove(ItemComponent.CHARGED_PROJECTILES);
            }
            return this;
        }
    }
}
