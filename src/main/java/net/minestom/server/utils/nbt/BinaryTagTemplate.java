package net.minestom.server.utils.nbt;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Supplier;

import static net.minestom.server.network.NetworkBufferTemplate.*;

public final class BinaryTagTemplate {

    public static <R> BinaryTagSerializer<R> object(Supplier<R> supplier) {
        return new ObjectBase<>(){
            @Override
            protected @NotNull R readObject(@NotNull Context context, @NotNull CompoundBinaryTag tag) {
                return supplier.get();
            }

            @Override
            protected @NotNull CompoundBinaryTag writeObject(@NotNull Context context, @NotNull R value) {
                return CompoundBinaryTag.empty();
            }
        };
    }

    public static <R, P1> BinaryTagSerializer<R> object(
            String name1, BinaryTagSerializer<P1> type1, Function<R, P1> getter1,
            F1<P1, R> ctor
    ) {
        return new ObjectBase<>(){
            @Override
            protected @NotNull R readObject(@NotNull Context context, @NotNull CompoundBinaryTag tag) {
                return ctor.apply(type1.read(context, tag.get(name1)));
            }

            @Override
            protected @NotNull CompoundBinaryTag writeObject(@NotNull Context context, @NotNull R value) {
                BinaryTag tag;
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                if ((tag = type1.write(context, getter1.apply(value))) != null) builder.put(name1, tag);
                return builder.build();
            }
        };
    }

    public static <R, P1, P2> BinaryTagSerializer<R> object(
            String name1, BinaryTagSerializer<P1> type1, Function<R, P1> getter1,
            String name2, BinaryTagSerializer<P2> type2, Function<R, P2> getter2,
            F2<P1, P2, R> ctor
    ) {
        return new ObjectBase<>(){
            @Override
            protected @NotNull R readObject(@NotNull Context context, @NotNull CompoundBinaryTag tag) {
                return ctor.apply(
                    type1.read(context, tag.get(name1)),
                    type2.read(context, tag.get(name2))
                );
            }

            @Override
            protected @NotNull CompoundBinaryTag writeObject(@NotNull Context context, @NotNull R value) {
                BinaryTag tag;
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                if ((tag = type1.write(context, getter1.apply(value))) != null) builder.put(name1, tag);
                if ((tag = type2.write(context, getter2.apply(value))) != null) builder.put(name2, tag);
                return builder.build();
            }
        };
    }

    public static <R, P1, P2, P3> BinaryTagSerializer<R> object(
            String name1, BinaryTagSerializer<P1> type1, Function<R, P1> getter1,
            String name2, BinaryTagSerializer<P2> type2, Function<R, P2> getter2,
            String name3, BinaryTagSerializer<P3> type3, Function<R, P3> getter3,
            F3<P1, P2, P3, R> ctor
    ) {
        return new ObjectBase<>(){
            @Override
            protected @NotNull R readObject(@NotNull Context context, @NotNull CompoundBinaryTag tag) {
                return ctor.apply(
                    type1.read(context, tag.get(name1)),
                    type2.read(context, tag.get(name2)),
                    type3.read(context, tag.get(name3))
                );
            }

            @Override
            protected @NotNull CompoundBinaryTag writeObject(@NotNull Context context, @NotNull R value) {
                BinaryTag tag;
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                if ((tag = type1.write(context, getter1.apply(value))) != null) builder.put(name1, tag);
                if ((tag = type2.write(context, getter2.apply(value))) != null) builder.put(name2, tag);
                if ((tag = type3.write(context, getter3.apply(value))) != null) builder.put(name3, tag);
                return builder.build();
            }
        };
    }

    public static <R, P1, P2, P3, P4> BinaryTagSerializer<R> object(
            String name1, BinaryTagSerializer<P1> type1, Function<R, P1> getter1,
            String name2, BinaryTagSerializer<P2> type2, Function<R, P2> getter2,
            String name3, BinaryTagSerializer<P3> type3, Function<R, P3> getter3,
            String name4, BinaryTagSerializer<P4> type4, Function<R, P4> getter4,
            F4<P1, P2, P3, P4, R> ctor
    ) {
        return new ObjectBase<>(){
            @Override
            protected @NotNull R readObject(@NotNull Context context, @NotNull CompoundBinaryTag tag) {
                return ctor.apply(
                    type1.read(context, tag.get(name1)),
                    type2.read(context, tag.get(name2)),
                    type3.read(context, tag.get(name3)),
                    type4.read(context, tag.get(name4))
                );
            }

            @Override
            protected @NotNull CompoundBinaryTag writeObject(@NotNull Context context, @NotNull R value) {
                BinaryTag tag;
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                if ((tag = type1.write(context, getter1.apply(value))) != null) builder.put(name1, tag);
                if ((tag = type2.write(context, getter2.apply(value))) != null) builder.put(name2, tag);
                if ((tag = type3.write(context, getter3.apply(value))) != null) builder.put(name3, tag);
                if ((tag = type4.write(context, getter4.apply(value))) != null) builder.put(name4, tag);
                return builder.build();
            }
        };
    }

    public static <R, P1, P2, P3, P4, P5> BinaryTagSerializer<R> object(
            String name1, BinaryTagSerializer<P1> type1, Function<R, P1> getter1,
            String name2, BinaryTagSerializer<P2> type2, Function<R, P2> getter2,
            String name3, BinaryTagSerializer<P3> type3, Function<R, P3> getter3,
            String name4, BinaryTagSerializer<P4> type4, Function<R, P4> getter4,
            String name5, BinaryTagSerializer<P5> type5, Function<R, P5> getter5,
            F5<P1, P2, P3, P4, P5, R> ctor
    ) {
        return new ObjectBase<>(){
            @Override
            protected @NotNull R readObject(@NotNull Context context, @NotNull CompoundBinaryTag tag) {
                return ctor.apply(
                    type1.read(context, tag.get(name1)),
                    type2.read(context, tag.get(name2)),
                    type3.read(context, tag.get(name3)),
                    type4.read(context, tag.get(name4)),
                    type5.read(context, tag.get(name5))
                );
            }

            @Override
            protected @NotNull CompoundBinaryTag writeObject(@NotNull Context context, @NotNull R value) {
                BinaryTag tag;
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                if ((tag = type1.write(context, getter1.apply(value))) != null) builder.put(name1, tag);
                if ((tag = type2.write(context, getter2.apply(value))) != null) builder.put(name2, tag);
                if ((tag = type3.write(context, getter3.apply(value))) != null) builder.put(name3, tag);
                if ((tag = type4.write(context, getter4.apply(value))) != null) builder.put(name4, tag);
                if ((tag = type5.write(context, getter5.apply(value))) != null) builder.put(name5, tag);
                return builder.build();
            }
        };
    }

    public static <R, P1, P2, P3, P4, P5, P6> BinaryTagSerializer<R> object(
            String name1, BinaryTagSerializer<P1> type1, Function<R, P1> getter1,
            String name2, BinaryTagSerializer<P2> type2, Function<R, P2> getter2,
            String name3, BinaryTagSerializer<P3> type3, Function<R, P3> getter3,
            String name4, BinaryTagSerializer<P4> type4, Function<R, P4> getter4,
            String name5, BinaryTagSerializer<P5> type5, Function<R, P5> getter5,
            String name6, BinaryTagSerializer<P6> type6, Function<R, P6> getter6,
            F6<P1, P2, P3, P4, P5, P6, R> ctor
    ) {
        return new ObjectBase<>(){
            @Override
            protected @NotNull R readObject(@NotNull Context context, @NotNull CompoundBinaryTag tag) {
                return ctor.apply(
                        type1.read(context, tag.get(name1)),
                        type2.read(context, tag.get(name2)),
                        type3.read(context, tag.get(name3)),
                        type4.read(context, tag.get(name4)),
                        type5.read(context, tag.get(name5)),
                        type6.read(context, tag.get(name6))
                );
            }

            @Override
            protected @NotNull CompoundBinaryTag writeObject(@NotNull Context context, @NotNull R value) {
                BinaryTag tag;
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                if ((tag = type1.write(context, getter1.apply(value))) != null) builder.put(name1, tag);
                if ((tag = type2.write(context, getter2.apply(value))) != null) builder.put(name2, tag);
                if ((tag = type3.write(context, getter3.apply(value))) != null) builder.put(name3, tag);
                if ((tag = type4.write(context, getter4.apply(value))) != null) builder.put(name4, tag);
                if ((tag = type5.write(context, getter5.apply(value))) != null) builder.put(name5, tag);
                if ((tag = type6.write(context, getter6.apply(value))) != null) builder.put(name6, tag);
                return builder.build();
            }
        };
    }

    public static <R, P1, P2, P3, P4, P5, P6, P7> BinaryTagSerializer<R> object(
            String name1, BinaryTagSerializer<P1> type1, Function<R, P1> getter1,
            String name2, BinaryTagSerializer<P2> type2, Function<R, P2> getter2,
            String name3, BinaryTagSerializer<P3> type3, Function<R, P3> getter3,
            String name4, BinaryTagSerializer<P4> type4, Function<R, P4> getter4,
            String name5, BinaryTagSerializer<P5> type5, Function<R, P5> getter5,
            String name6, BinaryTagSerializer<P6> type6, Function<R, P6> getter6,
            String name7, BinaryTagSerializer<P7> type7, Function<R, P7> getter7,
            F7<P1, P2, P3, P4, P5, P6, P7, R> ctor
    ) {
        return new ObjectBase<>(){
            @Override
            protected @NotNull R readObject(@NotNull Context context, @NotNull CompoundBinaryTag tag) {
                return ctor.apply(
                        type1.read(context, tag.get(name1)),
                        type2.read(context, tag.get(name2)),
                        type3.read(context, tag.get(name3)),
                        type4.read(context, tag.get(name4)),
                        type5.read(context, tag.get(name5)),
                        type6.read(context, tag.get(name6)),
                        type7.read(context, tag.get(name7))
                );
            }

            @Override
            protected @NotNull CompoundBinaryTag writeObject(@NotNull Context context, @NotNull R value) {
                BinaryTag tag;
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                if ((tag = type1.write(context, getter1.apply(value))) != null) builder.put(name1, tag);
                if ((tag = type2.write(context, getter2.apply(value))) != null) builder.put(name2, tag);
                if ((tag = type3.write(context, getter3.apply(value))) != null) builder.put(name3, tag);
                if ((tag = type4.write(context, getter4.apply(value))) != null) builder.put(name4, tag);
                if ((tag = type5.write(context, getter5.apply(value))) != null) builder.put(name5, tag);
                if ((tag = type6.write(context, getter6.apply(value))) != null) builder.put(name6, tag);
                if ((tag = type7.write(context, getter7.apply(value))) != null) builder.put(name7, tag);
                return builder.build();
            }
        };
    }

    public static <R, P1, P2, P3, P4, P5, P6, P7, P8> BinaryTagSerializer<R> object(
            String name1, BinaryTagSerializer<P1> type1, Function<R, P1> getter1,
            String name2, BinaryTagSerializer<P2> type2, Function<R, P2> getter2,
            String name3, BinaryTagSerializer<P3> type3, Function<R, P3> getter3,
            String name4, BinaryTagSerializer<P4> type4, Function<R, P4> getter4,
            String name5, BinaryTagSerializer<P5> type5, Function<R, P5> getter5,
            String name6, BinaryTagSerializer<P6> type6, Function<R, P6> getter6,
            String name7, BinaryTagSerializer<P7> type7, Function<R, P7> getter7,
            String name8, BinaryTagSerializer<P8> type8, Function<R, P8> getter8,
            F8<P1, P2, P3, P4, P5, P6, P7, P8, R> ctor
    ) {
        return new ObjectBase<>(){
            @Override
            protected @NotNull R readObject(@NotNull Context context, @NotNull CompoundBinaryTag tag) {
                return ctor.apply(
                        type1.read(context, tag.get(name1)),
                        type2.read(context, tag.get(name2)),
                        type3.read(context, tag.get(name3)),
                        type4.read(context, tag.get(name4)),
                        type5.read(context, tag.get(name5)),
                        type6.read(context, tag.get(name6)),
                        type7.read(context, tag.get(name7)),
                        type8.read(context, tag.get(name8))
                );
            }

            @Override
            protected @NotNull CompoundBinaryTag writeObject(@NotNull Context context, @NotNull R value) {
                BinaryTag tag;
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                if ((tag = type1.write(context, getter1.apply(value))) != null) builder.put(name1, tag);
                if ((tag = type2.write(context, getter2.apply(value))) != null) builder.put(name2, tag);
                if ((tag = type3.write(context, getter3.apply(value))) != null) builder.put(name3, tag);
                if ((tag = type4.write(context, getter4.apply(value))) != null) builder.put(name4, tag);
                if ((tag = type5.write(context, getter5.apply(value))) != null) builder.put(name5, tag);
                if ((tag = type6.write(context, getter6.apply(value))) != null) builder.put(name6, tag);
                if ((tag = type7.write(context, getter7.apply(value))) != null) builder.put(name7, tag);
                if ((tag = type8.write(context, getter8.apply(value))) != null) builder.put(name8, tag);
                return builder.build();
            }
        };
    }

    public static <R, P1, P2, P3, P4, P5, P6, P7, P8, P9> BinaryTagSerializer<R> object(
            String name1, BinaryTagSerializer<P1> type1, Function<R, P1> getter1,
            String name2, BinaryTagSerializer<P2> type2, Function<R, P2> getter2,
            String name3, BinaryTagSerializer<P3> type3, Function<R, P3> getter3,
            String name4, BinaryTagSerializer<P4> type4, Function<R, P4> getter4,
            String name5, BinaryTagSerializer<P5> type5, Function<R, P5> getter5,
            String name6, BinaryTagSerializer<P6> type6, Function<R, P6> getter6,
            String name7, BinaryTagSerializer<P7> type7, Function<R, P7> getter7,
            String name8, BinaryTagSerializer<P8> type8, Function<R, P8> getter8,
            String name9, BinaryTagSerializer<P9> type9, Function<R, P9> getter9,
            F9<P1, P2, P3, P4, P5, P6, P7, P8, P9, R> ctor
    ) {
        return new ObjectBase<>(){
            @Override
            protected @NotNull R readObject(@NotNull Context context, @NotNull CompoundBinaryTag tag) {
                return ctor.apply(
                        type1.read(context, tag.get(name1)),
                        type2.read(context, tag.get(name2)),
                        type3.read(context, tag.get(name3)),
                        type4.read(context, tag.get(name4)),
                        type5.read(context, tag.get(name5)),
                        type6.read(context, tag.get(name6)),
                        type7.read(context, tag.get(name7)),
                        type8.read(context, tag.get(name8)),
                        type9.read(context, tag.get(name9))
                );
            }

            @Override
            protected @NotNull CompoundBinaryTag writeObject(@NotNull Context context, @NotNull R value) {
                BinaryTag tag;
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                if ((tag = type1.write(context, getter1.apply(value))) != null) builder.put(name1, tag);
                if ((tag = type2.write(context, getter2.apply(value))) != null) builder.put(name2, tag);
                if ((tag = type3.write(context, getter3.apply(value))) != null) builder.put(name3, tag);
                if ((tag = type4.write(context, getter4.apply(value))) != null) builder.put(name4, tag);
                if ((tag = type5.write(context, getter5.apply(value))) != null) builder.put(name5, tag);
                if ((tag = type6.write(context, getter6.apply(value))) != null) builder.put(name6, tag);
                if ((tag = type7.write(context, getter7.apply(value))) != null) builder.put(name7, tag);
                if ((tag = type8.write(context, getter8.apply(value))) != null) builder.put(name8, tag);
                if ((tag = type9.write(context, getter9.apply(value))) != null) builder.put(name9, tag);
                return builder.build();
            }
        };
    }

    public static <R, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> BinaryTagSerializer<R> object(
            String name1, BinaryTagSerializer<P1> type1, Function<R, P1> getter1,
            String name2, BinaryTagSerializer<P2> type2, Function<R, P2> getter2,
            String name3, BinaryTagSerializer<P3> type3, Function<R, P3> getter3,
            String name4, BinaryTagSerializer<P4> type4, Function<R, P4> getter4,
            String name5, BinaryTagSerializer<P5> type5, Function<R, P5> getter5,
            String name6, BinaryTagSerializer<P6> type6, Function<R, P6> getter6,
            String name7, BinaryTagSerializer<P7> type7, Function<R, P7> getter7,
            String name8, BinaryTagSerializer<P8> type8, Function<R, P8> getter8,
            String name9, BinaryTagSerializer<P9> type9, Function<R, P9> getter9,
            String name10, BinaryTagSerializer<P10> type10, Function<R, P10> getter10,
            F10<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, R> ctor
    ) {
        return new ObjectBase<>(){
            @Override
            protected @NotNull R readObject(@NotNull Context context, @NotNull CompoundBinaryTag tag) {
                return ctor.apply(
                        type1.read(context, tag.get(name1)),
                        type2.read(context, tag.get(name2)),
                        type3.read(context, tag.get(name3)),
                        type4.read(context, tag.get(name4)),
                        type5.read(context, tag.get(name5)),
                        type6.read(context, tag.get(name6)),
                        type7.read(context, tag.get(name7)),
                        type8.read(context, tag.get(name8)),
                        type9.read(context, tag.get(name9)),
                        type10.read(context, tag.get(name10))
                );
            }

            @Override
            protected @NotNull CompoundBinaryTag writeObject(@NotNull Context context, @NotNull R value) {
                BinaryTag tag;
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                if ((tag = type1.write(context, getter1.apply(value))) != null) builder.put(name1, tag);
                if ((tag = type2.write(context, getter2.apply(value))) != null) builder.put(name2, tag);
                if ((tag = type3.write(context, getter3.apply(value))) != null) builder.put(name3, tag);
                if ((tag = type4.write(context, getter4.apply(value))) != null) builder.put(name4, tag);
                if ((tag = type5.write(context, getter5.apply(value))) != null) builder.put(name5, tag);
                if ((tag = type6.write(context, getter6.apply(value))) != null) builder.put(name6, tag);
                if ((tag = type7.write(context, getter7.apply(value))) != null) builder.put(name7, tag);
                if ((tag = type8.write(context, getter8.apply(value))) != null) builder.put(name8, tag);
                if ((tag = type9.write(context, getter9.apply(value))) != null) builder.put(name9, tag);
                if ((tag = type10.write(context, getter10.apply(value))) != null) builder.put(name10, tag);
                return builder.build();
            }
        };
    }


    // IMPLEMENTATION

    static abstract class ObjectBase<T> implements BinaryTagSerializer<T> {
        @Override
        public @NotNull T read(@NotNull Context context, @NotNull BinaryTag tag) {
            if (!(tag instanceof CompoundBinaryTag compound)) {
                throw new IllegalArgumentException("Expected a compound tag, got " + tag);
            }
            return readObject(context, compound);
        }

        @Override
        public @NotNull BinaryTag write(@NotNull Context context, @NotNull T value) {
            return writeObject(context, value);
        }

        protected abstract @NotNull T readObject(@NotNull Context context, @NotNull CompoundBinaryTag tag);

        protected abstract @NotNull CompoundBinaryTag writeObject(@NotNull Context context, @NotNull T value);
    }
}
