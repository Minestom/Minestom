package net.minestom.server.item.component;

import net.kyori.adventure.key.Key;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.loot.number.ResolvableInt;

import java.util.List;
import java.util.Map;

public class CompostableTest extends AbstractItemComponentRegistriesTest<Compostable> {

    @Override
    protected DataComponent<Compostable> component() {
        return DataComponents.COMPOSTABLE;
    }

    @Override
    protected List<Map.Entry<String, Compostable>> directReadWriteEntries() {
        return List.of(
                Map.entry("constant", new Compostable(1)),
                Map.entry("reference", new Compostable(ResolvableInt.of(Key.key("minecraft:compostable/low"))))
        );
    }
}
