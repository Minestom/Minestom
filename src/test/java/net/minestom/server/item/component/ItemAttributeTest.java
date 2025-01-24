package net.minestom.server.item.component;

import net.minestom.server.component.DataComponent;
import net.minestom.server.entity.EquipmentSlotGroup;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.item.ItemComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class ItemAttributeTest extends AbstractItemComponentTest<AttributeList> {
    @Override
    protected @NotNull DataComponent<AttributeList> component() {
        return ItemComponent.ATTRIBUTE_MODIFIERS;
    }

    @Override
    protected @NotNull List<Map.Entry<String, AttributeList>> directReadWriteEntries() {
        return List.of(
                Map.entry("empty", AttributeList.EMPTY),
                Map.entry("single", new AttributeList(new AttributeList.Modifier(Attribute.MOVEMENT_SPEED, new AttributeModifier("minestom:movement_test", 0.1, AttributeOperation.ADD_VALUE), EquipmentSlotGroup.MAIN_HAND))),
                Map.entry("multiple", new AttributeList(List.of(
                        new AttributeList.Modifier(Attribute.MAX_HEALTH, new AttributeModifier("minestom:health_test", 5, AttributeOperation.ADD_VALUE), EquipmentSlotGroup.MAIN_HAND),
                        new AttributeList.Modifier(Attribute.ATTACK_DAMAGE, new AttributeModifier("minestom:attack_test", 3, AttributeOperation.ADD_VALUE), EquipmentSlotGroup.ANY),
                        new AttributeList.Modifier(Attribute.ATTACK_DAMAGE, new AttributeModifier("minestom:attack_test_1", 1.4, AttributeOperation.MULTIPLY_BASE), EquipmentSlotGroup.CHEST)

                )))
        );
    }
}
