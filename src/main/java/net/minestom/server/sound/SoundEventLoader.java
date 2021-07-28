package net.minestom.server.sound;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
final class SoundEventLoader {

    // Maps do not need to be thread-safe as they are fully populated
    // in the static initializer, should not be modified during runtime

    // Block namespace -> registry data
    private static final Map<String, SoundEvent> NAMESPACE_MAP = new HashMap<>();
    // Block id -> registry data
    private static final Int2ObjectMap<SoundEvent> ID_MAP = new Int2ObjectOpenHashMap<>();

    static SoundEvent get(@NotNull String namespace) {
        if (namespace.indexOf(':') == -1) {
            // Default to minecraft namespace
            namespace = "minecraft:" + namespace;
        }
        return NAMESPACE_MAP.get(namespace);
    }

    static SoundEvent getId(int id) {
        return ID_MAP.get(id);
    }

    static Collection<SoundEvent> values() {
        return Collections.unmodifiableCollection(NAMESPACE_MAP.values());
    }

    static {
        // Load data from file
        JsonObject sounds = Registry.load(Registry.Resource.SOUNDS);
        sounds.entrySet().forEach(entry -> {
            final String namespace = entry.getKey();
            final JsonObject object = entry.getValue().getAsJsonObject();
            final int id = object.get("id").getAsInt();

            final var soundEvent = new SoundEventImpl(NamespaceID.from(namespace), id);
            ID_MAP.put(id, soundEvent);
            NAMESPACE_MAP.put(namespace, soundEvent);
        });
    }
}
