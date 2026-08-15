package net.minestom.server.recipe;

import net.minestom.server.item.Material;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.utils.validate.Check;

import java.util.List;

public record Ingredient(RegistryTag<Material> tag) {
    public static final NetworkBuffer.Type<Ingredient> NETWORK_TYPE = NetworkBufferTemplate.template(
            RegistryTag.networkType(Registries::material), Ingredient::tag,
            Ingredient::new
    );

    public Ingredient(Material... items) {
        this(List.of(items));
    }

    public Ingredient(List<Material> items) {
        Check.argCondition(items.isEmpty(), "Ingredients can't be empty");
        Check.argCondition(items.contains(Material.AIR), "Ingredient can't contain air");
        this(RegistryTag.direct(items.stream().map(Material::registryKey).toList()));
    }
}
