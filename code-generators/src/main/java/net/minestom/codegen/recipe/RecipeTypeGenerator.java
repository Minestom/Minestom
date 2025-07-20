package net.minestom.codegen.recipe;

import net.minestom.codegen.Generators;
import net.minestom.codegen.util.GenericEnumGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;

public class RecipeTypeGenerator extends GenericEnumGenerator {

    public RecipeTypeGenerator(@NotNull String packageName, @NotNull String className, @Nullable InputStream entriesFile, @NotNull File outputFolder) {
        super(packageName, className, entriesFile, outputFolder);
    }

    public RecipeTypeGenerator(Generators.@NotNull StaticEntry entry, @NotNull File outputFolder) {
        super(entry, outputFolder);
    }

    @Override
    protected @NotNull String nameGenerator(@NotNull String namespaceId) {
        return toConstant(namespaceId).replace("CRAFTING_", "");
    }

}
