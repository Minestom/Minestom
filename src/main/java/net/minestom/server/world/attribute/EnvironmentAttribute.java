package net.minestom.server.world.attribute;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.util.ARGBLike;
import net.kyori.adventure.util.RGBLike;
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

    @SuppressWarnings({"unchecked", "rawtypes"})
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
                Operator.MINIMUM, Float.MINIMUM);
        Map<Operator, Modifier<RGBLike, ?>> RGB_OPERATORS = Map.of(
                Operator.ALPHA_BLEND, Color.ALPHA_BLEND,
                Operator.ADD, Color.ADD,
                Operator.SUBTRACT, Color.SUBTRACT,
                Operator.MULTIPLY, Color.MULTIPLY_RGB,
                Operator.BLEND_TO_GRAY, Color.BLEND_TO_GRAY);
        Map<Operator, Modifier<ARGBLike, ?>> ARGB_OPERATORS = Map.of(
                Operator.ALPHA_BLEND, (Modifier) Color.ALPHA_BLEND,
                Operator.ADD, (Modifier) Color.ADD,
                Operator.SUBTRACT, (Modifier) Color.SUBTRACT,
                Operator.MULTIPLY, (Modifier) Color.MULTIPLY_ARGB,
                Operator.BLEND_TO_GRAY, (Modifier) Color.BLEND_TO_GRAY);

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
            public static final Modifier<RGBLike, ARGBLike> ALPHA_BLEND = ColorModifier.ALPHA_BLEND;
            public static final Modifier<RGBLike, RGBLike> ADD = ColorModifier.ADD;
            public static final Modifier<RGBLike, RGBLike> SUBTRACT = ColorModifier.SUBTRACT;
            public static final Modifier<RGBLike, RGBLike> MULTIPLY_RGB = ColorModifier.MULTIPLY_RGB;
            public static final Modifier<RGBLike, ARGBLike> MULTIPLY_ARGB = ColorModifier.MULTIPLY_ARGB;
            public static final Modifier<RGBLike, BlendToGray> BLEND_TO_GRAY = ColorModifier.BLEND_TO_GRAY;
        }

        Sub modify(Sub subject, Arg argument);

        Codec<Arg> argumentCodec();

    }
}
