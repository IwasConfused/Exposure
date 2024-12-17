package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.core.camera.ActiveCameraHolder;
import io.github.mortuusars.exposure.core.camera.NewCamera;
import io.github.mortuusars.exposure.item.CameraItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Player.class)
@SuppressWarnings("AddedMixinMembersNamePattern")
public abstract class PlayerMixin extends LivingEntity implements ActiveCameraHolder {
    @Unique
    @Nullable
    private NewCamera activeCamera;

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public Optional<NewCamera> getActiveCamera() {
        return Optional.ofNullable(activeCamera);
    }

    @Override
    public void setActiveCamera(@Nullable NewCamera camera) {
        activeCamera = camera;
    }

    @Override
    public void removeActiveCamera() {
        setActiveCamera(null);
    }

    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At(value = "RETURN"))
    void onDrop(ItemStack droppedItem, boolean dropAround, boolean includeThrowerName, CallbackInfoReturnable<ItemEntity> cir) {
        if (droppedItem.getItem() instanceof CameraItem cameraItem && cameraItem.isActive(droppedItem)) {
            cameraItem.deactivate((Player)(Object)this, droppedItem);
        }
    }
}
