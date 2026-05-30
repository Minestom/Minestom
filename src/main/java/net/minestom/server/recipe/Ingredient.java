package net.minestom.server.recipe;

import net.minestom.server.item.Material;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryTag;

public record Ingredient(RegistryTag<Material> tag) {
    public static final NetworkBuffer.Type<Ingredient> NETWORK_TYPE = NetworkBufferTemplate.template(
            RegistryTag.networkType(Registries::material), Ingredient::tag,
            Ingredient::new
    );
}
