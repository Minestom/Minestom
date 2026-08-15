module net.minestom.codegen {
    requires org.jetbrains.annotations; // TODO Remove when JSpecify is mature
    requires com.google.gson;
    requires com.palantir.javapoet;
    requires java.compiler;
    requires net.minestom.data;

    exports net.minestom.codegen;
}