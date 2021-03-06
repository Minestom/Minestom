package net.minestom.codegen.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.squareup.javapoet.*;
import net.minestom.codegen.MinestomCodeGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Modifier;
import java.io.*;
import java.util.Collections;
import java.util.function.Supplier;

public final class MaterialGenerator extends MinestomCodeGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MaterialGenerator.class);
    private final InputStream itemsFile;
    private final File outputFolder;

    public MaterialGenerator(@Nullable InputStream itemsFile, @NotNull File outputFolder) {
        this.itemsFile = itemsFile;
        this.outputFolder = outputFolder;
    }

    @Override
    public void generate() {
        if (itemsFile == null) {
            LOGGER.error("Failed to find items.json.");
            LOGGER.error("Stopped code generation for items.");
            return;
        }
        if (!outputFolder.exists() && !outputFolder.mkdirs()) {
            LOGGER.error("Output folder for code generation does not exist and could not be created.");
            return;
        }
        // Important classes we use alot
        ClassName namespaceIDClassName = ClassName.get("net.minestom.server.utils", "NamespaceID");
        ClassName registriesClassName = ClassName.get("net.minestom.server.registry", "Registries");
        ClassName blockCN = ClassName.get("net.minestom.server.instance.block", "Block");
        ParameterizedTypeName blocksCNSupplier = ParameterizedTypeName.get(ClassName.get(Supplier.class), blockCN);

        JsonArray items = GSON.fromJson(new JsonReader(new InputStreamReader(itemsFile)), JsonArray.class);
        ClassName itemClassName = ClassName.get("net.minestom.server.item", "Material");

        // Item
        TypeSpec.Builder itemClass = TypeSpec.enumBuilder(itemClassName)
                .addSuperinterface(ClassName.get("net.kyori.adventure.key", "Keyed"))
                .addModifiers(Modifier.PUBLIC).addJavadoc("AUTOGENERATED by " + getClass().getSimpleName());

        itemClass.addField(
                FieldSpec.builder(namespaceIDClassName, "id")
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL).addAnnotation(NotNull.class).build()
        );
        itemClass.addField(
                FieldSpec.builder(TypeName.BYTE, "maxDefaultStackSize")
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL).build()
        );
        itemClass.addField(
                FieldSpec.builder(blocksCNSupplier, "correspondingBlockSupplier")
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL).build()
        );
        // static field
        itemClass.addField(
                FieldSpec.builder(ArrayTypeName.of(itemClassName), "VALUES")
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("values()")
                        .build()
        );
        itemClass.addMethod(
                MethodSpec.constructorBuilder()
                        .addParameter(ParameterSpec.builder(namespaceIDClassName, "id").addAnnotation(NotNull.class).build())
                        .addParameter(TypeName.BYTE, "maxDefaultStackSize")
                        .addParameter(ParameterSpec.builder(blocksCNSupplier, "correspondingBlockSupplier").addAnnotation(NotNull.class).build())
                        .addStatement("this.id = id")
                        .addStatement("this.maxDefaultStackSize = maxDefaultStackSize")
                        .addStatement("this.correspondingBlockSupplier = correspondingBlockSupplier")
                        .addStatement("$T.materials.put(id, this)", registriesClassName)
                        .build()
        );
        // Override key method (adventure)
        itemClass.addMethod(
                MethodSpec.methodBuilder("key")
                        .returns(ClassName.get("net.kyori.adventure.key", "Key"))
                        .addAnnotation(Override.class)
                        .addAnnotation(NotNull.class)
                        .addStatement("return this.id")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // getId method
        itemClass.addMethod(
                MethodSpec.methodBuilder("getId")
                        .returns(TypeName.SHORT)
                        .addStatement("return (short) ordinal()")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // getNamespaceID method
        itemClass.addMethod(
                MethodSpec.methodBuilder("getNamespaceID")
                        .returns(namespaceIDClassName)
                        .addAnnotation(NotNull.class)
                        .addStatement("return this.id")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // getName method
        itemClass.addMethod(
                MethodSpec.methodBuilder("getName")
                        .addAnnotation(NotNull.class)
                        .returns(ClassName.get(String.class))
                        .addStatement("return this.id.asString()")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // getMaxDefaultStackSize
        itemClass.addMethod(
                MethodSpec.methodBuilder("getMaxDefaultStackSize")
                        .returns(TypeName.BYTE)
                        .addStatement("return this.maxDefaultStackSize")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // fromId Method
        itemClass.addMethod(
                MethodSpec.methodBuilder("fromId")
                        .returns(itemClassName)
                        .addAnnotation(Nullable.class)
                        .addParameter(TypeName.SHORT, "id")
                        .beginControlFlow("if(id >= 0 && id < VALUES.length)")
                        .addStatement("return VALUES[id]")
                        .endControlFlow()
                        .addStatement("return null")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .build()
        );
        // isFood method
        itemClass.addMethod(
                MethodSpec.methodBuilder("isFood")
                        .returns(TypeName.BOOLEAN)
                        .addStatement("return false")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // hasState method
        itemClass.addMethod(
                MethodSpec.methodBuilder("hasState")
                        .returns(TypeName.BOOLEAN)
                        .beginControlFlow("if (this == BOW || this == TRIDENT || this == CROSSBOW || this == SHIELD)")
                        .addStatement("return true")
                        .nextControlFlow("else")
                        .addStatement("return isFood()")
                        .endControlFlow()
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // isBlock method
        itemClass.addMethod(
                MethodSpec.methodBuilder("isBlock")
                        .returns(TypeName.BOOLEAN)
                        .addStatement(
                                "return this.correspondingBlockSupplier.get() != null && this.correspondingBlockSupplier.get() != $T.AIR",
                                blockCN
                        )
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // isArmor method
        itemClass.addMethod(
                MethodSpec.methodBuilder("isArmor")
                        .returns(TypeName.BOOLEAN)
                        .addStatement("return false")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // isHelmet method
        itemClass.addMethod(
                MethodSpec.methodBuilder("isHelmet")
                        .returns(TypeName.BOOLEAN)
                        .addStatement("return false")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // isChestplate method
        itemClass.addMethod(
                MethodSpec.methodBuilder("isChestplate")
                        .returns(TypeName.BOOLEAN)
                        .addStatement("return false")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // isLeggings method
        itemClass.addMethod(
                MethodSpec.methodBuilder("isLeggings")
                        .returns(TypeName.BOOLEAN)
                        .addStatement("return false")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // isBoots method
        itemClass.addMethod(
                MethodSpec.methodBuilder("isBoots")
                        .returns(TypeName.BOOLEAN)
                        .addStatement("return false")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // getBlock method
        itemClass.addMethod(
                MethodSpec.methodBuilder("getBlock")
                        .addAnnotation(Nullable.class)
                        .returns(blockCN)
                        .addStatement("return this.correspondingBlockSupplier.get()")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // toString method
        itemClass.addMethod(
                MethodSpec.methodBuilder("toString")
                        .addAnnotation(NotNull.class)
                        .addAnnotation(Override.class)
                        .returns(String.class)
                        // this resolves to [Namespace]
                        .addStatement("return \"[\" + this.id + \"]\"")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );

        // Use data
        for (JsonElement i : items) {
            JsonObject item = i.getAsJsonObject();

            String itemName = item.get("name").getAsString();
            TypeSpec.Builder enumConst;
            if (!(item.get("blockId").getAsString().equals("minecraft:air"))) {
                enumConst = TypeSpec.anonymousClassBuilder(
                        "$T.from($S), (byte) $L, () -> $T.getBlock($S)",
                        namespaceIDClassName,
                        item.get("id").getAsString(),
                        item.get("maxStackSize").getAsInt(),
                        // Supplier
                        registriesClassName,
                        item.get("blockId").getAsString()
                );
            } else {
                enumConst = TypeSpec.anonymousClassBuilder(
                        "$T.from($S), (byte) $L, () -> null",
                        namespaceIDClassName,
                        item.get("id").getAsString(),
                        item.get("maxStackSize").getAsInt()
                );
            }
            if (item.get("edible").getAsBoolean()) {
                enumConst.addMethod(
                        MethodSpec.methodBuilder("isFood")
                                .returns(TypeName.BOOLEAN)
                                .addAnnotation(Override.class)
                                .addModifiers(Modifier.PUBLIC)
                                .addStatement("return true")
                                .build()
                );
            }
            if (item.get("armorProperties") != null) {
                JsonObject ap = item.get("armorProperties").getAsJsonObject();
                enumConst.addMethod(
                        MethodSpec.methodBuilder("isArmor")
                                .returns(TypeName.BOOLEAN)
                                .addAnnotation(Override.class)
                                .addModifiers(Modifier.PUBLIC)
                                .addStatement("return true")
                                .build()
                );
                if (ap.get("slot") != null) {
                    switch (ap.get("slot").getAsString()) {
                        case "head": {
                            enumConst.addMethod(
                                    MethodSpec.methodBuilder("isHelmet")
                                            .returns(TypeName.BOOLEAN)
                                            .addAnnotation(Override.class)
                                            .addModifiers(Modifier.PUBLIC)
                                            .addStatement("return true")
                                            .build()
                            );
                            break;
                        }
                        case "chest": {
                            enumConst.addMethod(
                                    MethodSpec.methodBuilder("isChestplate")
                                            .returns(TypeName.BOOLEAN)
                                            .addAnnotation(Override.class)
                                            .addModifiers(Modifier.PUBLIC)
                                            .addStatement("return true")
                                            .build()
                            );
                            break;
                        }
                        case "legs": {
                            enumConst.addMethod(
                                    MethodSpec.methodBuilder("isLeggings")
                                            .returns(TypeName.BOOLEAN)
                                            .addAnnotation(Override.class)
                                            .addModifiers(Modifier.PUBLIC)
                                            .addStatement("return true")
                                            .build()
                            );
                            break;
                        }
                        case "feet": {
                            enumConst.addMethod(
                                    MethodSpec.methodBuilder("isBoots")
                                            .returns(TypeName.BOOLEAN)
                                            .addAnnotation(Override.class)
                                            .addModifiers(Modifier.PUBLIC)
                                            .addStatement("return true")
                                            .build()
                            );
                            break;
                        }
                    }
                }


            }
            itemClass.addEnumConstant(itemName, enumConst.build());
        }

        // Write files to outputFolder
        writeFiles(
                Collections.singletonList(
                        JavaFile.builder("net.minestom.server.item", itemClass.build())
                                .indent("    ")
                                .skipJavaLangImports(true)
                                .build()
                ),
                outputFolder
        );
    }
}
