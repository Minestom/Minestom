package net.minestom.server.item.component;

import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static net.minestom.server.codec.CodecAssertions.assertOk;

public class ComponentTest extends AbstractItemComponentTest<Component> {
    // This is not a test, but it creates a compile error if the component type is changed away from Component,
    // as a reminder that tests should be added for that new component type.
    private static final List<DataComponent<Component>> SHARED_COMPONENTS = List.of(
            DataComponents.CUSTOM_NAME,
            DataComponents.ITEM_NAME
    );

    @Override
    protected DataComponent<Component> component() {
        return SHARED_COMPONENTS.getFirst();
    }

    @Override
    protected List<Map.Entry<String, Component>> directReadWriteEntries() {
        // Component serialization is well tested elsewhere, this is just a sanity check really.
        return List.of(
                Map.entry("empty component", Component.empty()),
                Map.entry("text component", Component.text("Hello, world!"))
        );
    }

    @Test
    void testItemNameParseRegression() throws Exception {
        var nbt = MinestomAdventure.tagStringIO().asTag("{translate: \"item.minecraft.diamond\"}");
        var component = DataComponents.ITEM_NAME.decode(Transcoder.NBT, nbt);
        assertOk(component);
    }
}
