package net.minestom.server.item.metadata;

import net.minestom.server.item.ItemMetaView;
import net.minestom.server.item.component.*;
import net.minestom.server.item.firework.FireworkEffect;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@Deprecated
public record FireworkEffectMeta(@NotNull ItemComponentPatch components) implements ItemMetaView<FireworkEffectMeta.Builder> {
    public @Nullable FireworkEffect getFireworkEffect() {
        FireworkExplosion explosion = components.get(ItemComponent.FIREWORK_EXPLOSION);
        return explosion == null ? null : new FireworkEffect(explosion);
    }

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return components.get(ItemComponent.CUSTOM_DATA, CustomData.EMPTY).getTag(tag);
    }

    @Deprecated
    public record Builder(@NotNull ItemComponentMap.Builder components) implements ItemMetaView.Builder {

        public Builder effect(@Nullable FireworkEffect fireworkEffect) {
            if (fireworkEffect == null) {
                components.remove(ItemComponent.FIREWORK_EXPLOSION);
            } else {
                components.set(ItemComponent.FIREWORK_EXPLOSION, fireworkEffect.toExplosion());
            }
            return this;
        }
    }
}
