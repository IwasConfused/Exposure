package io.github.mortuusars.exposure.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;

public class ByteArrayUtils {
    public static final Codec<byte[]> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<T> encode(byte[] input, DynamicOps<T> ops, T prefix) {
            ListBuilder<T> builder = ops.listBuilder();
            for (byte inp : input)
                builder.add(ops.createByte(inp));
            return builder.build(prefix);
        }

        @Override
        public <T> DataResult<Pair<byte[], T>> decode(DynamicOps<T> ops, T input) {
            return ops.getByteBuffer(input).map(t -> Pair.of(t.array(), input));
        }
    };

    public static byte[][] splitToParts(byte[] input, int partSize) {
        int parts = (int)Math.ceil(input.length / (double)partSize);
        byte[][] output = new byte[parts][];

        for(int part = 0; part < parts; part++) {
            int start = part * partSize;
            int length = Math.min(input.length - start, partSize);

            byte[] bytes = new byte[length];
            System.arraycopy(input, start, bytes, 0, length);
            output[part] = bytes;
        }

        return output;
    }
}
