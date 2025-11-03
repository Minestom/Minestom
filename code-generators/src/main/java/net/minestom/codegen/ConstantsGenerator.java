package net.minestom.codegen;

import com.google.gson.JsonObject;
import com.palantir.javapoet.*;

import javax.lang.model.element.Modifier;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Objects;

public record ConstantsGenerator(InputStream constantsFile,
                                 Path outputFolder) implements MinestomCodeGenerator {
    public ConstantsGenerator {
        Objects.requireNonNull(constantsFile, "Constants file cannot be null");
        Objects.requireNonNull(outputFolder, "Output folder cannot be null");
    }

    @Override
    public void generate() {
        ensureDirectory(outputFolder);

        final ClassName implCN = ClassName.get("net.minestom.server", "MinecraftServer");

        // Important classes we use alot
        JsonObject constants = GSON.fromJson(new InputStreamReader(constantsFile), JsonObject.class);
        ClassName minecraftConstantsCN = ClassName.get("net.minestom.server", "MinecraftConstants");
        TypeSpec.Builder constantsInterface = TypeSpec.interfaceBuilder(minecraftConstantsCN)
                .addModifiers(Modifier.SEALED)
                .addPermittedSubclass(implCN)
                .addJavadoc(generateJavadoc(implCN));

        constantsInterface.addField(FieldSpec.builder(String.class, "VERSION_NAME")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", constants.get("name").getAsString())
                .build()
        );
        constantsInterface.addField(FieldSpec.builder(TypeName.INT, "PROTOCOL_VERSION")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$L", constants.get("protocol").getAsInt())
                .build()
        );
        constantsInterface.addField(FieldSpec.builder(TypeName.INT, "DATA_VERSION")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$L", constants.get("world").getAsInt())
                .build()
        );
        addMajorMinorField(constantsInterface, "RESOURCE_PACK_VERSION", constants.get("resourcepack").getAsString());
        addMajorMinorField(constantsInterface, "DATA_PACK_VERSION", constants.get("datapack").getAsString());

        // Write files to outputFolder
        writeFiles(JavaFile.builder("net.minestom.server", constantsInterface.build())
                .indent("    ")
                .skipJavaLangImports(true)
                .build()
        );
    }

    private static void addMajorMinorField(TypeSpec.Builder typeSpec, String name, String value) {
        String[] parts = value.split("\\.");
        if (parts.length != 2) throw new IllegalArgumentException("Invalid version format for " + name + ": " + value);

        var majorMinorClass = ClassName.get("net.minestom.server.utils", "MajorMinorVersion");
        typeSpec.addField(FieldSpec.builder(majorMinorClass, name)
                                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                                            .initializer("new $T($L, $L)", majorMinorClass, parts[0], parts[1])
                                            .build()
        );
    }

}
