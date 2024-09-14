package io.github.mortuusars.exposure.neoforge.mixin;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.block.entity.LightroomBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = LightroomBlockEntity.class, remap = false)
public abstract class LightroomBlockEntityForgeMixin extends BaseContainerBlockEntity implements WorldlyContainer {
    protected LightroomBlockEntityForgeMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    private static final BlockCapability<IItemHandler, @Nullable Direction> ITEM_HANDLER_BLOCK =
            BlockCapability.createSided(Exposure.resource("lightroom_item_handler"), IItemHandler.class);

    // Declare the field:
    private BlockCapabilityCache<IItemHandler, @Nullable Direction> capabilityCache;

// Later, for example in `onLoad` for a block entity:


    @Override
    public void onLoad() {
        super.onLoad();

        if (level instanceof ServerLevel serverLevel) {
            capabilityCache = BlockCapabilityCache.create(ITEM_HANDLER_BLOCK, serverLevel, worldPosition, Direction.NORTH);
        }
    }

//    @Unique
//    private final LazyOptional<? extends IItemHandler>[] exposure$handlers = new SidedInvWrapper().create(this, Direction.values());



//    @Override
//    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
//        if (!this.remove && facing != null && capability == Capabilities.ItemHandler.ITEM)
//            return exposure$handlers[facing.ordinal()].cast();
//        return super.getCapability(capability, facing);
//    }

//    @Override
//    public void setRemoved() {
//        super.setRemoved();
//        for (LazyOptional<? extends IItemHandler> handler : exposure$handlers)
//            handler.invalidate();
//    }
}
