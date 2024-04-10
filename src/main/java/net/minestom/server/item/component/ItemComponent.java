package net.minestom.server.item.component;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

public sealed interface ItemComponent permits CustomData {

    ItemComponent.Type<CustomData> CUSTOM_DATA = new ItemComponent.Type<>("custom_data", CustomData.NETWORK_TYPE, CustomData.TAG);

    record Type<T extends ItemComponent>(
            @NotNull String name,
            @NotNull NetworkBuffer.Type<T> network,
            @NotNull Tag<T> tag
    ) {

    }


}
