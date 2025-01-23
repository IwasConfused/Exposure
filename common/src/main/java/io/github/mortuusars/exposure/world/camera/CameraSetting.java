package io.github.mortuusars.exposure.world.camera;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record CameraSetting<T>(DataComponentType<T> component, T defaultValue) {
    public static final Codec<CameraSetting<?>> CODEC = ResourceLocation.CODEC.xmap(CameraSettings::byId, CameraSettings::idOf);

    public static final StreamCodec<ByteBuf, CameraSetting<?>> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, CameraSettings::idOf,
            CameraSettings::byId
    );

    // --

    public @Nullable T get(ItemStack stack) {
        return stack.get(component);
    }

    public Optional<T> getOptional(ItemStack stack) {
        return Optional.ofNullable(get(stack));
    }

    public T getOrDefault(ItemStack stack) {
        return stack.getOrDefault(component, defaultValue);
    }

    public T getOrElse(ItemStack stack, T defaultValue) {
        return stack.getOrDefault(component, defaultValue);
    }

    public CameraSetting<T> set(ItemStack stack, T value) {
        if (value instanceof Boolean bool && !bool) {
            stack.remove(component);
        } else {
            stack.set(component, value);
        }
        return this;
    }

    public void decodeAndSet(ItemStack stack, RegistryAccess registryAccess, byte[] bytes) {
        T value = decodeValue(registryAccess, bytes);
        set(stack, value);
    }

    // --

    public byte[] encodeValue(RegistryAccess registryAccess, T value) {
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess);
        try {
            component.streamCodec().encode(buffer, value);
            return buffer.array().clone();
        } finally {
            buffer.release();
        }
    }

    public T decodeValue(RegistryAccess registryAccess, byte[] bytes) {
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess);
        try {
            buffer.writeBytes(bytes);
            return component.streamCodec().decode(buffer);
        } finally {
            buffer.release();
        }
    }
}
