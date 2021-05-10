package net.minestom.codegen;


import java.io.File;
import java.util.Arrays;

public class TagGen {

	public static void main(String[] args) {
		StringBuilder out = new StringBuilder();
		File workingDir = new File("minecraft_data/data/minecraft/tags/blocks");
		Arrays.stream(workingDir.listFiles()).forEach((file) -> {
			out.append("addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from(\"");
			out.append(file.getName(), 0, file.getName().length()-5).append("\"));\n");
		});
		workingDir = new File("minecraft_data/data/minecraft/tags/entity_types");
		Arrays.stream(workingDir.listFiles()).forEach((file) -> {
			out.append("addRequiredTag(Tag.BasicTypes.ENTITY_TYPES, NamespaceID.from(\"");
			out.append(file.getName(), 0, file.getName().length()-5).append("\"));\n");
		});
		workingDir = new File("minecraft_data/data/minecraft/tags/fluids");
		Arrays.stream(workingDir.listFiles()).forEach((file) -> {
			out.append("addRequiredTag(Tag.BasicTypes.FLUIDS, NamespaceID.from(\"");
			out.append(file.getName(), 0, file.getName().length()-5).append("\"));\n");
		});
		workingDir = new File("minecraft_data/data/minecraft/tags/items");
		Arrays.stream(workingDir.listFiles()).forEach((file) -> {
			out.append("addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from(\"");
			out.append(file.getName(), 0, file.getName().length()-5).append("\"));\n");
		});
		System.out.println(out.toString());
	}
}
