package net.minestom.server.item.component;

import net.kyori.adventure.key.Key;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.loot.number.ResolvableFloat;
import net.minestom.server.loot.number.ResolvableInt;

import java.util.List;
import java.util.Map;

public class CookingFuelTest extends AbstractItemComponentRegistriesTest<CookingFuel> {

    @Override
    protected DataComponent<CookingFuel> component() {
        return DataComponents.COOKING_FUEL;
    }

    @Override
    protected List<Map.Entry<String, CookingFuel>> directReadWriteEntries() {
        return List.of(
                Map.entry("constant", new CookingFuel(1600, 1.5f)),
                Map.entry("reference", new CookingFuel(ResolvableInt.of(Key.key("minecraft:cooking/time_coal")),
                        ResolvableFloat.of(Key.key("minecraft:cooking/speed_default")))),
                Map.entry("constant_burn_time", new CookingFuel(ResolvableInt.of(1600), ResolvableFloat.of(Key.key("minecraft:cooking/speed_default")))),
                Map.entry("constant_speed", new CookingFuel(ResolvableInt.of(Key.key("minecraft:cooking/time_coal")), ResolvableFloat.of(1.5f)))
        );
    }
}
