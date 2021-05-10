package net.minestom.codegen;

import com.squareup.javapoet.ClassName;

public class ConstructorLambda {

    private final ClassName className;

    public ConstructorLambda(ClassName className) {
        this.className = className;
    }

    public ClassName getClassName() {
        return className;
    }
}
