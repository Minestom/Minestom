package net.minestom.server.world.attribute;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;

import java.util.Map;

public sealed interface EnvironmentAttribute<T> extends EnvironmentAttributes permits EnvironmentAttributeImpl {

    Key key();

    Type<T> type();

    Codec<T> valueCodec();

    sealed interface Type<T> extends EnvironmentAttributeTypes permits EnvironmentAttributeTypeImpl {

        Codec<T> codec();

        Codec<Modifier<T, ?>> modifierCodec();

    }

    sealed interface Modifier<Sub, Arg> permits BooleanModifier, FloatModifier, ColorModifier, Modifier.Override {

        Map<Operator, Modifier<java.lang.Boolean, ?>> BOOLEAN_OPERATORS = Map.of(
                Operator.AND, Boolean.AND,
                Operator.NAND, Boolean.NAND,
                Operator.OR, Boolean.OR,
                Operator.NOR, Boolean.NOR,
                Operator.XOR, Boolean.XOR,
                Operator.XNOR, Boolean.XNOR);
        Map<Operator, Modifier<java.lang.Float, ?>> FLOAT_OPERATORS = Map.of(
                Operator.ALPHA_BLEND, Float.ALPHA_BLEND,
                Operator.ADD, Float.ADD,
                Operator.SUBTRACT, Float.SUBTRACT,
                Operator.MULTIPLY, Float.MULTIPLY,
                Operator.MAXIMUM, Float.MAXIMUM,
                Operator.MINIMUM, Float.MINIMUM
        );

        enum Operator {
            OVERRIDE,
            ALPHA_BLEND,
            ADD,
            SUBTRACT,
            MULTIPLY,
            BLEND_TO_GRAY,
            MINIMUM,
            MAXIMUM,
            AND,
            NAND,
            OR,
            NOR,
            XOR,
            XNOR;

            public static final Codec<Operator> CODEC = Codec.Enum(Operator.class);
        }

        record Override<Value>(
                Codec<Value> argumentCodec
        ) implements Modifier<Value, Value> {
            @java.lang.Override
            public Value modify(Value subject, Value argument) {
                return argument;
            }
        }

        final class Boolean {
            public static final Modifier<java.lang.Boolean, java.lang.Boolean> AND = BooleanModifier.AND;
            public static final Modifier<java.lang.Boolean, java.lang.Boolean> NAND = BooleanModifier.NAND;
            public static final Modifier<java.lang.Boolean, java.lang.Boolean> OR = BooleanModifier.OR;
            public static final Modifier<java.lang.Boolean, java.lang.Boolean> NOR = BooleanModifier.NOR;
            public static final Modifier<java.lang.Boolean, java.lang.Boolean> XOR = BooleanModifier.XOR;
            public static final Modifier<java.lang.Boolean, java.lang.Boolean> XNOR = BooleanModifier.XNOR;
        }

        final class Float {
            public static final Modifier<java.lang.Float, AlphaFloat> ALPHA_BLEND = FloatModifier.ALPHA_BLEND;
            public static final Modifier<java.lang.Float, java.lang.Float> ADD = FloatModifier.ADD;
            public static final Modifier<java.lang.Float, java.lang.Float> SUBTRACT = FloatModifier.SUBTRACT;
            public static final Modifier<java.lang.Float, java.lang.Float> MULTIPLY = FloatModifier.MULTIPLY;
            public static final Modifier<java.lang.Float, java.lang.Float> MAXIMUM = FloatModifier.MAXIMUM;
            public static final Modifier<java.lang.Float, java.lang.Float> MINIMUM = FloatModifier.MINIMUM;
        }

        final class Color {

        }

        Sub modify(Sub subject, Arg argument);

        Codec<Arg> argumentCodec();

    }
}
