package io.github.mortuusars.exposure.item;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.block.FlashBlock;
import io.github.mortuusars.exposure.client.Client;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.core.camera.*;
import io.github.mortuusars.exposure.core.camera.component.FlashMode;
import io.github.mortuusars.exposure.core.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.item.part.Attachment;
import io.github.mortuusars.exposure.item.part.Setting;
import io.github.mortuusars.exposure.item.part.Shutter;
import io.github.mortuusars.exposure.sound.OnePerEntitySounds;
import io.github.mortuusars.exposure.util.ItemAndStack;
import io.github.mortuusars.exposure.util.LevelUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NewCameraItem extends Item {
    protected final Shutter shutter;

    public NewCameraItem(Shutter shutter, Properties properties) {
        super(properties);
        this.shutter = shutter;

        //TODO: shutter on open on close
//        shutter.onClosed((entity, stack) -> {});
    }

    // --

    public Shutter getShutter() {
        return shutter;
    }

    public SoundEvent getViewfinderOpenSound() {
        return Exposure.SoundEvents.VIEWFINDER_OPEN.get();
    }

    public SoundEvent getViewfinderCloseSound() {
        return Exposure.SoundEvents.VIEWFINDER_CLOSE.get();
    }

    public SoundEvent getReleaseButtonSound() {
        return Exposure.SoundEvents.CAMERA_RELEASE_BUTTON_CLICK.get();
    }

    public SoundEvent getFlashSound() {
        return Exposure.SoundEvents.FLASH.get();
    }

    // --

    public void setActive(ItemStack stack, boolean active) {
        stack.set(Exposure.DataComponents.CAMERA_VIEWFINDER_OPEN, active);
    }

    public boolean isActive(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.CAMERA_VIEWFINDER_OPEN, false);
    }

    public @NotNull InteractionResultHolder<ItemStack> activate(LivingEntity entity, ItemStack stack, @NotNull InteractionHand hand) {
        setActive(stack, true);
        if (entity instanceof ActiveCameraHolder cameraHolder) {
            cameraHolder.setActiveCamera(new NewCameraInHand(entity, hand));
        }
        //player.getCooldowns().addCooldown(this, 4);

        entity.gameEvent(GameEvent.EQUIP); // Sends skulk vibrations

        @Nullable Player excludedPlayer = entity instanceof Player player ? player : null;
        playSound(excludedPlayer, entity, getViewfinderOpenSound(), 0.35f, 0.9f, 0.2f);

        if (entity.level().isClientSide) {
            Client.releaseUseButton(); // Releasing use key to not take a shot immediately, if right click is still held.
        }

        return InteractionResultHolder.consume(stack);
    }

    public @NotNull InteractionResultHolder<ItemStack> deactivate(LivingEntity entity, ItemStack stack) {
        setActive(stack, false);
        if (entity instanceof ActiveCameraHolder cameraHolder) {
            cameraHolder.removeActiveCamera();
        }
        @Nullable Player excludedPlayer = entity instanceof Player player ? player : null;
        playSound(excludedPlayer, entity, getViewfinderCloseSound(), 0.35f, 0.9f, 0.2f);
        entity.gameEvent(GameEvent.EQUIP); // Sends skulk vibrations
        return InteractionResultHolder.consume(stack);
    }

    // --

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!(entity instanceof Player player)) return;

        if (!level.isClientSide()
                && isActive(stack)
                && !player.activeCameraMatches(stack)) {
            deactivate(player, stack);
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (hand == InteractionHand.MAIN_HAND
                && player.getOffhandItem().getItem() instanceof CameraItem offhandCameraItem
                && offhandCameraItem.isActive(player.getOffhandItem())) {
            return InteractionResultHolder.pass(stack);
        }

        if (isActive(stack)) {
            return release(level, player, hand, stack);
        }

        if (player.isSecondaryUseActive()) {
            return openCameraAttachments(player, stack);
        }

        return activate(player, stack, hand);
    }

    public @NotNull InteractionResultHolder<ItemStack> release(Level level, Player player, InteractionHand hand, ItemStack stack) {
        playSound(player, player, getReleaseButtonSound(), 0.3f, 1f, 0.1f);

        if (getShutter().isOpen(stack) || Attachment.FILM.isEmpty(stack)) {
            return InteractionResultHolder.fail(stack);
        }

        ItemAndStack<FilmRollItem> film = Attachment.FILM.get(stack).getItemAndStackCopy();

        if (!film.getItem().canAddFrame(film.getItemStack()))
            return InteractionResultHolder.fail(stack);

        if (!level.isClientSide()) {
            int lightLevel = LevelUtil.getLightLevelAt(player.level(), player.blockPosition());
            boolean shouldFlashFire = shouldFlashFire(player, stack, lightLevel);
            ShutterSpeed shutterSpeed = Setting.SHUTTER_SPEED.getOrDefault(stack, ShutterSpeed.DEFAULT);

            boolean flashHasFired = shouldFlashFire && tryUseFlash(player, stack);

            getShutter().open(player, stack, shutterSpeed);

            if (shutterSpeed.shouldCauseTickingSound()) {
                OnePerEntitySounds.playShutterTickingSoundForAllPlayers(CameraAccessors.ofHand(hand), player,
                        1f, 1f, shutterSpeed.getDurationTicks());
            }

            ExposureIdentifier exposureIdentifier = ExposureIdentifier.createId(player);

            //TODO: create persistent serverside camera object and reference it from id stored in item
//            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
//                tag.putString(ID_OF_LAST_SHOT, exposureIdentifier.getId());
//                tag.putBoolean(FLASH_HAS_FIRED_ON_LAST_SHOT, flashHasFired);
//                tag.putInt(LIGHT_LEVEL_ON_LAST_SHOT, lightLevel);
//            });

            //TODO: use Photographer instead of creator string
            ExposureServer.awaitExposure(exposureIdentifier, film.getItem().getType(), player.getScoreboardName());

            //TODO: send packet with all info to start exposure
            // then send packet back to server with client info (entities, etc) and capture image

//            CameraAccessor<?> cameraAccessor = CameraAccessors.ofHand(hand);
//            Packets.sendToClient(new StartExposureS2CP(exposureIdentifier, cameraAccessor, flashHasFired, lightLevel), serverPlayer);
        }

        return InteractionResultHolder.consume(stack);
    }

    protected @NotNull InteractionResultHolder<ItemStack> openCameraAttachments(@NotNull Player player, ItemStack stack) {
        if (getShutter().isOpen(stack)) {
            player.displayClientMessage(Component.translatable("item.exposure.camera.camera_attachments.fail.shutter_open")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        int cameraSlot = getMatchingSlotInInventory(player.getInventory(), stack);
        if (cameraSlot < 0)
            return InteractionResultHolder.fail(stack);

        return InteractionResultHolder.success(stack);
    }

    protected boolean shouldFlashFire(Player player, ItemStack stack, int lightLevel) {
        if (Attachment.FLASH.isEmpty(stack))
            return false;

        return switch (Setting.FLASH_MODE.getOrDefault(stack, FlashMode.OFF)) {
            case OFF -> false;
            case ON -> true;
            case AUTO -> lightLevel < 8;
        };
    }

    protected boolean tryUseFlash(Player player, ItemStack stack) {
        Level level = player.level();
        BlockPos playerHeadPos = player.blockPosition().above();
        @Nullable BlockPos flashPos = null;

        if (level.getBlockState(playerHeadPos).isAir() || level.getFluidState(playerHeadPos).isSourceOfType(Fluids.WATER))
            flashPos = playerHeadPos;
        else {
            for (Direction direction : Direction.values()) {
                BlockPos pos = playerHeadPos.relative(direction);
                if (level.getBlockState(pos).isAir() || level.getFluidState(pos).isSourceOfType(Fluids.WATER)) {
                    flashPos = pos;
                }
            }
        }

        if (flashPos == null)
            return false;

        level.setBlock(flashPos, Exposure.Blocks.FLASH.get().defaultBlockState()
                .setValue(FlashBlock.WATERLOGGED, level.getFluidState(flashPos)
                        .isSourceOfType(Fluids.WATER)), Block.UPDATE_ALL_IMMEDIATE);
        playSound(null, player, getFlashSound(), 1f, 1f, 0f);

        player.gameEvent(GameEvent.PRIME_FUSE);
        player.awardStat(Exposure.Stats.FLASHES_TRIGGERED);

        // Send particles to other players:
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            Vec3 pos = player.position();
            pos = pos.add(0, 1, 0).add(player.getLookAngle().multiply(0.5, 0, 0.5));
            ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(ParticleTypes.FLASH, false,
                    pos.x, pos.y, pos.z, 0, 0, 0, 0, 0);
            for (ServerPlayer pl : serverLevel.players()) {
                if (!pl.equals(serverPlayer)) {
                    pl.connection.send(packet);
                    RandomSource r = serverLevel.getRandom();
                    for (int i = 0; i < 4; i++) {
                        pl.connection.send(new ClientboundLevelParticlesPacket(ParticleTypes.END_ROD, false,
                                pos.x + r.nextFloat() * 0.5f - 0.25f, pos.y + r.nextFloat() * 0.5f + 0.2f, pos.z + r.nextFloat() * 0.5f - 0.25f,
                                0, 0, 0, 0, 0));
                    }
                }
            }
        }
        return true;
    }

    // --

    protected int getMatchingSlotInInventory(Inventory inventory, ItemStack stack) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).equals(stack)) {
                return i;
            }
        }
        return -1;
    }

    public void playSound(@Nullable Player excludedPlayer, @NotNull Entity origin, SoundEvent sound,
                          float volume, float pitch, float pitchVariety) {
        if (pitchVariety > 0f)
            pitch = pitch - (pitchVariety / 2f) + (origin.getRandom().nextFloat() * pitchVariety);
        origin.level().playSound(excludedPlayer, origin, sound, SoundSource.PLAYERS, volume, pitch);
    }
}
