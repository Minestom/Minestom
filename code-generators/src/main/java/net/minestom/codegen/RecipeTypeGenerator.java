package net.minestom.codegen;

import java.nio.file.Path;

public final class RecipeTypeGenerator extends GenericEnumGenerator {
    public RecipeTypeGenerator(Path outputFolder) {
        super(outputFolder);
    }

    @Override
    public String toConstant(String namespace) {
        return super.toConstant(namespace).replace("CRAFTING_", "");
    }
}
