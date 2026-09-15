package net.minestom.server.item.component;

import net.kyori.adventure.key.Key;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.loot.number.ResolvableFloat;
import net.minestom.server.loot.number.ResolvableInt;

import java.util.List;
import java.util.Map;

public class BrewingFuelTest extends AbstractItemComponentRegistriesTest<BrewingFuel> {

    @Override
    protected DataComponent<BrewingFuel> component() {
        return DataComponents.BREWING_FUEL;
    }

    @Override
    protected List<Map.Entry<String, BrewingFuel>> directReadWriteEntries() {
        return List.of(
                Map.entry("constant", new BrewingFuel(20, 1.0f)),
                Map.entry("reference", new BrewingFuel(ResolvableInt.of(Key.key("minecraft:brewing/uses_default")),
                        ResolvableFloat.of(Key.key("minecraft:brewing/speed_default")))),
                Map.entry("constant_uses", new BrewingFuel(ResolvableInt.of(20), ResolvableFloat.of(Key.key("minecraft:brewing/speed_default")))),
                Map.entry("constant_speed", new BrewingFuel(ResolvableInt.of(Key.key("minecraft:brewing/uses_default")), ResolvableFloat.of(1.0f)))
        );
    }
}
