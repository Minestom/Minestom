package net.minestom.codegen.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;

import static net.minestom.codegen.MinestomCodeGenerator.NAMESPACE_ID_CLASS;

@ApiStatus.Internal
@ApiStatus.NonExtendable
public final class GenerationHelper {

    public static final ClassName ADVENTURE_KEY = ClassName.get("net.kyori.adventure.key", "Key");
    public static final MethodSpec ID_GETTER;
    public static final MethodSpec ADVENTURE_KEY_METHOD;
    public static final MethodSpec TO_STRING;
    public static final String VARIABLE_SETTER = "this.$1L = $1L";

    static {
        ID_GETTER = MethodSpec.methodBuilder("getId")
                .returns(NAMESPACE_ID_CLASS)
                .addAnnotation(NotNull.class)
                .addStatement("return this.id")
                .addModifiers(Modifier.PUBLIC)
                .build();
        ADVENTURE_KEY_METHOD = MethodSpec.methodBuilder("key")
                .returns(ADVENTURE_KEY)
                .addAnnotation(Override.class)
                .addAnnotation(NotNull.class)
                .addStatement("return this.id")
                .addModifiers(Modifier.PUBLIC)
                .build();
        TO_STRING = MethodSpec.methodBuilder("toString")
                .addAnnotation(NotNull.class)
                .addAnnotation(Override.class)
                .returns(String.class)
                // this resolves to [Namespace]
                .addStatement("return \"[\" + this.id + \"]\"")
                .addModifiers(Modifier.PUBLIC)
                .build();
    }

    private GenerationHelper() { }
}
