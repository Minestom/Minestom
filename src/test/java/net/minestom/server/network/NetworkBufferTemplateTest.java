package net.minestom.server.network;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static net.minestom.server.network.NetworkBuffer.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NetworkBufferTemplateTest {

    private static <T> void assertRoundTrip(NetworkBuffer.Type<T> type, T expected) {
        var array = NetworkBuffer.makeArray(type, expected);
        var buffer = NetworkBuffer.wrap(array, 0, array.length);
        assertEquals(expected, buffer.read(type));
        assertEquals(0, buffer.readableBytes());
    }

    private static NetworkBuffer.Type<Integer> trackingVarInt(String name, List<String> events) {
        return new NetworkBuffer.Type<>() {
            @Override
            public void write(NetworkBuffer buffer, Integer value) {
                events.add("write:" + name + "=" + value);
                buffer.write(VAR_INT, value);
            }

            @Override
            public Integer read(NetworkBuffer buffer) {
                Integer value = buffer.read(VAR_INT);
                events.add("read:" + name + "=" + value);
                return value;
            }
        };
    }

    @Test
    public void singleFieldTemplate() {
        record TemplateSingle(int value) {
        }
        NetworkBuffer.Type<TemplateSingle> singleType = NetworkBufferTemplate.template(
                VAR_INT, TemplateSingle::value,
                TemplateSingle::new
        );
        assertRoundTrip(singleType, new TemplateSingle(12));
    }

    @Test
    public void twoFieldTemplate() {
        record TemplatePair(int first, String second) {
        }
        NetworkBuffer.Type<TemplatePair> pairType = NetworkBufferTemplate.template(
                VAR_INT, TemplatePair::first,
                STRING, TemplatePair::second,
                TemplatePair::new
        );
        assertRoundTrip(pairType, new TemplatePair(-7, "pair"));
    }

    @Test
    public void threeFieldTemplate() {
        record TemplateTriple(int first, String second, long third) {
        }
        NetworkBuffer.Type<TemplateTriple> tripleType = NetworkBufferTemplate.template(
                VAR_INT, TemplateTriple::first, STRING, TemplateTriple::second, LONG, TemplateTriple::third,
                TemplateTriple::new);
        assertRoundTrip(tripleType, new TemplateTriple(1, "test", 3L));
    }

    @Test
    public void mixedTypeTemplate() {
        record Mixed(boolean flag, byte b, short s, int var, long l, float f, double d, String text,
                     String optionalText, List<Integer> ints) {
        }
        NetworkBuffer.Type<Mixed> mixedType = NetworkBufferTemplate.template(
                BOOLEAN, Mixed::flag,
                BYTE, Mixed::b,
                SHORT, Mixed::s,
                VAR_INT, Mixed::var,
                LONG, Mixed::l,
                FLOAT, Mixed::f,
                DOUBLE, Mixed::d,
                STRING, Mixed::text,
                STRING.optional(), Mixed::optionalText,
                VAR_INT.list(16), Mixed::ints,
                Mixed::new
        );

        assertRoundTrip(mixedType, new Mixed(true, (byte) -12, (short) 1234, 2_097_151, Long.MIN_VALUE, 12.5f, -0.25d, "hello", "optional", List.of(1, -2, 3, 4)));
        assertRoundTrip(mixedType, new Mixed(false, (byte) 42, (short) -1234, -1, Long.MAX_VALUE, -5.75f, 1024.5d, "world", null, List.of()));
    }

    @Test
    public void maxFieldTemplate() {
        record TwentyFields(int f1, int f2, int f3, int f4, int f5, int f6, int f7, int f8, int f9, int f10, int f11,
                            int f12, int f13, int f14, int f15, int f16, int f17, int f18, int f19, int f20) {
        }
        NetworkBuffer.Type<TwentyFields> twentyFieldsType = NetworkBufferTemplate.template(
                VAR_INT, TwentyFields::f1, VAR_INT, TwentyFields::f2, VAR_INT, TwentyFields::f3, VAR_INT, TwentyFields::f4,
                VAR_INT, TwentyFields::f5, VAR_INT, TwentyFields::f6, VAR_INT, TwentyFields::f7, VAR_INT, TwentyFields::f8,
                VAR_INT, TwentyFields::f9, VAR_INT, TwentyFields::f10, VAR_INT, TwentyFields::f11, VAR_INT, TwentyFields::f12,
                VAR_INT, TwentyFields::f13, VAR_INT, TwentyFields::f14, VAR_INT, TwentyFields::f15, VAR_INT, TwentyFields::f16,
                VAR_INT, TwentyFields::f17, VAR_INT, TwentyFields::f18, VAR_INT, TwentyFields::f19, VAR_INT, TwentyFields::f20,
                TwentyFields::new
        );

        assertRoundTrip(twentyFieldsType, new TwentyFields(1, -2, 3, -4, 5, -6, 7, -8, 9, -10, 11, -12, 13, -14, 15, -16, 17, -18, 19, -20));
    }

    @Test
    public void constantTemplateWritesNoBytesAndReadsConstantValue() {
        NetworkBuffer.Type<String> constantType = NetworkBufferTemplate.template("constant");
        var buffer = NetworkBuffer.resizableBuffer();

        buffer.write(constantType, "ignored");

        assertEquals(0, buffer.writeIndex());
        assertEquals("constant", buffer.read(constantType));
        assertEquals(0, buffer.readIndex());
    }

    @Test
    public void supplierTemplateWritesNoBytesAndReadsSuppliedValue() {
        int[] calls = {0};
        NetworkBuffer.Type<String> supplierType = NetworkBufferTemplate.template(() -> "value-" + ++calls[0]);
        var buffer = NetworkBuffer.resizableBuffer();

        buffer.write(supplierType, "ignored");

        assertEquals(0, buffer.writeIndex());
        assertEquals("value-1", buffer.read(supplierType));
        assertEquals("value-2", buffer.read(supplierType));
        assertEquals(0, buffer.readIndex());
        assertEquals(2, calls[0]);
    }

    @Test
    public void templatePreservesFieldOrder() {
        record Ordered(int first, int second, int third) {
        }
        var events = new ArrayList<String>();
        NetworkBuffer.Type<Integer> first = trackingVarInt("first", events);
        NetworkBuffer.Type<Integer> second = trackingVarInt("second", events);
        NetworkBuffer.Type<Integer> third = trackingVarInt("third", events);
        NetworkBuffer.Type<Ordered> orderedType = NetworkBufferTemplate.template(
                first, Ordered::first,
                second, Ordered::second,
                third, Ordered::third,
                Ordered::new
        );

        assertRoundTrip(orderedType, new Ordered(1, 2, 3));

        assertEquals(List.of("write:first=1", "write:second=2", "write:third=3", "read:first=1", "read:second=2", "read:third=3"), events);
    }
}
