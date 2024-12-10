package io.github.mortuusars.exposure.neoforge;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ItemAbilities;

import java.util.List;
import java.util.function.Consumer;

public class PlatformHelperImpl {
    public static boolean canShear(ItemStack stack) {
        return stack.canPerformAction(ItemAbilities.SHEARS_CARVE);
    }

    public static boolean canStrip(ItemStack stack) {
        return stack.canPerformAction(ItemAbilities.AXE_STRIP);
    }

    public static void openMenu(ServerPlayer serverPlayer, MenuProvider menuProvider, Consumer<RegistryFriendlyByteBuf> extraDataWriter) {
        serverPlayer.openMenu(menuProvider, extraDataWriter);
    }

    public static List<String> getDefaultSpoutDevelopmentColorSequence() {
        return List.of(
                "{FluidName:\"create:potion\",Amount:250,Tag:{Potion:\"minecraft:awkward\"}}",
                "{FluidName:\"create:potion\",Amount:250,Tag:{Potion:\"minecraft:thick\"}}",
                "{FluidName:\"create:potion\",Amount:250,Tag:{Potion:\"minecraft:mundane\"}}");
    }

    public static List<String> getDefaultSpoutDevelopmentBWSequence() {
        return List.of(
                "{FluidName:\"minecraft:water\",Amount:250}");
    }

    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    public static boolean isInDevEnv() {
        return !FMLEnvironment.production;
    }

//    public static boolean fireShutterOpeningEvent(Player player, ItemStack cameraStack, int lightLevel, boolean shouldFlashFire) {
//        ShutterOpeningEvent event = new ShutterOpeningEvent(player, cameraStack, lightLevel, shouldFlashFire);
//        return MinecraftForge.EVENT_BUS.post(event);
//    }
//
//    public static void fireModifyFrameDataEvent(ServerPlayer player, ItemStack cameraStack, CompoundTag frame, List<Entity> entitiesInFrame) {
//        ModifyFrameDataEvent event = new ModifyFrameDataEvent(player, cameraStack, frame, entitiesInFrame);
//        MinecraftForge.EVENT_BUS.post(event);
//    }
//
//    public static void fireFrameAddedEvent(ServerPlayer player, ItemStack cameraStack, CompoundTag frame) {
//        FrameAddedEvent event = new FrameAddedEvent(player, cameraStack, frame);
//        MinecraftForge.EVENT_BUS.post(event);
//    }
}
