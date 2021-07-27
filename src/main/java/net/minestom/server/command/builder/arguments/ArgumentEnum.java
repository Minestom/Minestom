package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.UnaryOperator;

@SuppressWarnings("rawtypes")
public class ArgumentEnum<E extends Enum> extends Argument<E> {

    public final static int NOT_ENUM_VALUE_ERROR = 1;

    private final Class<E> enumClass;
    private final E[] values;
    private UnaryOperator<String> formatter = Format.DEFAULT.formatter;

    public ArgumentEnum(@NotNull String id, Class<E> enumClass) {
        super(id);
        this.enumClass = enumClass;
        this.values = enumClass.getEnumConstants();
    }

    public ArgumentEnum<E> setFormat(@NotNull Format format) {
        this.formatter = format.formatter;
        return this;
    }
    
    public ArgumentEnum<E> setFormat(@NotNull UnaryOperator<String> formatter) {
        this.formatter = formatter;
        return this;
    }

    @NotNull
    @Override
    public E parse(@NotNull String input) throws ArgumentSyntaxException {
        for (E value : this.values) {
            if (this.formatter.apply(value.name()).equals(input)) {
                return value;
            }
        }
        throw new ArgumentSyntaxException("Not a " + this.enumClass.getSimpleName() + " value", input, NOT_ENUM_VALUE_ERROR);
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        // Create a primitive array for mapping
        DeclareCommandsPacket.Node[] nodes = new DeclareCommandsPacket.Node[this.values.length];

        // Create a node for each restrictions as literal
        for (int i = 0; i < nodes.length; i++) {
            DeclareCommandsPacket.Node argumentNode = new DeclareCommandsPacket.Node();

            argumentNode.flags = DeclareCommandsPacket.getFlag(DeclareCommandsPacket.NodeType.LITERAL,
                    executable, false, false);
            argumentNode.name = this.format.formatter.apply(this.values[i].name());
            nodes[i] = argumentNode;
        }
        nodeMaker.addNodes(nodes);
    }

    public enum Format {
        /**
         * Default case just returns <code>name()</code> as is.
         */
        DEFAULT(name -> name),
        /**
         * thisislowercase
         * <br>
         * <code>"EXAMPLESTRING"        -> "examplestring"</code><br>
         * <code>"Example_TWO"          -> "example_two"</code><br>
         * <code>"ANOTHER_EXAMPLE"      -> "another_example"</code><br>
         * <code>"anotherExample_AGAIN" -> "anotherexample_again"</code><br>
         */
        LOWER_CASED(name -> name.toLowerCase(Locale.ROOT)),
        /**
         * THISISUPPERCASE
         * <br>
         * <code>"EXAMPLESTRING"        -> "EXAMPLESTRING"</code><br>
         * <code>"Example_TWO"          -> "EXAMPLE_TWO"</code><br>
         * <code>"ANOTHER_EXAMPLE"      -> "ANOTHER_EXAMPLE"</code><br>
         * <code>"anotherExample_AGAIN" -> "ANOTHEREXAMPLE_AGAIN"</code><br>
         */
        UPPER_CASED(name -> name.toUpperCase(Locale.ROOT)),
        /**
         * thisIsCamelCase
         * <br>
         * <code>"EXAMPLESTRING"        -> "examplestring"</code><br>
         * <code>"Example_TWO"          -> "exampleTwo"</code><br>
         * <code>"ANOTHER_EXAMPLE"      -> "anotherExample"</code><br>
         * <code>"anotherExample_AGAIN" -> "anotherExampleAgain"</code><br>
         */
        CAMEL_CASED(name -> {
            StringBuilder sb = new StringBuilder();
            boolean hasLetter = true;
            boolean lowerCase = true;
            for (int i = 0; i < name.length(); i++) {
                String part = name.substring(i, i+1);
                char c = part.charAt(0);
                if (Character.isLetter(c)) {
                    if (!hasLetter || (i > 0 && lowerCase && Character.isUpperCase(c))) {
                        sb.append(part.toUpperCase());
                        hasLetter = true;
                    } else {
                        sb.append(part.toLowerCase());
                    }
                    lowerCase = Character.isLowerCase(c);
                } else if (i == 0) {
                    continue;
                } else {
                    hasLetter = false;
                }
            }
            return sb.toString();
        }),
        /**
         * ThisIsPascalCase
         * <br>
         * <code>"EXAMPLESTRING"        -> "Examplestring"</code><br>
         * <code>"Example_TWO"          -> "ExampleTwo"</code><br>
         * <code>"ANOTHER_EXAMPLE"      -> "AnotherExample"</code><br>
         * <code>"anotherExample_AGAIN" -> "AnotherExampleAgain"</code><br>
         */
        PASCAL_CASED(name -> {
            StringBuilder sb = new StringBuilder();
            boolean hasLetter = false;
            boolean lowerCase = true;
            for (int i = 0; i < name.length(); i++) {
                String part = name.substring(i, i+1);
                char c = part.charAt(0);
                if (Character.isLetter(c)) {
                    if (!hasLetter || (i > 0 && lowerCase && Character.isUpperCase(c))) {
                        sb.append(part.toUpperCase());
                        hasLetter = true;
                    } else {
                        sb.append(part.toLowerCase());
                    }
                    lowerCase = Character.isLowerCase(c);
                } else if (i == 0) {
                    continue;
                } else {
                    hasLetter = false;
                }
            }
            return sb.toString();
        }),
        /**
         * This Is Title Case
         * <br>
         * <code>"EXAMPLESTRING"        -> "Examplestring"</code><br>
         * <code>"Example_TWO"          -> "Example Two"</code><br>
         * <code>"ANOTHER_EXAMPLE"      -> "Another Example"</code><br>
         * <code>"anotherExample_AGAIN" -> "Another Example Again"</code><br>
         */
        TITLE_CASED(name -> {
            if (name.length() == 0) return "";
            StringBuilder sb = new StringBuilder();
            boolean hasLetter = false;
            boolean lowerCase = true;
            for (int i = 0; i < name.length(); i++) {
                String part = name.substring(i, i+1);
                char c = part.charAt(0);
                if (Character.isLetter(c)) {
                    if (!hasLetter || (i > 0 && lowerCase && Character.isUpperCase(c))) {
                        sb.append(" ").append(part.toUpperCase());
                        hasLetter = true;
                    } else {
                        sb.append(part.toLowerCase());
                    }
                    lowerCase = Character.isLowerCase(c);
                } else if (i == 0) {
                    continue;
                } else {
                    hasLetter = false;
                }
            }
            return sb.toString().substring(1);
        }),
        /**
         * this_is_snake_case
         * <br>
         * <code>"EXAMPLESTRING"        -> "examplestring"</code><br>
         * <code>"Example_TWO"          -> "example_two"</code><br>
         * <code>"ANOTHER_EXAMPLE"      -> "another_example"</code><br>
         * <code>"anotherExample_AGAIN" -> "another_example_again"</code><br>
         */
        SNAKE_CASED(name -> {
            if (name.length() == 0) return "";
            StringBuilder sb = new StringBuilder();
            boolean hasLetter = true;
            boolean lowerCase = true;
            for (int i = 0; i < name.length(); i++) {
                String part = name.substring(i, i+1);
                char c = part.charAt(0);
                if (Character.isLetter(c)) {
                    if (!hasLetter || (i > 0 && lowerCase && Character.isUpperCase(c))) {
                        sb.append("_");
                        hasLetter = true;
                    }
                    sb.append(part.toLowerCase());
                    lowerCase = Character.isLowerCase(c);
                } else if (i == 0) {
                    continue;
                } else {
                    hasLetter = false;
                }
            }
            return sb.toString();
        });

        private final UnaryOperator<String> formatter;

        Format(@NotNull UnaryOperator<String> formatter) {
            this.formatter = formatter;
        }
    }

    @Override
    public String toString() {
        return String.format("Enum<%s>", getId());
    }
}
