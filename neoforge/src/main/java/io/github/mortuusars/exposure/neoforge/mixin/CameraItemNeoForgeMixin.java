package io.github.mortuusars.exposure.neoforge.mixin;

import io.github.mortuusars.exposure.world.item.CameraItem;
import io.github.mortuusars.exposure.neoforge.item.CameraItemForgeClientExtensions;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;

@Mixin(value = CameraItem.class, remap = false)
public abstract class CameraItemNeoForgeMixin extends Item implements IItemExtension {
    public CameraItemNeoForgeMixin(Properties properties) {
        super(properties);
    }

//    @Shadow abstract InteractionResult useCamera(Player player, InteractionHand hand);

//    @Override
//    public @NotNull InteractionResult onItemUseFirst(@NotNull ItemStack stack, UseOnContext context) {
//        Player player = context.getPlayer();
//        if (player != null) {
//            InteractionHand hand = context.getHand();
//
//            //TODO: both hands
////            if (hand == InteractionHand.MAIN_HAND && Camera.getCamera(player)
////                    .filter(c -> c instanceof CameraInHand<?>)
////                    .map(c -> ((CameraInHand<?>) c).getHand() == InteractionHand.OFF_HAND).orElse(false)) {
////                return InteractionResult.PASS;
////            }
//
////            return useCamera(player, hand);
//        }
//        return InteractionResult.CONSUME; // To not play attack animation.
//    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
        if (oldStack.getItem() instanceof CameraItem oldItem && newStack.getItem() instanceof CameraItem newItem) {
            return !oldItem.getOrCreateID(oldStack).equals(newItem.getOrCreateID(newStack));
        }
        return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.CUSTOM;
    }

    @SuppressWarnings("removal")
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(CameraItemForgeClientExtensions.INSTANCE);
    }
}
