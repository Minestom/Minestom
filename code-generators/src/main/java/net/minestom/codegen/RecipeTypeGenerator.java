package net.minestom.codegen;

public final class RecipeTypeGenerator extends GenericEnumGenerator {

    @Override
    public String toConstant(String namespace) {
        return super.toConstant(namespace).replace("CRAFTING_", "");
    }
}
