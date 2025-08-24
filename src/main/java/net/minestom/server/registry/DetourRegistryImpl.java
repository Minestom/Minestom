package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
final class DetourRegistryImpl implements DetourRegistry {
    static final DetourRegistryImpl INSTANCE = new DetourRegistryImpl();

    private final Map<Key, Detour<?>> detours;

    DetourRegistryImpl() {
        this.detours = new HashMap<>(0);
    }

    @Override
    public <T> void register(RegistryKey<T> registryKey, Detour<T> detour) {
        this.registerKeyed(registryKey, detour);
    }

    @Override
    public <T> void register(TagKey<T> tagKey, Detour<RegistryTag.Builder<T>> detour) {
        this.registerKeyed(tagKey, detour);
    }

    @Override
    public boolean hasDetour(Keyed key) {
        Check.notNull(key, "Registry key cannot be null");
        return detours.containsKey(key.key());
    }

    @Override
    public boolean hasDetours() {
        return !detours.isEmpty();
    }

    @Override
    public <T> T consume(RegistryKey<T> registryKey, T value) {
        return this.consumeKeyed(registryKey, value);
    }

    @Override
    public <T> void consume(TagKey<T> key, RegistryTag.Builder<T> builder) {
        this.consumeKeyed(key, builder);
    }

    @SuppressWarnings("unchecked")
    public <T> void registerKeyed(Keyed keyedKey, Detour<T> detour) {
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
    private <T> T consumeKeyed(Keyed keyedKey, T value) {
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
