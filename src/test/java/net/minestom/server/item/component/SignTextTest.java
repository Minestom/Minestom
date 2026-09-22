package net.minestom.server.item.component;

import net.kyori.adventure.text.Component;
import net.minestom.server.color.DyeColor;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class SignTextTest extends AbstractItemComponentRegistriesTest<SignText> {
    private static final List<Component> LINES = List.of(Component.text("a"), Component.text("b"), Component.text("c"), Component.text("d"));

    @Override
    protected DataComponent<SignText> component() {
        return DataComponents.SIGN_TEXT_FRONT;
    }

    @Override
    protected List<Map.Entry<String, SignText>> directReadWriteEntries() {
        return List.of(
                Map.entry("plain", new SignText(LINES, null, DyeColor.BLACK, false)),
                Map.entry("filtered", new SignText(LINES, LINES, DyeColor.RED, true))
        );
    }

    @Test
    public void lineCount() {
        assertThrows(IllegalArgumentException.class, () -> new SignText(List.of(), null, DyeColor.BLACK, false));
        assertThrows(IllegalArgumentException.class, () -> new SignText(LINES, List.of(Component.text("a")), DyeColor.BLACK, false));
    }
}
