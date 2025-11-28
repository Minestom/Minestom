/**
 * Codecs are ways of expressing objects in intermediary formats.
 * The core abstraction is the {@link net.minestom.server.codec.Transcoder} interface,
 * which supports multiple formats such as NBT, JSON and Java objects.
 * <p>
 * Codecs define conversion logic for individual types, while transcoders handle the representation and
 * construction of data structures like lists and maps. This design allows super extensible, and type-safe
 * serialization and deserialization across different data backends.
 * </p>
 * <p>
 * The codec system is built on codecs, where multiple codecs can be combined into a {@link net.minestom.server.codec.StructCodec}
 * to represent complex data structures. Each codec can be used to encode and decode values in
 * various formats, such as JSON or NBT, using the {@link net.minestom.server.codec.Transcoder} interface.
 * </p>
 * <p>
 * See {@link net.minestom.server.codec.Transcoder} for format details and available implementations.
 * </p>
 * A quick example is below; keep in mind this is a simplified example of the possibilities.
 * <pre>{@code
 * record MyType(int id, @Nullable String name) {
 *     static final StructCodec<MyType> CODEC = StructCodec.struct(
 *             "id", Codec.INT, MyType::id,
 *             "name", Codec.STRING.optional(), MyType::name,
 *             MyType::new
 *     );
 * }
 *
 * MyType value = new MyType(42, "Example"); // Or use a null name for no name.
 * // Encoding to JSON
 * JsonElement encoded = MyType.CODEC.encode(Transcoder.JSON, value).orElseThrow();
 * // Decoding from JSON
 * MyType decoded = MyType.CODEC.decode(Transcoder.JSON, encoded).orElseThrow();
 * }</pre>
 */
@NotNullByDefault
package net.minestom.server.codec;

import org.jetbrains.annotations.NotNullByDefault;