package net.minestom.codegen;

import javax.lang.model.element.Modifier;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public final class GenericPackagePrivateEnumGenerator extends GenericEnumGenerator {
    public GenericPackagePrivateEnumGenerator(Path outputFolder) {
        super(outputFolder);
    }

    @Override
    protected Collection<Modifier> modifiers() {
        return List.of();
    }
}
