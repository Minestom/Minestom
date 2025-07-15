package net.minestom.codegen.recipe;

import net.minestom.codegen.util.GenericEnumGenerator;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.InputStream;

public class RecipeTypeGenerator extends GenericEnumGenerator {

    public RecipeTypeGenerator(@Nullable InputStream recipeTypesFile, File outputFolder) {
        super("net.minestom.server.recipe", "RecipeType", recipeTypesFile, outputFolder);
    }

    @Override
    protected String nameGenerator(String namespaceId) {
        return toConstant(namespaceId).replace("CRAFTING_", "");
    }

}
