package net.minestom.server.item.component;

import net.minestom.server.component.DataComponent;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.attribute.AttributeSlot;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ItemAttributeTest extends AbstractItemComponentTest<AttributeList> {
    @Override
    protected @NotNull DataComponent<AttributeList> component() {
        return ItemComponent.ATTRIBUTE_MODIFIERS;
    }

    @Override
    protected @NotNull List<Map.Entry<String, AttributeList>> directReadWriteEntries() {
        return List.of(
                Map.entry("empty", AttributeList.EMPTY),
                Map.entry("single", new AttributeList(new AttributeList.Modifier(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID.randomUUID(), "MovementTest", 0.1, AttributeOperation.ADD_VALUE), AttributeSlot.MAINHAND))),
                Map.entry("multiple", new AttributeList(List.of(
                        new AttributeList.Modifier(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(UUID.randomUUID(), "HealthTest", 5, AttributeOperation.ADD_VALUE), AttributeSlot.MAINHAND),
                        new AttributeList.Modifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(), "AttackTest", 3, AttributeOperation.ADD_VALUE), AttributeSlot.ANY),
                        new AttributeList.Modifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(), "AttackTest1", 1.4, AttributeOperation.MULTIPLY_BASE), AttributeSlot.CHEST)

                )))
        );
    }
}
