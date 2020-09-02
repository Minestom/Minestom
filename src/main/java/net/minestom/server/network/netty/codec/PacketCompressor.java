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
import net.minestom.server.utils.Utils;

import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

// TODO Optimize
public class PacketCompressor extends ByteToMessageCodec<ByteBuf> {

    private final byte[] buffer = new byte[8192];

    private final int threshold;

    private final Inflater inflater;
    private final Deflater deflater;

    public PacketCompressor(int threshold) {
        this.inflater = new Inflater();
        this.deflater = new Deflater();

        this.threshold = threshold;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf from, ByteBuf to) {
        int i = from.readableBytes();

        if (i < this.threshold) {
            Utils.writeVarIntBuf(to, 0);
            to.writeBytes(from);
        } else {
            byte[] abyte = new byte[i];
            from.readBytes(abyte);

            Utils.writeVarIntBuf(to, abyte.length);
            this.deflater.setInput(abyte, 0, i);
            this.deflater.finish();

            while (!this.deflater.finished()) {
                int j = this.deflater.deflate(this.buffer);

                to.writeBytes(this.buffer, 0, j);
            }

            this.deflater.reset();
        }
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

                if (i > 2097152) {
                    throw new DecoderException("Badly compressed packet - size of " + i + " is larger than protocol maximum of 2097152");
                }

                byte[] abyte = new byte[buf.readableBytes()];
                buf.readBytes(abyte);

                this.inflater.setInput(abyte);
                byte[] abyte1 = new byte[i];

                this.inflater.inflate(abyte1);
                out.add(Unpooled.wrappedBuffer(abyte1));

                this.inflater.reset();
            }
        }
    }
}
