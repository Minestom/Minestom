package net.minestom.server.tag;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static java.util.Map.entry;

final class TagRecord {
    static final Map<Class<?>, Function<String, Tag<?>>> SUPPORTED_TYPES = Map.ofEntries(
            entry(Byte.class, Tag::Byte), entry(byte.class, Tag::Byte),
            entry(Boolean.class, Tag::Boolean), entry(boolean.class, Tag::Boolean),
            entry(Short.class, Tag::Short), entry(short.class, Tag::Short),
            entry(Integer.class, Tag::Integer), entry(int.class, Tag::Integer),
            entry(Long.class, Tag::Long), entry(long.class, Tag::Long),
            entry(Float.class, Tag::Float), entry(float.class, Tag::Float),
            entry(Double.class, Tag::Double), entry(double.class, Tag::Double),
            entry(String.class, Tag::String),

            entry(UUID.class, Tag::UUID),
            entry(ItemStack.class, Tag::ItemStack),
            entry(Component.class, Tag::Component));

    static final ClassValue<Serializer<? extends Record>> serializers = new ClassValue<>() {
        @Override
        protected Serializer<? extends Record> computeValue(Class<?> type) {
            assert type.isRecord();
            final RecordComponent[] components = type.getRecordComponents();
            final Entry[] entries = Arrays.stream(components)
                    .map(recordComponent -> {
                        final String componentName = recordComponent.getName();
                        final Class<?> componentType = recordComponent.getType();
                        final Tag<?> tag;
                        if (componentType.isRecord()) {
                            tag = Tag.Structure(componentName, serializers.get(componentType));
                        } else if (NBT.class.isAssignableFrom(componentType)) {
                            tag = Tag.NBT(componentName);
                        } else {
                            final var fun = SUPPORTED_TYPES.get(componentType);
                            if (fun == null)
                                throw new IllegalArgumentException("Unsupported type: " + componentType);
                            tag = fun.apply(componentName);
                        }
                        return new Entry(recordComponent, (Tag<Object>) tag);
                    }).toArray(Entry[]::new);
            Constructor<?> constructor;
            try {
                constructor = type.getDeclaredConstructor(Arrays.stream(components).map(RecordComponent::getType).toArray(Class[]::new));
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            return new Serializer<>(Constructor.class.cast(constructor), entries);
        }
    };

    static <T extends Record> @NotNull Serializer<T> serializer(@NotNull Class<T> type) {
        assert type.isRecord();
        //noinspection unchecked
        return (Serializer<T>) serializers.get(type);
    }

    static final class Serializer<T extends Record> implements TagSerializer<T> {
        final Constructor<T> constructor;
        final Entry[] entries;
        final Serializers.Entry<T, NBTCompound> serializerEntry;

        Serializer(Constructor<T> constructor, Entry[] entries) {
            this.constructor = constructor;
            this.entries = entries;
            this.serializerEntry = Serializers.fromTagSerializer(this);
        }

        @Override
        public @Nullable T read(@NotNull TagReadable reader) {
            Object[] components = new Object[entries.length];
            for (int i = 0; i < components.length; i++) {
                final Entry entry = entries[i];
                Object component = reader.getTag(entry.tag);
                if (component == null) return null;
                components[i] = component;
            }
            try {
                return constructor.newInstance(components);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void write(@NotNull TagWritable writer, @NotNull T value) {
            try {
                for (Entry entry : entries) {
                    final Object component = entry.component.getAccessor().invoke(value);
                    writer.setTag(entry.tag, component);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    record Entry(RecordComponent component, Tag<Object> tag) {
    }
}
