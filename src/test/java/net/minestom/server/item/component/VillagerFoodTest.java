package net.minestom.server.item.component;

import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class VillagerFoodTest extends AbstractItemComponentRegistriesTest<VillagerFood> {

    @Override
    protected DataComponent<VillagerFood> component() {
        return DataComponents.VILLAGER_FOOD;
    }

    @Override
    protected List<Map.Entry<String, VillagerFood>> directReadWriteEntries() {
        return List.of(
                Map.entry("one", new VillagerFood(1)),
                Map.entry("four", new VillagerFood(4))
        );
    }

    @Test
    public void nutritionMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new VillagerFood(0));
        assertThrows(IllegalArgumentException.class, () -> new VillagerFood(-1));
    }
}
