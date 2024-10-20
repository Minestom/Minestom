package net.minestom.server.item.component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.kyori.adventure.nbt.TagStringIOExt;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponent;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.*;

public class ItemComponentReadWriteTest {
    private static final Gson GSON = new Gson();

    private static final BinaryTagSerializer.Context CONTEXT;

    static {
        MinecraftServer.init();
        CONTEXT = new BinaryTagSerializer.ContextWithRegistries(MinecraftServer.process());
    }

    // This test will go through all of the default components present on vanilla items and make sure that we are
    // capable of reading/writing them correctly. This will help to find cases where fields have changed in case
    // they are otherwise missed.
    // Notably this does not test every component because they are not all used in vanilla, let alone on default items.
    //
    // Additional entries can be added by appending them to the following list:
    private static final Map<String, String> EXTRA_CASES = Map.ofEntries(
            entry("minecraft:glider", "{}")
    );

    @Test
    public void testReadWrite() throws IOException {
        var componentEntries = new ArrayList<>(EXTRA_CASES.entrySet());
        try (InputStream is = ItemComponentReadWriteTest.class.getResourceAsStream("/items.json")) {
            Check.notNull(is, "items.json not found");

            var object = GSON.fromJson(new InputStreamReader(is), JsonObject.class);
            for (var itemEntry : object.entrySet()) {
                for (var componentEntry : itemEntry.getValue().getAsJsonObject().getAsJsonObject("components").entrySet()) {
                    componentEntries.add(entry(componentEntry.getKey(), componentEntry.getValue().getAsString()));
                }
            }
        }

        assertAll(componentEntries.stream().map(entry -> () -> {
            var component = ItemComponent.fromNamespaceId(entry.getKey());
            assertNotNull(component, "Component not found: " + entry.getKey());
            //noinspection unchecked
            readWriteTestImpl((DataComponent<Object>) component, entry.getValue());
        }));
    }

    private static void readWriteTestImpl(@NotNull DataComponent<Object> component, @NotNull String input) {
        if (Objects.equals(ItemComponent.DAMAGE_RESISTANT, component) || Objects.equals(ItemComponent.CONSUMABLE, component) || Objects.equals(ItemComponent.DEATH_PROTECTION, component))
            return;
        try {
            var nbt = TagStringIOExt.readTag(input);
            var value = component.read(CONTEXT, nbt);
            var actual = component.write(CONTEXT, value);

            // Need to rewrite because adventure formats slightly different from vanilla.
            var expected = nbt;
            assertEquals(expected, actual, () -> "\n--- " + component.name() +" (NBT) ---\n" +
                    "EXP: " + TagStringIOExt.writeTag(expected) + "\n" +
                    "ACT: " + TagStringIOExt.writeTag(actual));

            if (component.isSynced()) {
                try {
                    var buffer = NetworkBuffer.resizableBuffer(MinecraftServer.process());
                    component.write(buffer, value);
                    var comp2 = component.read(buffer);
                    var expected2 = component.write(CONTEXT, comp2);
                    assertEquals(expected2, actual, () -> "\n--- " + component.name() +" (NETWORK) ---\n" +
                            "EXP: " + TagStringIOExt.writeTag(expected2) + "\n" +
                            "ACT: " + TagStringIOExt.writeTag(actual));
                } catch (UnsupportedOperationException ignored) {
                    //todo
                }
            }
        } catch (Exception e) {
            throw new AssertionError(component.name() + " failed on \"" + input + "\"", e);
        }

    }
}

