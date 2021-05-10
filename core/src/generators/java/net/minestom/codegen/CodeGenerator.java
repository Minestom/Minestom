package net.minestom.codegen;

import com.squareup.javapoet.JavaFile;
import org.slf4j.Logger;

import java.io.*;
import java.util.List;

/**
 * Interface representing a code generator
 */
public interface CodeGenerator {

    /**
     * Generates the Java code
     * @return
     */
    List<JavaFile> generate() throws IOException;

    default void generateTo(File targetFolder) throws IOException {
        List<JavaFile> code = generate();

        for(JavaFile file : code) {
            getLogger().debug("Writing file: "+file.packageName+"."+file.typeSpec.name);
            file.writeTo(targetFolder);
        }
    }

    Logger getLogger();

    static String decapitalize(String text) {
        char first = text.charAt(0);
        return ""+Character.toLowerCase(first)+text.substring(1);
    }
}
