package io.github.mortuusars.exposure.menu;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.item.part.Attachment;
import io.github.mortuusars.exposure.item.OldCameraItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.List;
import java.util.Map;

public class CameraAttachmentsMenu extends AbstractContainerMenu {
    protected final Player player;
    protected final int cameraSlotIndex;
    protected final ItemAndStack<CameraItem> camera;
    protected final List<Attachment<?>> attachments;

    protected boolean clientContentsInitialized;

    public CameraAttachmentsMenu(int containerId, Inventory playerInventory, int cameraSlotIndex) {
        super(Exposure.MenuTypes.CAMERA.get(), containerId);

        ItemStack cameraStack = playerInventory.items.get(cameraSlotIndex);
        Preconditions.checkState(cameraStack.getItem() instanceof CameraItem,
                "Failed to open Camera Attachments. " + cameraStack + " is not a CameraItem.");

        this.player = playerInventory.player;
        this.cameraSlotIndex = cameraSlotIndex;
        this.camera = new ItemAndStack<>(cameraStack);

        this.attachments = List.of(Attachment.FILM, Attachment.FLASH, Attachment.LENS, Attachment.FILTER);

        SimpleContainer container = createAttachmentsContainer(cameraSlotIndex);

        addAttachmentSlots(container);
        addPlayerSlots(playerInventory);
    }

    protected @NotNull SimpleContainer createAttachmentsContainer(int cameraSlotIndex) {
        ItemStack[] attachmentItems = attachments.stream()
                .map(attachment -> attachment.get(camera.getItemStack()).getCopy())
                .toArray(ItemStack[]::new);

        SimpleContainer container = new SimpleContainer(attachmentItems) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        };

        container.addListener(c -> {
            for (int slotId = 0; slotId < c.getContainerSize(); slotId++) {
                Attachment<?> attachment = attachments.get(slotId);

                attachment.set(camera.getItemStack(), c.getItem(slotId));

                if (!player.level().isClientSide() && player.isCreative()) {
                    // Fixes item not updating properly when not in "Inventory" tab of creative inventory
                    player.getInventory().setItem(cameraSlotIndex, camera.getItemStack());
                }
            }
        });
        return container;
    }

    public ItemAndStack<CameraItem> getCamera() {
        return camera;
    }

    /**
     * Only called client-side.
     */
    @Override
    public void initializeContents(int stateId, List<ItemStack> items, ItemStack carried) {
        clientContentsInitialized = false;
        super.initializeContents(stateId, items, carried);
        clientContentsInitialized = true;
    }

    protected void addAttachmentSlots(Container container) {
        Map<Attachment<?>, Vector2i> slotPositions = Map.of(
                Attachment.FILM, new Vector2i(13, 42),
                Attachment.FLASH, new Vector2i(147, 15),
                Attachment.LENS, new Vector2i(147, 43),
                Attachment.FILTER, new Vector2i(147, 71));

        for (int index = 0; index < attachments.size(); index++) {
            Attachment<?> attachmentType = attachments.get(index);
            Vector2i pos = slotPositions.get(attachmentType);
            addSlot(new FilteredSlot(container, index, pos.x(), pos.y(), 1,
                    this::onItemInSlotChanged, attachmentType.itemPredicate()));
        }
    }

    protected void addPlayerSlots(Inventory playerInventory) {
        //Player Inventory
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, (column + row * 9) + 9, column * 18 + 8, 103 + row * 18){
                    @Override
                    public boolean mayPickup(@NotNull Player player) {
                        return super.mayPickup(player) && getContainerSlot() != cameraSlotIndex;
                    }

                    @Override
                    public boolean isActive() {
                        return getContainerSlot() != cameraSlotIndex;
                    }

                    @Override
                    public boolean isHighlightable() {
                        return getContainerSlot() != cameraSlotIndex;
                    }
                });
            }
        }

        //Hotbar
        for (int slot = 0; slot < 9; slot++) {
            int finalSlot = slot;
            addSlot(new Slot(playerInventory, finalSlot, slot * 18 + 8, 161) {
                @Override
                public boolean mayPickup(@NotNull Player player) {
                    return super.mayPickup(player) && getContainerSlot() != cameraSlotIndex;
                }

                @Override
                public boolean isActive() {
                    return getContainerSlot() != cameraSlotIndex;
                }

                @Override
                public boolean isHighlightable() {
                    return getContainerSlot() != cameraSlotIndex;
                }
            });
        }
    }

    protected void onItemInSlotChanged(FilteredSlot.SlotChangedArgs args) {
        int slotId = args.slot().getSlotId();
        ItemStack newStack = args.newStack();

        Attachment<?> attachment = attachments.get(slotId);
        attachment.set(camera.getItemStack(), newStack);

        if (player.level().isClientSide() && clientContentsInitialized)
            attachment.sound().playOnePerPlayer(player, newStack.isEmpty());

        if (!player.level().isClientSide() && player.isCreative()) {
            // Fixes item not updating properly when not in "Inventory" tab of creative inventory
            player.getInventory().setItem(cameraSlotIndex, camera.getItemStack());
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slotIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot clickedSlot = this.slots.get(slotIndex);
        if (clickedSlot.hasItem()) {
            ItemStack slotStack = clickedSlot.getItem();
            itemstack = slotStack.copy();
            if (slotIndex < attachments.size()) {
                if (!this.moveItemStackTo(slotStack, attachments.size(), this.slots.size(), true))
                    return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(slotStack, 0, attachments.size(), false))
                    return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty())
                clickedSlot.set(ItemStack.EMPTY);
            else
                clickedSlot.setChanged();
        }

        return itemstack;
    }

    /**
     * Fixed method to respect slot photo limit.
     */
    @Override
    protected boolean moveItemStackTo(ItemStack movedStack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean hasRemainder = false;
        int i = startIndex;
        if (reverseDirection) {
            i = endIndex - 1;
        }
        if (movedStack.isStackable()) {
            while (!movedStack.isEmpty() && !(!reverseDirection ? i >= endIndex : i < startIndex)) {
                Slot slot = this.slots.get(i);
                ItemStack slotStack = slot.getItem();
                if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(movedStack, slotStack)) {
                    int maxSize;
                    int j = slotStack.getCount() + movedStack.getCount();
                    if (j <= (maxSize = Math.min(slot.getMaxStackSize(), movedStack.getMaxStackSize()))) {
                        movedStack.setCount(0);
                        slotStack.setCount(j);
                        slot.setChanged();
                        hasRemainder = true;
                    } else if (slotStack.getCount() < maxSize) {
                        movedStack.shrink(maxSize - slotStack.getCount());
                        slotStack.setCount(maxSize);
                        slot.setChanged();
                        hasRemainder = true;
                    }
                }
                if (reverseDirection) {
                    --i;
                    continue;
                }
                ++i;
            }
        }
        if (!movedStack.isEmpty()) {
            i = reverseDirection ? endIndex - 1 : startIndex;
            while (!(!reverseDirection ? i >= endIndex : i < startIndex)) {
                Slot slot1 = this.slots.get(i);
                ItemStack movedStack1 = slot1.getItem();
                if (movedStack1.isEmpty() && slot1.mayPlace(movedStack)) {
                    if (movedStack.getCount() > slot1.getMaxStackSize()) {
                        slot1.setByPlayer(movedStack.split(slot1.getMaxStackSize()));
                    } else {
                        slot1.setByPlayer(movedStack.split(movedStack.getCount()));
                    }
                    slot1.setChanged();
                    hasRemainder = true;
                    break;
                }
                if (reverseDirection) {
                    --i;
                    continue;
                }
                ++i;
            }
        }
        return hasRemainder;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        // Without this, client inventory is syncing properly when menu is closed. (only when opened by r-click in GUI)
        player.inventoryMenu.resumeRemoteUpdates();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return ItemStack.isSameItemSameComponents(player.getInventory().getItem(cameraSlotIndex), camera.getItemStack());
    }

    public static CameraAttachmentsMenu fromBuffer(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        return new CameraAttachmentsMenu(containerId, playerInventory, buffer.readInt());
    }
}
