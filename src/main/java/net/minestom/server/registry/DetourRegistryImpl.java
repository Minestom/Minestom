package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class DetourRegistryImpl implements DetourRegistry {
    private final Map<Key, Detour<?>> detours;

    DetourRegistryImpl() {
        detours = new ConcurrentHashMap<>();
    }

    @Override
    public <T> void register(@NotNull RegistryKey<T> registryKey, @NotNull Detour<T> detour) {
        this.registerKeyed(registryKey, detour);
    }

    @Override
    public <T> void registerTag(@NotNull TagKey<T> key, @NotNull Detour<RegistryTag.Builder<T>> detour) {
        this.registerTag(key, detour);
    }

    @Override
    public boolean hasDetour(@NotNull Keyed key) {
        Check.notNull(key, "Registry key cannot be null");
        return detours.containsKey(key.key());
    }

    @Override
    public boolean hasDetours() {
        return !detours.isEmpty();
    }

    @Override
    public <T> @NotNull T consume(@NotNull RegistryKey<T> registryKey, @NotNull T value) {
        return this.consumeKeyed(registryKey, value);
    }

    @Override
    public <T> void consumeTag(@NotNull TagKey<T> key, RegistryTag.@NotNull Builder<T> builder) {
        this.consumeKeyed(key, builder);
    }

    @SuppressWarnings("unchecked")
    public <T> void registerKeyed(@NotNull Keyed keyedKey, @NotNull Detour<T> detour) {
        Check.notNull(keyedKey, "Registry key cannot be null");
        Check.notNull(detour, "Detour cannot be null");
        final Key key = keyedKey.key();
        Detour<?> existingDetour = detours.get(key);
        if (existingDetour != null) {
            detours.put(key, ((Detour<T>) existingDetour).andThen(detour));
        } else {
            detours.put(key, detour);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T consumeKeyed(@NotNull Keyed keyedKey, @NotNull T value) {
        Check.notNull(keyedKey, "Keyed key cannot be null");
        Check.notNull(value, "Value cannot be null");
        final Key key = keyedKey.key();
        Detour<?> detour = detours.get(key);
        if (detour == null) {
            return value; // No detour registered for this key, return the original value
        }
        try {
            // Cast the detour to the appropriate type and apply it
            T finalValue = ((Detour<T>) detour).apply(value);
            Check.notNull(finalValue, "Detour returned null for key: {0}", key.asString());
            return finalValue;
        } finally {
            Check.notNull(detours.remove(key), "Detour for key {0} was consumed during usage in the registry", key.asString());
        }
    }
}
