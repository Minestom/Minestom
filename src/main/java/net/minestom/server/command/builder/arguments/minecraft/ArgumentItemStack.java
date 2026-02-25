package net.minestom.server.command.builder.arguments.minecraft;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.CustomData;
import net.minestom.server.registry.RegistryTranscoder;

import java.io.IOException;

/**
 * Argument which can be used to retrieve an {@link ItemStack} from its material and with NBT data.
 * <p>
 * It is the same type as the one used in the /give command.
 * <p>
 * Example: diamond_sword{display:{Name:"{\"text\":\"Sword of Power\"}"}}
 */
public class ArgumentItemStack extends Argument<ItemStack> {

    public static final int NO_MATERIAL = 1;
    public static final int INVALID_NBT = 2;
    public static final int INVALID_MATERIAL = 3;
    public static final int INVALID_COMPONENT = 4;

    public ArgumentItemStack(String id) {
        super(id, true);
    }

    @Override
    public ItemStack parse(CommandSender sender, String input) throws ArgumentSyntaxException {
        return staticParse(input);
    }

    @Override
    public ArgumentParserType parser() {
        return ArgumentParserType.ITEM_STACK;
    }

    /**
     * @deprecated use {@link Argument#parse(CommandSender, Argument)}
     */
    @SuppressWarnings("unchecked") @Deprecated
    public static ItemStack staticParse(String input) throws ArgumentSyntaxException {
        var reader = new StringReader(input);

        final Material material = Material.fromKey(reader.readKey());
        if (material == null)
            throw new ArgumentSyntaxException("Material is invalid", input, INVALID_MATERIAL);
        if (!reader.hasMore()) {
            return ItemStack.of(material); // Nothing else, we have our item
        }

        DataComponentMap.Builder components = DataComponentMap.builder();

        // Parse the declared components
        if (reader.peek() == '[') {
            reader.consume('[');
            final Transcoder<BinaryTag> coder = new RegistryTranscoder<>(Transcoder.NBT, MinecraftServer.process());
            do {
                final Key componentId = reader.readKey();
                final DataComponent<?> component = DataComponent.fromKey(componentId);
                if (component == null)
                    throw new ArgumentSyntaxException("Unknown item component", input, INVALID_COMPONENT);

                reader.consume('=');

                final Result<Object> componentValueResult = (Result<Object>) component.decode(coder, reader.readTag());
                components.set((DataComponent<Object>) component, componentValueResult.orElseThrow());

                if (reader.peek() != ']')
                    reader.consume(',');
            } while (reader.peek() != ']');
            reader.consume(']');
        }

        // Parse the NBT
        if (reader.hasMore() && reader.peek() == '{') {
            final BinaryTag nbt = reader.readTag();
            if (!(nbt instanceof CompoundBinaryTag compound))
                throw new ArgumentSyntaxException("Item NBT must be compound", input, INVALID_NBT);

            final CompoundBinaryTag customData = CompoundBinaryTag.builder()
                    .put(components.get(DataComponents.CUSTOM_DATA, CustomData.EMPTY).nbt())
                    .put(compound)
                    .build();
            components.set(DataComponents.CUSTOM_DATA, new CustomData(customData));
        }

        if (reader.hasMore())
            throw new ArgumentSyntaxException("Unexpected remaining input", input, INVALID_NBT);

        return ItemStack.of(material, components.build());
    }

    @Override
    public String toString() {
        return String.format("ItemStack<%s>", getId());
    }

    private static class StringReader {
        private String input;
        private int index = 0;

        public StringReader(String input) {
            this.input = input;
        }

        public boolean hasMore() {
            return index < input.length();
        }

        public char peek() {
            if (!hasMore()) {
                throw new ArgumentSyntaxException("Unexpected end of input", input, INVALID_NBT);
            }

            return input.charAt(index);
        }

        public void consume(char c) {
            char next = peek();
            if (next != c) {
                throw new ArgumentSyntaxException("Expected '" + c + "', got '" + next + "'", input, INVALID_NBT);
            }
            index++;
        }

        public Key readKey() {
            char c;
            int start = index;
            while (hasMore() && (c = peek()) != '{' && c != '[' && c != '=') {
                index++;
            }
            return Key.key(input.substring(start, index));
        }

        public BinaryTag readTag() {
            try {
                StringBuilder remainder = new StringBuilder();
                final BinaryTag result = MinestomAdventure.tagStringIO().asTag(input.substring(index), remainder);
                this.input = remainder.toString();
                this.index = 0;

                return result;
            } catch (IOException e) {
                throw new ArgumentSyntaxException("Invalid NBT", input, INVALID_NBT);
            }
        }
    }
}
