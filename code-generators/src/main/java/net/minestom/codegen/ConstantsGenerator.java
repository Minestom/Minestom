package net.minestom.codegen;

import com.google.gson.JsonObject;
import com.palantir.javapoet.*;

import javax.lang.model.element.Modifier;
import java.nio.file.Path;

public final class ConstantsGenerator implements MinestomCodeGenerator {

    @Override
    public void generate(Path outputFolder, CodegenRegistry registry, CodegenValue value) {
        final ClassName implCN = ClassName.get(value.packageName(), value.typeName());

        // Important classes we use alot
        JsonObject constants = GSON.fromJson(registry.resource(value.resource()), JsonObject.class);
        ClassName minecraftConstantsCN = ClassName.get(value.packageName(), value.generatedName());
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
        writeFiles(outputFolder, JavaFile.builder(value.packageName(), constantsInterface.build())
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
