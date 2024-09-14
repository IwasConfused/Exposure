package io.github.mortuusars.exposure.item;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.item.component.album.SignedAlbumContent;
import io.github.mortuusars.exposure.menu.AlbumMenu;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SignedAlbumItem extends Item {
    public SignedAlbumItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull Component getName(ItemStack stack) {
        @Nullable SignedAlbumContent content = stack.get(Exposure.DataComponents.SIGNED_ALBUM_CONTENT);
        if (content != null) {
            String title = content.title();
            if (!StringUtil.isBlank(title)) {
                return Component.literal(title);
            }
        }

        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        @Nullable SignedAlbumContent content = stack.get(Exposure.DataComponents.SIGNED_ALBUM_CONTENT);

        if (content != null) {
            String author = content.author();
            if (!StringUtil.isBlank(author)) {
                tooltipComponents.add(Component.translatable("gui.exposure.album.by_author", author).withStyle(ChatFormatting.GRAY));
            }

            if (Config.Client.ALBUM_SHOW_PHOTOS_COUNT.get()) {
                int photographsCount = (int)content.pages().stream().filter(page -> !page.photograph().isEmpty()).count();
                if (photographsCount > 0)
                    tooltipComponents.add(Component.translatable("item.exposure.album.tooltip.photos_count", photographsCount));
            }
        }

    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);

        if (player instanceof ServerPlayer serverPlayer) {
            int albumSlot = usedHand == InteractionHand.OFF_HAND ? Inventory.SLOT_OFFHAND : player.getInventory().selected;
            open(serverPlayer, itemStack, albumSlot);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        BlockPos blockPos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState blockState = level.getBlockState(blockPos);
        if (blockState.is(Blocks.LECTERN))
            return LecternBlock.tryPlaceBook(context.getPlayer(), level, blockPos, blockState,
                    context.getItemInHand()) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
        return InteractionResult.PASS;
    }

    public void open(ServerPlayer player, ItemStack albumStack, int albumSlot) {
        MenuProvider menuProvider = new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return albumStack.getHoverName();
            }

            @Override
            public @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
                //TODO: Signed Menu
                return new AlbumMenu(containerId, playerInventory, albumSlot);
            }
        };

        PlatformHelper.openMenu(player, menuProvider, buffer -> {
            buffer.writeVarInt(albumSlot);
//            ItemStack.STREAM_CODEC.encode(buffer, albumStack);
        });
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return Config.Client.SIGNED_ALBUM_GLINT.get();
    }
}
