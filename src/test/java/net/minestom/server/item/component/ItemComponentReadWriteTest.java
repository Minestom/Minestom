package net.minestom.server.item.component;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.MinecraftServer;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.component.DataComponent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.RegistryTranscoder;
import net.minestom.server.utils.validate.Check;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

import static java.util.Map.entry;
import static net.minestom.server.codec.CodecAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.*;

public class ItemComponentReadWriteTest {
    private static final Gson GSON = new Gson();

    private static final Transcoder<JsonElement> CODER;

    static {
        MinecraftServer.init();
        CODER = new RegistryTranscoder<>(Transcoder.JSON, MinecraftServer.process());
    }

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
    public void testReadWrite() throws IOException {
        var componentEntries = new ArrayList<>(EXTRA_CASES.entrySet());
        try (InputStream is = ItemComponentReadWriteTest.class.getResourceAsStream("/item.json")) {
            Check.notNull(is, "items.json not found");

            var object = GSON.fromJson(new InputStreamReader(is), JsonObject.class);
            for (var itemEntry : object.entrySet()) {
                for (var componentEntry : itemEntry.getValue().getAsJsonObject().getAsJsonObject("components").entrySet()) {
                    componentEntries.add(entry(componentEntry.getKey(), componentEntry.getValue()));
                }
            }
        }

        assertAll(componentEntries.stream().map(entry -> () -> {
            var component = DataComponent.fromKey(entry.getKey());
            assertNotNull(component, "Component not found: " + entry.getKey());
            //noinspection unchecked
            readWriteTestImpl((DataComponent<Object>) component, entry.getValue());
        }));
    }

    private static void readWriteTestImpl(DataComponent<Object> component, JsonElement input) {
        try {
            var value = assertOk(component.decode(CODER, input));
            var actual = assertOk(component.encode(CODER, value));
            // This is pretty cursed but we need to serialize and reparse because the JsonPrimitive number implementation changes
            // When reading from a string it has LazilyParsedNumber which is NOT equal to `new JsonPrimitive(1)` for example.
            var actualParsed = GSON.fromJson(actual.toString(), JsonElement.class);
            var inputParsed = GSON.fromJson(input.toString(), JsonElement.class);

            // Need to rewrite because adventure formats slightly different from vanilla.
            assertEquals(inputParsed, actualParsed, () -> "\n--- " + component.name() + " (NBT) ---\n" +
                    "EXP: " + input + "\n" +
                    "ACT: " + actualParsed.toString());

            if (component.isSynced()) {
                var buffer = NetworkBuffer.resizableBuffer(MinecraftServer.process());
                component.write(buffer, value);
                var comp2 = component.read(buffer);
                var expected2 = assertOk(component.encode(CODER, comp2));
                assertEquals(expected2, actual, () -> "\n--- " + component.name() + " (NETWORK) ---\n" +
                        "EXP: " + expected2 + "\n" +
                        "ACT: " + actual);
            }
        } catch (AssertionError | Exception e) {
            throw new AssertionError(component.name() + " failed on \"" + input + "\"", e);
        }
    }

    private static void assertEqualsJson(JsonElement expected, JsonElement actual) {

    }
}

