package io.github.mortuusars.exposure.item.part;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.camera.CameraAccessor;
import io.github.mortuusars.exposure.core.camera.component.CompositionGuide;
import io.github.mortuusars.exposure.core.camera.component.FlashMode;
import io.github.mortuusars.exposure.core.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.server.CameraSetSettingC2SP;
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

public record Setting<T>(DataComponentType<T> component) {
    public static final Codec<Setting<?>> CODEC = ResourceLocation.CODEC.xmap(Setting::byId, Setting::idOf);

    public static final StreamCodec<ByteBuf, Setting<?>> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, Setting::idOf,
            Setting::byId
    );

    private static final Map<ResourceLocation, Setting<?>> REGISTRY = new HashMap<>();

    public static <T> Setting<T> register(ResourceLocation id, Setting<T> setting) {
        Preconditions.checkArgument(!REGISTRY.containsKey(id), "Setting with id '%s' is already registered.", id);
        REGISTRY.put(id, setting);
        return setting;
    }

    public static Setting<?> byId(ResourceLocation id) {
        @Nullable Setting<?> setting = REGISTRY.get(id);
        if (setting == null) {
            throw new IllegalStateException("Setting with id '" + id + "' is not registered.");
        }
        return setting;
    }

    public static ResourceLocation idOf(Setting<?> setting) {
        for (Map.Entry<ResourceLocation, Setting<?>> entry : REGISTRY.entrySet()) {
            if (entry.getValue().equals(setting)) {
                return entry.getKey();
            }
        }
        throw new IllegalStateException("Setting is not registered.");
    }

    // --

    public static final Setting<Boolean> ACTIVE =
            register(Exposure.resource("active"), new Setting<>(Exposure.DataComponents.CAMERA_ACTIVE));
    public static final Setting<Boolean> SELFIE =
            register(Exposure.resource("selfie"), new Setting<>(Exposure.DataComponents.SELFIE_MODE));
    public static final Setting<Double> ZOOM =
            register(Exposure.resource("zoom"), new Setting<>(Exposure.DataComponents.ZOOM));
    public static final Setting<ShutterSpeed> SHUTTER_SPEED =
            register(Exposure.resource("shutter_speed"), new Setting<>(Exposure.DataComponents.SHUTTER_SPEED));
    public static final Setting<CompositionGuide> COMPOSITION_GUIDE =
            register(Exposure.resource("composition_guide"), new Setting<>(Exposure.DataComponents.COMPOSITION_GUIDE));
    public static final Setting<FlashMode> FLASH_MODE =
            register(Exposure.resource("flash_mode"), new Setting<>(Exposure.DataComponents.FLASH_MODE));

    // --

    public @Nullable T get(ItemStack stack) {
        return stack.get(component);
    }

    public T getOrDefault(ItemStack stack, T defaultValue) {
        return stack.getOrDefault(component, defaultValue);
    }

    public Setting<T> set(ItemStack stack, T value) {
        if (value instanceof Boolean bool && !bool) {
            stack.remove(component);
        } else {
            stack.set(component, value);
        }
        return this;
    }

    public Setting<T> set(ItemStack stack, RegistryFriendlyByteBuf buffer) {
        T value = component.streamCodec().decode(buffer);
        set(stack, value);
        return this;
    }

    public void setAndSync(CameraAccessor<?> accessor, Player player, T value) {
        accessor.ifPresent(player, camera -> {
            set(camera.getItemStack(), value);

            RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.level().registryAccess());
            component.streamCodec().encode(buffer, value);
            byte[] bytes = buffer.array();
            Packets.sendToServer(new CameraSetSettingC2SP(accessor, this, bytes));
            buffer.clear();
        });
    }
}
