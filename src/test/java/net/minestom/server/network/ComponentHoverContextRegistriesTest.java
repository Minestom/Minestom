package net.minestom.server.network;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.minestom.server.adventure.serializer.nbt.NbtDataComponentValue;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryKey;
import net.minestom.testing.RegistriesTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RegistriesTest
class ComponentHoverContextRegistriesTest {

    @Test
    void showItemUsesBufferRegistryContext(Registries registries) {
        final Registries detached = Registries.vanilla();
        final RegistryKey<DamageType> customKey = detached.damageType().register("minestom:hover_damage",
                DamageType.builder()
                        .messageId("hover_damage")
                        .scaling("never")
                        .build());
        assertNull(registries.damageType().get(customKey));

        final ItemStack item = ItemStack.of(Material.STONE).with(DataComponents.DAMAGE_TYPE, customKey);
        final Component component = Component.text("item").hoverEvent(item.asHoverEvent());
        final NetworkBuffer buffer = NetworkBuffer.resizableBuffer(256, detached);
        buffer.write(NetworkBuffer.COMPONENT, component);
        buffer.readIndex(0);

        final CompoundBinaryTag root = (CompoundBinaryTag) buffer.read(NetworkBuffer.NBT);
        final CompoundBinaryTag hoverEvent = (CompoundBinaryTag) root.get("hover_event");
        final CompoundBinaryTag components = (CompoundBinaryTag) hoverEvent.get("components");
        assertEquals(StringBinaryTag.stringBinaryTag(customKey.key().asString()),
                components.get(DataComponents.DAMAGE_TYPE.key().asString()));
    }

    @Test
    void typedShowItemComponentRequiresBufferRegistryContext() {
        final ItemStack item = ItemStack.of(Material.STONE)
                .with(DataComponents.DAMAGE_TYPE, DamageType.GENERIC);
        final Component component = Component.text("item").hoverEvent(item.asHoverEvent());
        final NetworkBuffer buffer = NetworkBuffer.resizableBuffer(256);

        assertThrows(NullPointerException.class, () -> buffer.write(NetworkBuffer.COMPONENT, component));
    }

    @Test
    void preEncodedShowItemComponentDoesNotRequireBufferRegistryContext() {
        final HoverEvent.ShowItem item = HoverEvent.ShowItem.showItem(
                Material.STONE, 1,
                Map.of(
                        DataComponents.DAMAGE_TYPE.key(),
                        NbtDataComponentValue.nbtDataComponentValue(
                                StringBinaryTag.stringBinaryTag(DamageType.GENERIC.key().asString()))));
        final Component component = Component.text("item").hoverEvent(HoverEvent.showItem(item));
        final NetworkBuffer buffer = NetworkBuffer.resizableBuffer(256);

        buffer.write(NetworkBuffer.COMPONENT, component);
    }
}
