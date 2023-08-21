package io.github.mortuusars.exposure.menu;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.menu.inventory.CameraItemStackHandler;
import io.github.mortuusars.exposure.util.ItemAndStack;
import io.github.mortuusars.exposure.util.OnePerPlayerSounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CameraAttachmentsMenu extends AbstractContainerMenu {
    private final ItemAndStack<CameraItem> camera;
    private final int attachmentSlots;
    private final int slotMatchingItem;

    public CameraAttachmentsMenu(int containerId, Inventory playerInventory, ItemStack cameraStack) {
        super(Exposure.MenuTypes.CAMERA.get(), containerId);
        this.camera = new ItemAndStack<>(cameraStack);
        List<CameraItem.AttachmentType> attachmentTypes = camera.getItem().getAttachmentTypes(camera.getStack());
        this.slotMatchingItem = playerInventory.findSlotMatchingItem(cameraStack);


        IItemHandler itemStackHandler = new CameraItemStackHandler(camera);

        int attachmentSlots = 0;

        if (attachmentTypes.contains(CameraItem.FILM_ATTACHMENT)) {
            addSlot(new SlotItemHandler(itemStackHandler, CameraItem.FILM_ATTACHMENT.slot(), 13, 42) {
                @Override
                public void set(@NotNull ItemStack stack) {
                    super.set(stack);
                    if (!stack.isEmpty() && playerInventory.player.getLevel().isClientSide)
                        OnePerPlayerSounds.play(playerInventory.player, Exposure.SoundEvents.FILM_ADVANCE.get(),
                                SoundSource.PLAYERS, 0.9f, 1f);
                }
            });
            attachmentSlots++;
        }

        if (attachmentTypes.contains(CameraItem.FLASH_ATTACHMENT)) {
            addSlot(new SlotItemHandler(itemStackHandler, CameraItem.FLASH_ATTACHMENT.slot(), 147, 15) {
                @Override
                public void set(@NotNull ItemStack stack) {
                    super.set(stack);
                    if (playerInventory.player.getLevel().isClientSide)
                        OnePerPlayerSounds.play(playerInventory.player, Exposure.SoundEvents.CAMERA_BUTTON_CLICK.get(),
                                SoundSource.PLAYERS, stack.isEmpty() ? 0.6f : 0.8f, stack.isEmpty() ? 1.2f : 0.8f);
                }
            });
            attachmentSlots++;
        }

        if (attachmentTypes.contains(CameraItem.LENS_ATTACHMENT)) {
            addSlot(new SlotItemHandler(itemStackHandler, CameraItem.LENS_ATTACHMENT.slot(), 147, 43) {
                @Override
                public void set(@NotNull ItemStack stack) {
                    super.set(stack);
                    if (playerInventory.player.getLevel().isClientSide)
                        OnePerPlayerSounds.play(playerInventory.player, stack.isEmpty() ?
                                SoundEvents.SPYGLASS_STOP_USING : SoundEvents.SPYGLASS_USE, SoundSource.PLAYERS, 0.9f, 1f);
                }
            });
            attachmentSlots++;
        }
        if (attachmentTypes.contains(CameraItem.FILTER_ATTACHMENT)) {
            addSlot(new SlotItemHandler(itemStackHandler, CameraItem.FILTER_ATTACHMENT.slot(), 147, 71) {
                @Override
                public void set(@NotNull ItemStack stack) {
                    super.set(stack);
                    if (!stack.isEmpty() && playerInventory.player.getLevel().isClientSide)
                        OnePerPlayerSounds.play(playerInventory.player, Exposure.SoundEvents.FILTER_PLACE.get(), SoundSource.PLAYERS, 0.8f,
                                playerInventory.player.getLevel().getRandom().nextFloat() * 0.2f + 0.9f);
                }
            });
            attachmentSlots++;
        }

        this.attachmentSlots = attachmentSlots;

        //Player Inventory
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, (column + row * 9) + 9, column * 18 + 8, 103 + row * 18));
            }
        }

        //Hotbar
        for (int slot = 0; slot < 9; slot++) {
            addSlot(new Slot(playerInventory, slot, slot * 18 + 8, 161));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slotIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();
            if (slotIndex < attachmentSlots) {
                if (!this.moveItemStackTo(slotStack, attachmentSlots, this.slots.size(), true))
                    return ItemStack.EMPTY;
            }
            else {
                if (!this.moveItemStackTo(slotStack, 0, attachmentSlots, false))
                    return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty())
                slot.set(ItemStack.EMPTY);
            else
                slot.setChanged();
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return !Exposure.getCamera().isActive(player) && player.getInventory().getItem(slotMatchingItem).getItem() instanceof CameraItem;
    }

    public static CameraAttachmentsMenu fromBuffer(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new CameraAttachmentsMenu(containerId, playerInventory, buffer.readItem());
    }
}
