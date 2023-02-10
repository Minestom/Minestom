package net.minestom.server.item.metadata;

import net.minestom.server.item.ItemMetaView;
import net.minestom.server.item.firework.FireworkEffect;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.tag.TagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;

public record FireworkMeta(TagReadable readable) implements ItemMetaView<FireworkMeta.Builder> {
    private static final Tag<List<FireworkEffect>> EFFECTS = Tag.Structure("Explosions",
                    TagSerializer.fromCompound(FireworkEffect::fromCompound, FireworkEffect::asCompound))
            .path("Fireworks").list().defaultValue(List.of());
    private static final Tag<Byte> FLIGHT_DURATION = Tag.Byte("Flight").path("Fireworks");

    public @NotNull List<FireworkEffect> getEffects() {
        return getTag(EFFECTS);
    }

    public @Nullable Byte getFlightDuration() {
        return getTag(FLIGHT_DURATION);
    }

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return readable.getTag(tag);
    }

    public record Builder(TagHandler tagHandler) implements ItemMetaView.Builder {
        public Builder() {
            this(TagHandler.newHandler());
        }

        public Builder effects(List<FireworkEffect> effects) {
            setTag(EFFECTS, effects);
            return this;
        }

        public Builder flightDuration(byte flightDuration) {
            setTag(FLIGHT_DURATION, flightDuration);
            return this;
        }
    }
}
