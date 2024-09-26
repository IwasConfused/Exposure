package io.github.mortuusars.exposure;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.mortuusars.exposure.item.component.ExposureFrame;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public class PlatformHelper {
    @ExpectPlatform
    public static boolean canShear(ItemStack stack) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean canStrip(ItemStack stack) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void openMenu(ServerPlayer serverPlayer, MenuProvider menuProvider, Consumer<RegistryFriendlyByteBuf> extraDataWriter) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static List<String> getDefaultSpoutDevelopmentColorSequence() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static List<String> getDefaultSpoutDevelopmentBWSequence() {
        throw new AssertionError();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @ExpectPlatform
    public static boolean isModLoaded(String modId) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean fireShutterOpeningEvent(Player player, ItemStack cameraStack, int lightLevel, boolean shouldFlashFire) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void fireModifyFrameDataEvent(ServerPlayer player, ItemStack cameraStack, ExposureFrame.Mutable frame, List<Entity> entitiesInFrame) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void fireFrameAddedEvent(ServerPlayer player, ItemStack cameraStack, ExposureFrame frame) {
        throw new AssertionError();
    }
}
