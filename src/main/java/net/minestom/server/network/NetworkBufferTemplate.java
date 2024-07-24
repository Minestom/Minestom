package net.minestom.server.network;

import net.minestom.server.network.NetworkBuffer.Type;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Supplier;

public final class NetworkBufferTemplate {

    @FunctionalInterface
    public interface F1<P1, R> {
        R apply(P1 p1);
    }

    @FunctionalInterface
    public interface F2<P1, P2, R> {
        R apply(P1 p1, P2 p2);
    }

    @FunctionalInterface
    public interface F3<P1, P2, P3, R> {
        R apply(P1 p1, P2 p2, P3 p3);
    }

    @FunctionalInterface
    public interface F4<P1, P2, P3, P4, R> {
        R apply(P1 p1, P2 p2, P3 p3, P4 p4);
    }

    @FunctionalInterface
    public interface F5<P1, P2, P3, P4, P5, R> {
        R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5);
    }

    @FunctionalInterface
    public interface F6<P1, P2, P3, P4, P5, P6, R> {
        R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6);
    }

    @FunctionalInterface
    public interface F7<P1, P2, P3, P4, P5, P6, P7, R> {
        R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7);
    }

    @FunctionalInterface
    public interface F8<P1, P2, P3, P4, P5, P6, P7, P8, R> {
        R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8);
    }

    @FunctionalInterface
    public interface F9<P1, P2, P3, P4, P5, P6, P7, P8, P9, R> {
        R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9);
    }

    @FunctionalInterface
    public interface F10<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, R> {
        R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10);
    }

    public static <R> Type<R> template(Supplier<R> supplier) {
        return new NetworkBufferTypeImpl<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, R value) {
            }

            @Override
            public R read(@NotNull NetworkBuffer buffer) {
                return supplier.get();
            }
        };
    }

    public static <P1, R> Type<R> template(Type<P1> p1, Function<R, P1> g1, F1<P1, R> reader) {
        return new NetworkBufferTypeImpl<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, R value) {
                p1.write(buffer, g1.apply(value));
            }

            @Override
            public R read(@NotNull NetworkBuffer buffer) {
                return reader.apply(p1.read(buffer));
            }
        };
    }

    public static <P1, P2, R> Type<R> template(
            Type<P1> p1, Function<R, P1> g1, Type<P2> p2, Function<R, P2> g2,
            F2<P1, P2, R> reader
    ) {
        return new NetworkBufferTypeImpl<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, R value) {
                p1.write(buffer, g1.apply(value));
                p2.write(buffer, g2.apply(value));
            }

            @Override
            public R read(@NotNull NetworkBuffer buffer) {
                return reader.apply(p1.read(buffer), p2.read(buffer));
            }
        };
    }

    public static <P1, P2, P3, R> Type<R> template(
            Type<P1> p1, Function<R, P1> g1, Type<P2> p2, Function<R, P2> g2,
            Type<P3> p3, Function<R, P3> g3, F3<P1, P2, P3, R> reader
    ) {
        return new NetworkBufferTypeImpl<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, R value) {
                p1.write(buffer, g1.apply(value));
                p2.write(buffer, g2.apply(value));
                p3.write(buffer, g3.apply(value));
            }

            @Override
            public R read(@NotNull NetworkBuffer buffer) {
                return reader.apply(p1.read(buffer), p2.read(buffer), p3.read(buffer));
            }
        };
    }

    public static <P1, P2, P3, P4, R> Type<R> template(
            Type<P1> p1, Function<R, P1> g1, Type<P2> p2, Function<R, P2> g2,
            Type<P3> p3, Function<R, P3> g3, Type<P4> p4, Function<R, P4> g4,
            F4<P1, P2, P3, P4, R> reader
    ) {
        return new NetworkBufferTypeImpl<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, R value) {
                p1.write(buffer, g1.apply(value));
                p2.write(buffer, g2.apply(value));
                p3.write(buffer, g3.apply(value));
                p4.write(buffer, g4.apply(value));
            }

            @Override
            public R read(@NotNull NetworkBuffer buffer) {
                return reader.apply(
                        p1.read(buffer), p2.read(buffer),
                        p3.read(buffer), p4.read(buffer)
                );
            }
        };
    }

    public static <P1, P2, P3, P4, P5, R> Type<R> template(
            Type<P1> p1, Function<R, P1> g1, Type<P2> p2, Function<R, P2> g2,
            Type<P3> p3, Function<R, P3> g3, Type<P4> p4, Function<R, P4> g4,
            Type<P5> p5, Function<R, P5> g5, F5<P1, P2, P3, P4, P5, R> reader
    ) {
        return new NetworkBufferTypeImpl<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, R value) {
                p1.write(buffer, g1.apply(value));
                p2.write(buffer, g2.apply(value));
                p3.write(buffer, g3.apply(value));
                p4.write(buffer, g4.apply(value));
                p5.write(buffer, g5.apply(value));
            }

            @Override
            public R read(@NotNull NetworkBuffer buffer) {
                return reader.apply(
                        p1.read(buffer), p2.read(buffer),
                        p3.read(buffer), p4.read(buffer),
                        p5.read(buffer)
                );
            }
        };
    }

    public static <P1, P2, P3, P4, P5, P6, R> Type<R> template(
            Type<P1> p1, Function<R, P1> g1, Type<P2> p2, Function<R, P2> g2,
            Type<P3> p3, Function<R, P3> g3, Type<P4> p4, Function<R, P4> g4,
            Type<P5> p5, Function<R, P5> g5, Type<P6> p6, Function<R, P6> g6,
            F6<P1, P2, P3, P4, P5, P6, R> reader
    ) {
        return new NetworkBufferTypeImpl<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, R value) {
                p1.write(buffer, g1.apply(value));
                p2.write(buffer, g2.apply(value));
                p3.write(buffer, g3.apply(value));
                p4.write(buffer, g4.apply(value));
                p5.write(buffer, g5.apply(value));
                p6.write(buffer, g6.apply(value));
            }

            @Override
            public R read(@NotNull NetworkBuffer buffer) {
                return reader.apply(
                        p1.read(buffer), p2.read(buffer),
                        p3.read(buffer), p4.read(buffer),
                        p5.read(buffer), p6.read(buffer)
                );
            }
        };
    }

    public static <P1, P2, P3, P4, P5, P6, P7, R> Type<R> template(
            Type<P1> p1, Function<R, P1> g1, Type<P2> p2, Function<R, P2> g2,
            Type<P3> p3, Function<R, P3> g3, Type<P4> p4, Function<R, P4> g4,
            Type<P5> p5, Function<R, P5> g5, Type<P6> p6, Function<R, P6> g6,
            Type<P7> p7, Function<R, P7> g7, F7<P1, P2, P3, P4, P5, P6, P7, R> reader
    ) {
        return new NetworkBufferTypeImpl<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, R value) {
                p1.write(buffer, g1.apply(value));
                p2.write(buffer, g2.apply(value));
                p3.write(buffer, g3.apply(value));
                p4.write(buffer, g4.apply(value));
                p5.write(buffer, g5.apply(value));
                p6.write(buffer, g6.apply(value));
                p7.write(buffer, g7.apply(value));
            }

            @Override
            public R read(@NotNull NetworkBuffer buffer) {
                return reader.apply(
                        p1.read(buffer), p2.read(buffer),
                        p3.read(buffer), p4.read(buffer),
                        p5.read(buffer), p6.read(buffer),
                        p7.read(buffer)
                );
            }
        };
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, R> Type<R> template(
            Type<P1> p1, Function<R, P1> g1, Type<P2> p2, Function<R, P2> g2,
            Type<P3> p3, Function<R, P3> g3, Type<P4> p4, Function<R, P4> g4,
            Type<P5> p5, Function<R, P5> g5, Type<P6> p6, Function<R, P6> g6,
            Type<P7> p7, Function<R, P7> g7, Type<P8> p8, Function<R, P8> g8,
            F8<P1, P2, P3, P4, P5, P6, P7, P8, R> reader
    ) {
        return new NetworkBufferTypeImpl<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, R value) {
                p1.write(buffer, g1.apply(value));
                p2.write(buffer, g2.apply(value));
                p3.write(buffer, g3.apply(value));
                p4.write(buffer, g4.apply(value));
                p5.write(buffer, g5.apply(value));
                p6.write(buffer, g6.apply(value));
                p7.write(buffer, g7.apply(value));
                p8.write(buffer, g8.apply(value));
            }

            @Override
            public R read(@NotNull NetworkBuffer buffer) {
                return reader.apply(
                        p1.read(buffer), p2.read(buffer),
                        p3.read(buffer), p4.read(buffer),
                        p5.read(buffer), p6.read(buffer),
                        p7.read(buffer), p8.read(buffer)
                );
            }
        };
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, R> Type<R> template(
            Type<P1> p1, Function<R, P1> g1, Type<P2> p2, Function<R, P2> g2,
            Type<P3> p3, Function<R, P3> g3, Type<P4> p4, Function<R, P4> g4,
            Type<P5> p5, Function<R, P5> g5, Type<P6> p6, Function<R, P6> g6,
            Type<P7> p7, Function<R, P7> g7, Type<P8> p8, Function<R, P8> g8,
            Type<P9> p9, Function<R, P9> g9, F9<P1, P2, P3, P4, P5, P6, P7, P8, P9, R> reader
    ) {
        return new NetworkBufferTypeImpl<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, R value) {
                p1.write(buffer, g1.apply(value));
                p2.write(buffer, g2.apply(value));
                p3.write(buffer, g3.apply(value));
                p4.write(buffer, g4.apply(value));
                p5.write(buffer, g5.apply(value));
                p6.write(buffer, g6.apply(value));
                p7.write(buffer, g7.apply(value));
                p8.write(buffer, g8.apply(value));
                p9.write(buffer, g9.apply(value));
            }

            @Override
            public R read(@NotNull NetworkBuffer buffer) {
                return reader.apply(
                        p1.read(buffer), p2.read(buffer),
                        p3.read(buffer), p4.read(buffer),
                        p5.read(buffer), p6.read(buffer),
                        p7.read(buffer), p8.read(buffer),
                        p9.read(buffer)
                );
            }
        };
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, R> Type<R> template(
            Type<P1> p1, Function<R, P1> g1, Type<P2> p2, Function<R, P2> g2,
            Type<P3> p3, Function<R, P3> g3, Type<P4> p4, Function<R, P4> g4,
            Type<P5> p5, Function<R, P5> g5, Type<P6> p6, Function<R, P6> g6,
            Type<P7> p7, Function<R, P7> g7, Type<P8> p8, Function<R, P8> g8,
            Type<P9> p9, Function<R, P9> g9, Type<P10> p10, Function<R, P10> g10,
            F10<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, R> reader
    ) {
        return new NetworkBufferTypeImpl<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, R value) {
                p1.write(buffer, g1.apply(value));
                p2.write(buffer, g2.apply(value));
                p3.write(buffer, g3.apply(value));
                p4.write(buffer, g4.apply(value));
                p5.write(buffer, g5.apply(value));
                p6.write(buffer, g6.apply(value));
                p7.write(buffer, g7.apply(value));
                p8.write(buffer, g8.apply(value));
                p9.write(buffer, g9.apply(value));
                p10.write(buffer, g10.apply(value));
            }

            @Override
            public R read(@NotNull NetworkBuffer buffer) {
                return reader.apply(
                        p1.read(buffer), p2.read(buffer),
                        p3.read(buffer), p4.read(buffer),
                        p5.read(buffer), p6.read(buffer),
                        p7.read(buffer), p8.read(buffer),
                        p9.read(buffer), p10.read(buffer)
                );
            }
        };
    }
}
