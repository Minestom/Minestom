package net.minestom.server.item.metadata;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.item.ItemMetaView;
import net.minestom.server.item.component.CustomData;
import net.minestom.server.item.component.ItemComponent;
import net.minestom.server.item.component.ItemComponentPatch;
import net.minestom.server.item.component.LodestoneTracker;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@Deprecated
public record CompassMeta(@NotNull ItemComponentPatch components) implements ItemMetaView<CompassMeta.Builder> {

    public boolean isLodestoneTracked() {
        LodestoneTracker tracker = components.get(ItemComponent.LODESTONE_TRACKER);
        return tracker != null && tracker.tracked();
    }

    public @Nullable String getLodestoneDimension() {
        LodestoneTracker tracker = components.get(ItemComponent.LODESTONE_TRACKER);
        return tracker == null ? null : tracker.dimension();
    }

    public @Nullable Point getLodestonePosition() {
        LodestoneTracker tracker = components.get(ItemComponent.LODESTONE_TRACKER);
        return tracker == null ? null : tracker.blockPosition();
    }

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return components.get(ItemComponent.CUSTOM_DATA, CustomData.EMPTY).getTag(tag);
    }

    @Deprecated
    public record Builder(@NotNull ItemComponentPatch.Builder components) implements ItemMetaView.Builder {
        // The empty state isnt really valid because the dimension is empty (invalid), but these functions need to set each so its simpler.
        private static final LodestoneTracker EMPTY = new LodestoneTracker("", Vec.ZERO, false);

        public Builder lodestoneTracked(boolean lodestoneTracked) {
            LodestoneTracker tracker = components.get(ItemComponent.LODESTONE_TRACKER, EMPTY);
            components.set(ItemComponent.LODESTONE_TRACKER, tracker.withTracked(lodestoneTracked));
            return this;
        }

        public Builder lodestoneDimension(@Nullable String lodestoneDimension) {
            LodestoneTracker tracker = components.get(ItemComponent.LODESTONE_TRACKER, EMPTY);
            components.set(ItemComponent.LODESTONE_TRACKER, tracker.withDimension(lodestoneDimension));
            return this;
        }

        public Builder lodestonePosition(@Nullable Point lodestonePosition) {
            LodestoneTracker tracker = components.get(ItemComponent.LODESTONE_TRACKER, EMPTY);
            components.set(ItemComponent.LODESTONE_TRACKER, tracker.withBlockPosition(lodestonePosition));
            return this;
        }
    }
}
