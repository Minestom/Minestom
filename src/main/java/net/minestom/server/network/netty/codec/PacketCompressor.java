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

    private final Deflater deflater = new Deflater();
    private final Inflater inflater = new Inflater();

    public PacketCompressor(int threshold) {
        this.threshold = threshold;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf from, ByteBuf to) {
        PacketUtils.compressBuffer(deflater, from, to);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() != 0) {
            final int claimedUncompressedSize = Utils.readVarInt(in);

            if (claimedUncompressedSize == 0) {
                out.add(in.readRetainedSlice(in.readableBytes()));
            } else {
                if (claimedUncompressedSize < this.threshold) {
                    throw new DecoderException("Badly compressed packet - size of " + claimedUncompressedSize + " is below server threshold of " + this.threshold);
                }

                if (claimedUncompressedSize > MAX_SIZE) {
                    throw new DecoderException("Badly compressed packet - size of " + claimedUncompressedSize + " is larger than protocol maximum of " + MAX_SIZE);
                }

                // TODO optimize to do not initialize arrays each time

                byte[] input = new byte[in.readableBytes()];
                in.readBytes(input);

                inflater.setInput(input);
                byte[] output = new byte[claimedUncompressedSize];
                inflater.inflate(output);
                inflater.reset();

                out.add(Unpooled.wrappedBuffer(output));
            }
        }
    }
}
