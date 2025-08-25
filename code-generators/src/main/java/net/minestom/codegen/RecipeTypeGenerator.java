package net.minestom.codegen;

import java.nio.file.Path;

public final class RecipeTypeGenerator extends GenericEnumGenerator {

    public RecipeTypeGenerator(Entry entry, Path outputFolder) {
        super(entry, outputFolder);
    }

    @Override
    public String toConstant(String namespace) {
        return super.toConstant(namespace).replace("CRAFTING_", "");
    }
}
