package net.minestom.server.item.metadata;

import net.minestom.server.item.ItemMetaView;
import net.minestom.server.item.component.CustomData;
import net.minestom.server.item.component.FireworkList;
import net.minestom.server.item.component.ItemComponent;
import net.minestom.server.item.component.ItemComponentPatch;
import net.minestom.server.item.firework.FireworkEffect;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;

@Deprecated
public record FireworkMeta(@NotNull ItemComponentPatch components) implements ItemMetaView<FireworkMeta.Builder> {

    public @NotNull List<FireworkEffect> getEffects() {
        FireworkList value = components.get(ItemComponent.FIREWORKS);
        return value == null ? List.of() : value.explosions().stream().map(FireworkEffect::new).toList();
    }

    public @Nullable Byte getFlightDuration() {
        FireworkList value = components.get(ItemComponent.FIREWORKS);
        return value == null ? null : value.flightDuration();
    }

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return components.get(ItemComponent.CUSTOM_DATA, CustomData.EMPTY).getTag(tag);
    }

    @Deprecated
    public record Builder(@NotNull ItemComponentPatch.Builder components) implements ItemMetaView.Builder {

        public Builder effects(List<FireworkEffect> effects) {
            FireworkList value = components.get(ItemComponent.FIREWORKS, FireworkList.EMPTY);
            components.set(ItemComponent.FIREWORKS, value.withExplosions(effects.stream().map(FireworkEffect::toExplosion).toList()));
            return this;
        }

        public Builder flightDuration(byte flightDuration) {
            FireworkList value = components.get(ItemComponent.FIREWORKS, FireworkList.EMPTY);
            components.set(ItemComponent.FIREWORKS, value.withFlightDuration(flightDuration));
            return this;
        }
    }
}
