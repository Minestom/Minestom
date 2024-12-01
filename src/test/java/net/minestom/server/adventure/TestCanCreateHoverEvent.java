package net.minestom.server.adventure;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestCanCreateHoverEvent {

    @Test
    public void createHoverEvent() {
        ItemStack itemStack = ItemStack.of(Material.GOLD_INGOT).with(ItemComponent.CUSTOM_NAME, Component.text("Gold!", NamedTextColor.GOLD));
        HoverEvent<HoverEvent.ShowItem> hoverEvent = itemStack.asHoverEvent();
        assertEquals(Key.key("minecraft", "gold_ingot"), hoverEvent.value().item());
        assertEquals(1, hoverEvent.value().count());
        assertFalse(hoverEvent.value().dataComponents().isEmpty());
        assertTrue(hoverEvent.value().dataComponents().containsKey(ItemComponent.CUSTOM_NAME.key()));
    }
}
