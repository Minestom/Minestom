import org.jspecify.annotations.NullMarked;

@NullMarked
module net.minestom.codegen {
    requires static org.jspecify;
    requires static org.jetbrains.annotations; // TODO Remove when JSpecify is mature
    requires org.slf4j;
    requires com.google.gson;
    requires com.squareup.javapoet;
    requires java.compiler;

    exports net.minestom.codegen;
    exports net.minestom.codegen.color;
    exports net.minestom.codegen.particle;
    exports net.minestom.codegen.recipe;
    exports net.minestom.codegen.util;
    exports net.minestom.codegen.worldevent;
}