package net.minestom.codegen.recipe;

import net.minestom.codegen.util.GenericEnumGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;

public class RecipeTypeGenerator extends GenericEnumGenerator {

    public RecipeTypeGenerator(@Nullable InputStream recipeTypesFile, @NotNull File outputFolder) {
        super("net.minestom.server.recipe", "RecipeType", recipeTypesFile, outputFolder);
    }

    @Override
    protected @NotNull String nameGenerator(@NotNull String namespaceId) {
        return toConstant(namespaceId).replace("CRAFTING_", "");
    }

}
