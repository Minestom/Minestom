package net.minestom.server.raw_data;

import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class RawFluidData {
    public final @NotNull Supplier<@NotNull Material> bucketItem;

    public RawFluidData(@NotNull Supplier<@NotNull Material> bucketItem) {
        this.bucketItem = bucketItem;
    }

    @NotNull
    public Material getBucketItem() {
        return bucketItem.get();
    }
}