package net.minestom.server.adventure.provider;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.event.DataComponentValueConverterRegistry;
import net.kyori.adventure.text.serializer.gson.GsonDataComponentValue;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.MinestomDataComponentValue;
import net.minestom.server.adventure.serializer.nbt.NbtDataComponentValue;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.component.DataComponent;
import net.minestom.server.registry.RegistryTranscoder;

import java.util.List;

import static net.kyori.adventure.text.event.DataComponentValueConverterRegistry.Conversion.convert;

public class MinestomDataComponentValueConverterProvider implements DataComponentValueConverterRegistry.Provider {

    @Override
    public Key id() {
        return Key.key("minestom", "data_component_value_converter");
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterable<DataComponentValueConverterRegistry.Conversion<?, ?>> conversions() {
        return List.of(
                // GSON
                convert(GsonDataComponentValue.class, MinestomDataComponentValue.class, (key, gsonValue) -> {
                    if (gsonValue instanceof DataComponentValue.Removed)
                        return MinestomDataComponentValue.removed();
                    final DataComponent<Object> component = (DataComponent<Object>) DataComponent.fromKey(key);
                    if (component == null) throw new IllegalArgumentException("Unknown data component: " + key);
                    final Object value = component.decode(new RegistryTranscoder<>(Transcoder.JSON,
                            MinecraftServer.process()), gsonValue.element()).orElseThrow("failed to decode " + key);
                    return MinestomDataComponentValue.dataComponentValue(value);
                }),
                convert(MinestomDataComponentValue.class, GsonDataComponentValue.class, (key, minestomValue) -> {
                    if (minestomValue instanceof DataComponentValue.Removed)
                        return GsonDataComponentValue.gsonDataComponentValue(JsonNull.INSTANCE);
                    final DataComponent<Object> component = (DataComponent<Object>) DataComponent.fromKey(key);
                    if (component == null) throw new IllegalArgumentException("Unknown data component: " + key);
                    final JsonElement value = component.encode(new RegistryTranscoder<>(Transcoder.JSON,
                            MinecraftServer.process()), minestomValue.value()).orElseThrow("failed to encode " + key);
                    return GsonDataComponentValue.gsonDataComponentValue(value);
                }),

                // NBT
                convert(NbtDataComponentValue.class, MinestomDataComponentValue.class, (key, nbtValue) -> {
                    if (nbtValue instanceof DataComponentValue.Removed)
                        return MinestomDataComponentValue.removed();
                    final DataComponent<Object> component = (DataComponent<Object>) DataComponent.fromKey(key);
                    if (component == null) throw new IllegalArgumentException("Unknown data component: " + key);
                    final Object value = component.decode(new RegistryTranscoder<>(Transcoder.NBT,
                            MinecraftServer.process()), nbtValue.value()).orElseThrow("failed to decode " + key);
                    return MinestomDataComponentValue.dataComponentValue(value);
                }),
                convert(MinestomDataComponentValue.class, NbtDataComponentValue.class, (key, minestomValue) -> {
                    if (minestomValue instanceof DataComponentValue.Removed)
                        return NbtDataComponentValue.removed();
                    final DataComponent<Object> component = (DataComponent<Object>) DataComponent.fromKey(key);
                    if (component == null) throw new IllegalArgumentException("Unknown data component: " + key);
                    final BinaryTag value = component.encode(new RegistryTranscoder<>(Transcoder.NBT,
                            MinecraftServer.process()), minestomValue.value()).orElseThrow("failed to encode " + key);
                    return NbtDataComponentValue.nbtDataComponentValue(value);
                })
        );
    }

}
