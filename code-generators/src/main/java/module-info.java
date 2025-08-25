module net.minestom.codegen {
    requires static org.jetbrains.annotations; // TODO Remove when JSpecify is mature
    requires com.google.gson;
    requires com.palantir.javapoet;
    requires java.compiler;
    requires net.kyori.adventure.key;

    exports net.minestom.codegen;
}