package net.minestom.server.item.component;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import net.minestom.data.MinestomData;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.component.DataComponent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryTranscoder;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import static java.util.Map.entry;
import static net.minestom.server.codec.CodecAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class ItemComponentReadWriteIntegrationTest {
    private static final Gson GSON = new Gson();

    // This test will go through all of the default components present on vanilla items and make sure that we are
    // capable of reading/writing them correctly. This will help to find cases where fields have changed in case
    // they are otherwise missed.
    // Notably this does not test every component because they are not all used in vanilla, let alone on default items.
    //
    // Additional entries can be added by appending them to the following list:
    private static final Map<String, JsonElement> EXTRA_CASES = Map.ofEntries(
            entry("minecraft:glider", new JsonObject())
    );

    @Test
    @SuppressWarnings("unchecked")
    public void testReadWrite(Env env) throws IOException {
        var componentEntries = new ArrayList<>(EXTRA_CASES.entrySet());
        try (InputStream is = MinestomData.resource("item.json")) {
            Objects.requireNonNull(is, "item.json not found");

            var object = GSON.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), JsonObject.class);
            for (var itemEntry : object.entrySet()) {
                for (var componentEntry : itemEntry.getValue().getAsJsonObject().getAsJsonObject("components").entrySet()) {
                    componentEntries.add(entry(componentEntry.getKey(), componentEntry.getValue()));
                }
            }
        }

        assertAll(componentEntries.stream().map(entry -> () -> {
            var component = DataComponent.fromKey(entry.getKey());
            assertNotNull(component, "Component not found: " + entry.getKey());
            readWriteTestImpl((DataComponent<Object>) component, entry.getValue(), env.process());
        }));
    }

    private static void readWriteTestImpl(DataComponent<Object> component, JsonElement input, Registries registries) {
        try {
            var transcoder = new RegistryTranscoder<>(Transcoder.JSON, registries);
            var value = assertOk(component.decode(transcoder, input));
            var actual = assertOk(component.encode(transcoder, value));
            // This is pretty cursed but we need to serialize and reparse because the JsonPrimitive number implementation changes
            // When reading from a string it has LazilyParsedNumber which is NOT equal to `new JsonPrimitive(1)` for example.
            var actualParsed = GSON.fromJson(actual.toString(), JsonElement.class);
            var inputParsed = GSON.fromJson(input.toString(), JsonElement.class);

            //TODO(26.1) see If this is a problem.
            if (actualParsed.isJsonObject() && actualParsed.getAsJsonObject().has("count"))
                actualParsed.getAsJsonObject().remove("count");

            // Need to rewrite because adventure formats slightly different from vanilla.
            assertEquals(inputParsed, actualParsed, () -> "\n--- " + component.name() + " (NBT) ---\n" +
                    "EXP: " + input + "\n" +
                    "ACT: " + actualParsed);

            if (component.isSynced()) {
                var buffer = NetworkBuffer.resizableBuffer(registries);
                component.write(buffer, value);
                var comp2 = component.read(buffer);
                var expected2 = assertOk(component.encode(transcoder, comp2));
                assertEquals(expected2, actual, () -> "\n--- " + component.name() + " (NETWORK) ---\n" +
                        "EXP: " + expected2 + "\n" +
                        "ACT: " + actual);
            }
        } catch (AssertionError | Exception e) {
            throw new AssertionError(component.name() + " failed on \"" + input + "\"", e);
        }
    }

}

