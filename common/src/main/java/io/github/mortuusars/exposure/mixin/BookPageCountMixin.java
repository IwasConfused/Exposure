package io.github.mortuusars.exposure.mixin;

import net.minecraft.world.item.WrittenBookItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WrittenBookItem.class)
public abstract class BookPageCountMixin {
//    @Inject(method = "getPageCount", at = @At("HEAD"), cancellable = true)
//    private static void getPageCount(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
//        if (stack.getItem() instanceof AlbumItem albumItem) {
//            cir.setReturnValue(albumItem.getPages(stack).size());
//        }
//    }
}
