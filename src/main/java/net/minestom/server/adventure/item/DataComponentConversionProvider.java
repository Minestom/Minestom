package net.minestom.server.adventure.item;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.text.event.DataComponentValueConverterRegistry;
import net.kyori.adventure.text.serializer.nbt.NBTDataComponentValue;
import net.minestom.server.component.DataComponent;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Represents a {@linkplain DataComponentValueConverterRegistry.Provider data component value converter provider},
 * which converts {@linkplain MinestomDataComponentValue minestom data component value} amd
 * {@linkplain NBTDataComponentValue NBT data component value} to each other.
 */
public final class DataComponentConversionProvider implements DataComponentValueConverterRegistry.Provider {

    private static final Key KEY = Key.key("minestom", "data-component-conversion-provider");

    private static final Set<DataComponentValueConverterRegistry.Conversion<?, ?>> CONVERSIONS = Set.of(
            DataComponentValueConverterRegistry.Conversion.convert(
                    MinestomDataComponentValue.class, NBTDataComponentValue.class,
                    DataComponentConversionProvider::convertToNbt
            ),
            DataComponentValueConverterRegistry.Conversion.convert(
                    NBTDataComponentValue.class, MinestomDataComponentValue.class,
                    DataComponentConversionProvider::convertFromNbt
            )
    );

    @Override
    public @NotNull Key id() {
        return KEY;
    }

    @Override
    public @NotNull Iterable<DataComponentValueConverterRegistry.Conversion<?, ?>> conversions() {
        return CONVERSIONS;
    }

    private static <T> @NotNull NBTDataComponentValue convertToNbt(@NotNull Key key,
                                                                   @NotNull MinestomDataComponentValue<T> value) {
        DataComponent<T> component = value.component();

        if (!component.namespace().equals(key)) {
            throw new IllegalArgumentException("A key of component value and a key of data component are not equal");
        }

        BinaryTag tag = component.write(BinaryTagSerializer.Context.EMPTY, value.value());
        return NBTDataComponentValue.nbtDataComponentValue(tag);
    }

    private static @NotNull MinestomDataComponentValue<?> convertFromNbt(@NotNull Key key,
                                                                         @NotNull NBTDataComponentValue value) {
        String componentKey = key.asString();
        DataComponent<?> component = ItemComponent.fromNamespaceId(componentKey);

        if (component == null) {
            throw new IllegalArgumentException("Could not find an item component with key of: " + componentKey);
        }

        return createValue(component, value.binaryTag());
    }

    private static <T> @NotNull MinestomDataComponentValue<T> createValue(@NotNull DataComponent<T> dataComponent,
                                                                          @NotNull BinaryTag binaryTag) {
        T componentValue = dataComponent.read(BinaryTagSerializer.Context.EMPTY, binaryTag);
        return new MinestomDataComponentValue<>(dataComponent, componentValue);
    }
}