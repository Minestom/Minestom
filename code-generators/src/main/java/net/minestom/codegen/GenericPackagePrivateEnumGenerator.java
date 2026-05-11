package net.minestom.codegen;

import javax.lang.model.element.Modifier;
import java.util.Collection;
import java.util.List;

public final class GenericPackagePrivateEnumGenerator extends GenericEnumGenerator {
    @Override
    protected Collection<Modifier> modifiers() {
        return List.of();
    }
}
