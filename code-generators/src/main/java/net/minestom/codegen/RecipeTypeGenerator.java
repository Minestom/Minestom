package net.minestom.codegen;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.nio.file.Path;

public final class RecipeTypeGenerator extends GenericEnumGenerator {
    public RecipeTypeGenerator(@Nullable InputStream recipeTypesFile, @NotNull Path outputFolder) {
        super("net.minestom.server.recipe", "RecipeType", recipeTypesFile, outputFolder);
    }

    @Override
    public String toConstant(@NotNull String namespace) {
        return super.toConstant(namespace).replace("CRAFTING_", "");
    }
}
