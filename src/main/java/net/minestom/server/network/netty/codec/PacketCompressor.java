/*
 * Copyright (2020) [artem]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.minestom.server.network.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.DecoderException;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.Utils;

import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class PacketCompressor extends ByteToMessageCodec<ByteBuf> {

    private final static int MAX_SIZE = 2097152;

    private final int threshold;

    private final byte[] buffer = new byte[8192];

    private final Deflater deflater = new Deflater(3);
    private final Inflater inflater = new Inflater();

    public PacketCompressor(int threshold) {
        this.threshold = threshold;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf from, ByteBuf to) {
        PacketUtils.compressBuffer(deflater, buffer, from, to);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        if (buf.readableBytes() != 0) {
            final int i = Utils.readVarInt(buf);

            if (i == 0) {
                out.add(buf.readRetainedSlice(buf.readableBytes()));
            } else {
                if (i < this.threshold) {
                    throw new DecoderException("Badly compressed packet - size of " + i + " is below server threshold of " + this.threshold);
                }

                if (i > MAX_SIZE) {
                    throw new DecoderException("Badly compressed packet - size of " + i + " is larger than protocol maximum of " + MAX_SIZE);
                }

                // TODO optimize to do not initialize arrays each time

                byte[] input = new byte[buf.readableBytes()];
                buf.readBytes(input);

                inflater.setInput(input);
                byte[] output = new byte[i];
                inflater.inflate(output);
                inflater.reset();

                out.add(Unpooled.wrappedBuffer(output));
            }
        }
    }
}
