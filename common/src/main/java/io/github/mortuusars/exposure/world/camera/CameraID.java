package io.github.mortuusars.exposure.world.camera;

import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.Exposure;
import io.netty.buffer.ByteBuf;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public record CameraID(UUID uuid) {
    public static final Codec<CameraID> CODEC = UUIDUtil.CODEC.xmap(CameraID::new, CameraID::uuid);
    public static final StreamCodec<ByteBuf, CameraID> STREAM_CODEC = UUIDUtil.STREAM_CODEC.map(CameraID::new, CameraID::uuid);

    public static CameraID create() {
        return new CameraID(UUID.randomUUID());
    }

    public static CameraID ofStack(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.CAMERA_ID, new CameraID(Util.NIL_UUID));
    }

    public boolean matches(ItemStack stack) {
        return equals(stack.get(Exposure.DataComponents.CAMERA_ID));
    }
}
