package io.github.mortuusars.exposure.world.item.part;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.component.CompositionGuide;
import io.github.mortuusars.exposure.world.camera.component.CompositionGuides;
import io.github.mortuusars.exposure.world.camera.component.FlashMode;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.server.SetCameraSettingC2SP;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public record CameraSetting<T>(DataComponentType<T> component, T defaultValue) {
    public static final Codec<CameraSetting<?>> CODEC = ResourceLocation.CODEC.xmap(CameraSetting::byId, CameraSetting::idOf);

    public static final StreamCodec<ByteBuf, CameraSetting<?>> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, CameraSetting::idOf,
            CameraSetting::byId
    );

    private static final Map<ResourceLocation, CameraSetting<?>> REGISTRY = new HashMap<>();

    public static <T> CameraSetting<T> register(ResourceLocation id, CameraSetting<T> setting) {
        Preconditions.checkArgument(!REGISTRY.containsKey(id), "Setting with id '%s' is already registered.", id);
        REGISTRY.put(id, setting);
        return setting;
    }

    public static CameraSetting<?> byId(ResourceLocation id) {
        @Nullable CameraSetting<?> setting = REGISTRY.get(id);
        if (setting == null) {
            throw new IllegalStateException("Setting with id '" + id + "' is not registered.");
        }
        return setting;
    }

    public static ResourceLocation idOf(CameraSetting<?> setting) {
        for (Map.Entry<ResourceLocation, CameraSetting<?>> entry : REGISTRY.entrySet()) {
            if (entry.getValue().equals(setting)) {
                return entry.getKey();
            }
        }
        throw new IllegalStateException("Setting is not registered.");
    }

    // --

    public static final CameraSetting<Boolean> SELFIE_MODE = register(Exposure.resource("selfie"),
            new CameraSetting<>(Exposure.DataComponents.SELFIE_MODE, false));
    public static final CameraSetting<Float> ZOOM = register(Exposure.resource("zoom"),
            new CameraSetting<>(Exposure.DataComponents.ZOOM, 0f));
    public static final CameraSetting<ShutterSpeed> SHUTTER_SPEED = register(Exposure.resource("shutter_speed"),
            new CameraSetting<>(Exposure.DataComponents.SHUTTER_SPEED, ShutterSpeed.DEFAULT));
    public static final CameraSetting<CompositionGuide> COMPOSITION_GUIDE = register(Exposure.resource("composition_guide"),
            new CameraSetting<>(Exposure.DataComponents.COMPOSITION_GUIDE, CompositionGuides.NONE));
    public static final CameraSetting<FlashMode> FLASH_MODE = register(Exposure.resource("flash_mode"),
            new CameraSetting<>(Exposure.DataComponents.FLASH_MODE, FlashMode.OFF));

    // --

    public @Nullable T get(ItemStack stack) {
        return stack.get(component);
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

    public CameraSetting<T> set(ItemStack stack, RegistryFriendlyByteBuf buffer) {
        T value = component.streamCodec().decode(buffer);
        set(stack, value);
        return this;
    }

    public void setAndSync(Player player, T value) {
        player.getActiveExposureCamera().ifPresent(camera -> {
            set(camera.getItemStack(), value);

            RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.level().registryAccess());
            component.streamCodec().encode(buffer, value);
            byte[] bytes = buffer.array();
            Packets.sendToServer(new SetCameraSettingC2SP(this, bytes));
            buffer.clear();
        });
    }
}
