package io.github.mortuusars.exposure.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;

public class Codecs {
    public static Codec<int[]> intArrayCodec(int minSize, int maxSize) {
        return new Codec<>() {
            @Override
            public <T> DataResult<T> encode(int[] input, DynamicOps<T> ops, T prefix) {
                if (input.length < minSize || input.length > maxSize)
                    return DataResult.error(() -> "Array size must be between " + minSize + " and " + maxSize + ", got: " + input.length);
                ListBuilder<T> builder = ops.listBuilder();
                for (int inp : input)
                    builder.add(ops.createInt(inp));
                return builder.build(prefix);
            }

            @Override
            public <T> DataResult<Pair<int[], T>> decode(DynamicOps<T> ops, T input) {
                return ops.getIntStream(input)
                        .flatMap(stream -> {
                            int[] array = stream.toArray();
                            if (array.length < minSize || array.length > maxSize)
                                return DataResult.error(() -> "Array size must be between " + minSize + " and " + maxSize + ", got: " + array.length);
                            return DataResult.success(Pair.of(array, input));
                        });
            }
        };
    }
}
