module net.minestom.codegen {
    requires static org.jetbrains.annotations; // TODO Remove when JSpecify is mature
    requires org.slf4j;
    requires com.google.gson;
    requires com.palantir.javapoet;
    requires java.compiler;

    exports net.minestom.codegen;
}