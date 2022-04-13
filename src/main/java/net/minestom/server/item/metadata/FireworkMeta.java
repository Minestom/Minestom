package net.minestom.server.item.metadata;

import net.minestom.server.item.ItemMetaView;
import net.minestom.server.item.firework.FireworkEffect;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.tag.TagReadable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.List;

public record FireworkMeta(TagReadable readable) implements ItemMetaView<FireworkMeta.Builder> {
    private static final Tag<List<FireworkEffect>> EFFECTS = Tag.NBT("Explosions").path("Fireworks")
            .map(nbt -> FireworkEffect.fromCompound((NBTCompound) nbt), FireworkEffect::asCompound)
            .list().defaultValue(List.of());
    private static final Tag<Byte> FLIGHT_DURATION = Tag.Byte("Flight").path("Fireworks");

    public List<FireworkEffect> getEffects() {
        return getTag(EFFECTS);
    }

    public byte getFlightDuration() {
        return getTag(FLIGHT_DURATION);
    }

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return readable.getTag(tag);
    }

    public record Builder(TagHandler tagHandler) implements ItemMetaView.Builder {
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
