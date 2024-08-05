package net.minestom.codegen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.javapoet.ClassName;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;
import java.util.Locale;
import java.util.Map;

@ApiStatus.Internal
public abstract class MinestomCodeGenerator implements CodeExporter {
    public static final ClassName NAMESPACE_ID_CLASS =
            ClassName.get("net.minestom.server.utils", "NamespaceID");
    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    protected static final ClassName REGISTRY_CLASS =
            ClassName.get("net.minestom.server.registry", "Registries");
    protected static final ClassName KEYORI_ADVENTURE_KEY =
            ClassName.get("net.kyori.adventure.key", "Keyed");
    protected static final Modifier[] CONSTANT_MODIFIERS = {Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL};
    protected static final Modifier[] PRIVATE_FINAL_MODIFIERS = {Modifier.PRIVATE, Modifier.FINAL};
    protected static final String DEFAULT_INDENT = "    ";

    /**
     * Creates a new code generator.
     */
    protected MinestomCodeGenerator() {
    }

    public abstract void generate();

    protected static @NotNull String extractNamespace(@NotNull String namespace) {
        return namespace.replace("minecraft:", "").toUpperCase(Locale.ROOT);
    }

    protected static @NotNull String extractNamespaces(@NotNull String namespace, @NotNull Map<String, String> arguments) {
        if (arguments.isEmpty()) return extractNamespace(namespace);

        for (Map.Entry<String, String> entry : arguments.entrySet()) {
            namespace = namespace.replace(entry.getKey(), entry.getValue());
        }
        return namespace.toUpperCase(Locale.ROOT);
    }

    protected static String toConstant(String namespace) {
        return namespace.replace("minecraft:", "").toUpperCase(Locale.ROOT);
    }
}
