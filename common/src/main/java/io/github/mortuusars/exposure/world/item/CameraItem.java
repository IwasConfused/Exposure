package io.github.mortuusars.exposure.world.item;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.*;
import io.github.mortuusars.exposure.world.block.FlashBlock;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.camera.CameraID;
import io.github.mortuusars.exposure.world.camera.CameraInHand;
import io.github.mortuusars.exposure.world.camera.capture.CaptureType;
import io.github.mortuusars.exposure.world.camera.component.FocalRange;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.world.camera.capture.CaptureProperties;
import io.github.mortuusars.exposure.world.camera.capture.ProjectionInfo;
import io.github.mortuusars.exposure.world.camera.ColorChannel;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.world.camera.frame.*;
import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.data.Lenses;
import io.github.mortuusars.exposure.world.entity.PhotographerEntity;
import io.github.mortuusars.exposure.world.item.component.StoredItemStack;
import io.github.mortuusars.exposure.world.item.part.Attachment;
import io.github.mortuusars.exposure.world.item.part.CameraSetting;
import io.github.mortuusars.exposure.world.item.part.Shutter;
import io.github.mortuusars.exposure.world.inventory.CameraAttachmentsMenu;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.StartCaptureS2CP;
import io.github.mortuusars.exposure.network.packet.server.OpenCameraAttachmentsInCreativePacketC2SP;
import io.github.mortuusars.exposure.server.CameraInstance;
import io.github.mortuusars.exposure.server.CameraInstances;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import io.github.mortuusars.exposure.world.level.LevelUtil;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import io.github.mortuusars.exposure.world.sound.OnePerEntitySounds;
import io.github.mortuusars.exposure.util.*;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CameraItem extends Item {
    public static final int BASE_COOLDOWN = 2;
    public static final int FLASH_COOLDOWN = 10;
    public static final int PROJECT_COOLDOWN = 20;

    protected final Shutter shutter;
    protected final List<Attachment<?>> attachments;
    protected final List<ShutterSpeed> availableShutterSpeeds;

    public CameraItem(Shutter shutter, Properties properties) {
        super(properties);
        this.shutter = shutter;
        this.attachments = defineAttachments();
        this.availableShutterSpeeds = defineShutterSpeeds();

        shutter.onOpen(this::onShutterOpen);
        shutter.onClosed(this::onShutterClosed);
    }

    protected @NotNull List<Attachment<?>> defineAttachments() {
        return List.of(Attachment.FILM, Attachment.FLASH, Attachment.LENS, Attachment.FILTER);
    }

    protected List<ShutterSpeed> defineShutterSpeeds() {
        return List.of(
                new ShutterSpeed("1/500"),
                new ShutterSpeed("1/250"),
                new ShutterSpeed("1/125"),
                new ShutterSpeed("1/60"),
                new ShutterSpeed("1/30"),
                new ShutterSpeed("1/15"),
                new ShutterSpeed("1/8"),
                new ShutterSpeed("1/4"),
                new ShutterSpeed("1/2"),
                new ShutterSpeed("1\""),
                new ShutterSpeed("2\""),
                new ShutterSpeed("4\""),
                new ShutterSpeed("8\""),
                new ShutterSpeed("15\"")
        );
    }

    // --

    public Shutter getShutter() {
        return shutter;
    }

    public List<ShutterSpeed> getAvailableShutterSpeeds() {
        return availableShutterSpeeds;
    }

    public List<Attachment<?>> getAttachments() {
        return attachments;
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

    public float getCropFactor() {
        return Exposure.CROP_FACTOR;
    }

    public FocalRange getFocalRange(RegistryAccess registryAccess, ItemStack stack) {
        return Attachment.LENS.map(stack, lensStack -> Lenses.getFocalRangeOrDefault(registryAccess, lensStack))
                .orElse(FocalRange.getDefault());
    }

    public Holder<ColorPalette> getColorPalette(RegistryAccess registryAccess, ItemStack stack) {
        ResourceKey<ColorPalette> key = Attachment.FILM.map(stack, FilmItem::getColorPaletteId).orElse(ColorPalettes.DEFAULT);
        return ColorPalettes.get(registryAccess, key);
    }

    protected Optional<ColorChannel> getChromaChannel(ItemStack stack) {
        return Attachment.FILTER.map(stack, ColorChannel::fromFilterStack).orElse(Optional.empty());
    }

    protected Optional<ProjectionInfo> getFileLoadingData(ItemStack stack) {
        return Attachment.FILTER.map(stack, (filterItem, filterStack) ->
                        filterItem instanceof InterplanarProjectorItem projectorItem
                                ? projectorItem.getFileLoadingData(filterStack)
                                : Optional.<ProjectionInfo>empty())
                .orElse(Optional.empty());
    }

    // --

    public CameraID getOrCreateID(ItemStack stack) {
        if (!stack.has(Exposure.DataComponents.CAMERA_ID)) {
            stack.set(Exposure.DataComponents.CAMERA_ID, CameraID.create());
        }
        return stack.get(Exposure.DataComponents.CAMERA_ID);
    }

    public boolean isInSelfieMode(ItemStack stack) {
        return CameraSetting.SELFIE_MODE.getOrDefault(stack);
    }

    public void setActive(ItemStack stack, boolean active) {
        stack.set(Exposure.DataComponents.CAMERA_ACTIVE, active);
    }

    public boolean isActive(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.CAMERA_ACTIVE, false);
    }

    public @NotNull InteractionResultHolder<ItemStack> activateInHand(PhotographerEntity photographer,
                                                                      ItemStack stack, @NotNull InteractionHand hand) {
        setActive(stack, true);

        if (photographer instanceof LivingEntity) {
            photographer.setActiveExposureCamera(new CameraInHand(photographer, getOrCreateID(stack), hand));
        }

        photographer.playCameraSound(getViewfinderOpenSound(), 0.35f, 0.9f, 0.2f);

        photographer.asEntity().gameEvent(GameEvent.EQUIP); // Sends skulk vibrations

        if (photographer instanceof Player player && player.level().isClientSide) {
            Minecrft.releaseUseButton(); // Releasing use key to not take a shot immediately, if right click is still held.
        }

        return InteractionResultHolder.consume(stack);
    }

    public @NotNull InteractionResultHolder<ItemStack> deactivate(PhotographerEntity photographer, ItemStack stack) {
        setActive(stack, false);
        CameraSetting.SELFIE_MODE.set(stack, false);
        photographer.removeActiveExposureCamera();
        photographer.playCameraSound(getViewfinderCloseSound(), 0.35f, 0.9f, 0.2f);
        photographer.asEntity().gameEvent(GameEvent.EQUIP); // Sends skulk vibrations
        return InteractionResultHolder.consume(stack);
    }

    public int calculateCooldownAfterShot(ItemStack stack, PhotographerEntity photographer, CaptureProperties captureProperties) {
        if (captureProperties.projectingInfo().isPresent()) return PROJECT_COOLDOWN;
        if (captureProperties.flash()) return FLASH_COOLDOWN;
        return BASE_COOLDOWN;
    }

    // --

    public boolean isBarVisible(@NotNull ItemStack stack) {
        return Config.Client.CAMERA_SHOW_FILM_BAR_ON_ITEM.get()
                && Attachment.FILM.map(stack, FilmRollItem::isBarVisible).orElse(false);
    }

    public int getBarWidth(@NotNull ItemStack stack) {
        return Attachment.FILM.map(stack, FilmRollItem::getBarWidth).orElse(0);
    }

    public int getBarColor(@NotNull ItemStack stack) {
        return Attachment.FILM.map(stack, FilmRollItem::getBarColor).orElse(0);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag tooltipFlag) {
        if (Config.Client.CAMERA_SHOW_FILM_FRAMES_IN_TOOLTIP.get()) {
            Attachment.FILM.ifPresent(stack, (filmItem, filmStack) -> {
                int exposed = filmItem.getStoredFramesCount(filmStack);
                int max = filmItem.getMaxFrameCount(filmStack);
                components.add(Component.translatable("item.exposure.camera.tooltip.film_roll_frames", exposed, max));
            });
        }

        if (Config.Client.CAMERA_SHOW_TOOLTIP_DETAILS.get()) {
            boolean rClickAttachments = Config.Common.CAMERA_GUI_RIGHT_CLICK_ATTACHMENTS_SCREEN.get();
            boolean rClickHotswap = Config.Common.CAMERA_GUI_RIGHT_CLICK_HOTSWAP.get();

            if (rClickAttachments || rClickHotswap) {
                if (Screen.hasShiftDown()) {
                    if (rClickAttachments)
                        components.add(Component.translatable("item.exposure.camera.tooltip.details_attachments_screen"));
                    if (rClickHotswap)
                        components.add(Component.translatable("item.exposure.camera.tooltip.details_hotswap"));
                } else
                    components.add(Component.translatable("tooltip.exposure.hold_for_details"));
            }
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY) return false;

        if (getShutter().isOpen(stack)) {
            playSound(player, player, Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 0.9f, 1f, 0);
            player.displayClientMessage(Component.translatable("item.exposure.camera.camera_attachments.fail.shutter_open")
                    .withStyle(ChatFormatting.RED), true);
            return true;
        }

        if (otherStack.isEmpty() && Config.Common.CAMERA_GUI_RIGHT_CLICK_ATTACHMENTS_SCREEN.get()) {
            if (!(slot.container instanceof Inventory)) {
                return false; // Cannot open when not in player's inventory
            }

            if (player.isCreative() && player.level().isClientSide()) {
                Packets.sendToServer(new OpenCameraAttachmentsInCreativePacketC2SP(slot.getContainerSlot()));
                return true;
            }

            openCameraAttachments(player, slot.getContainerSlot());
            return true;
        }

        if (Config.Common.CAMERA_GUI_RIGHT_CLICK_HOTSWAP.get()) {
            for (Attachment<?> attachment : getAttachments()) {
                if (attachment.matches(otherStack)) {
                    StoredItemStack currentAttachment = attachment.get(stack);

                    if (otherStack.getCount() > 1 && !currentAttachment.isEmpty()) {
                        if (player.level().isClientSide())
                            playSound(null, player, Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 0.9f, 1f, 0);
                        return true; // Cannot swap when holding more than one item
                    }

                    attachment.set(stack, otherStack.split(1));

                    ItemStack returnedStack = !currentAttachment.isEmpty() ? currentAttachment.getCopy() : otherStack;
                    access.set(returnedStack);

                    attachment.sound().playOnePerPlayer(player, false);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!(entity instanceof Player player) || !(level instanceof ServerLevel serverLevel)) return;

        getShutter().tick(player, serverLevel, stack);

        boolean isHolding = isSelected || slotId == Inventory.SLOT_OFFHAND;
        if (isActive(stack) && (!isHolding || player.activeExposureCamera() == null)) {
            deactivate(player, stack);
        }

        CameraInstances.ifPresent(stack, instance -> {
            CameraInstance.ProjectionState state = instance.getProjectionState(level);
            switch (state) {
                case SUCCESSFUL, FAILED, TIMED_OUT -> {
                    handleProjectionResult(stack, player, serverLevel, state, instance.getProjectionError(level));
                    instance.stopWaitingForProjection();
                }
            }
        });

        // Tests entities in frame
         if (isActive(stack)) {
             List<LivingEntity> entitiesInFrame = getEntitiesInFrame(player, serverLevel, stack);
             entitiesInFrame.forEach(e -> {
                 e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 2));
             });
         }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        return useAs(level, player, hand);
    }

    public @NotNull InteractionResultHolder<ItemStack> useAs(@NotNull Level level, PhotographerEntity photographer, InteractionHand hand) {
        LivingEntity entity = (LivingEntity) photographer.asEntity();
        ItemStack stack = entity.getItemInHand(hand);

        if (hand == InteractionHand.MAIN_HAND
                && entity.getOffhandItem().getItem() instanceof CameraItem offhandCameraItem
                && offhandCameraItem.isActive(entity.getOffhandItem())) {
            return InteractionResultHolder.pass(stack);
        }

        if (!isActive(stack)) {
            return photographer.asEntity() instanceof Player player && player.isSecondaryUseActive()
                    ? openCameraAttachments(player, stack)
                    : activateInHand(photographer, stack, hand);
        }

        return release(level, photographer, stack);
    }

    public @NotNull InteractionResultHolder<ItemStack> release(Level level, PhotographerEntity photographer, ItemStack stack) {
        photographer.playCameraSound(getReleaseButtonSound(), 0.3f, 1f, 0.1f);

        if (level.isClientSide
                || getShutter().isOpen(stack)
                || Attachment.FILM.isEmpty(stack)
                || !CameraInstances.canReleaseShutter(CameraID.ofStack(stack))) {
            return InteractionResultHolder.consume(stack);
        }

        ItemAndStack<FilmRollItem> film = Attachment.FILM.get(stack).getItemAndStackCopy();

        if (!film.getItem().canAddFrame(film.getItemStack()))
            return InteractionResultHolder.consume(stack);

        if (level instanceof ServerLevel serverLevel) {
            if (!(photographer.getExecutingPlayer() instanceof ServerPlayer serverPlayer)) {
                Exposure.LOGGER.error("Cannot start capture: photographer '{}' does not have valid executing player.", photographer);
                return InteractionResultHolder.consume(stack);
            }

            Entity entity = photographer.asEntity();

            int lightLevel = LevelUtil.getLightLevelAt(level, entity.blockPosition());
            boolean shouldFlashFire = shouldFlashFire(stack, lightLevel);
            ShutterSpeed shutterSpeed = CameraSetting.SHUTTER_SPEED.getOrDefault(stack);

            boolean flashHasFired = shouldFlashFire && tryUseFlash(photographer, stack);

            getShutter().open(photographer, serverLevel, stack, shutterSpeed);

            CameraID cameraID = getOrCreateID(stack);

            String exposureId = ExposureIdentifier.createId(photographer.getExecutingPlayer());

            CaptureProperties captureProperties = new CaptureProperties(
                    exposureId,
                    photographer.asEntity().getUUID(),
                    Optional.of(cameraID),
                    CameraSetting.SHUTTER_SPEED.getOrDefault(stack),
                    Optional.empty(),
                    film.getItem().getType(),
                    film.getItem().getFrameSize(film.getItemStack()),
                    getCropFactor(),
                    getColorPalette(level.registryAccess(), stack),
                    flashHasFired,
                    lightLevel,
                    getFileLoadingData(stack),
                    getChromaChannel(stack),
                    new CompoundTag());

            if (shutterSpeed.shouldCauseTickingSound() || captureProperties.projectingInfo().isPresent()) {
                int duration = Math.max(shutterSpeed.getDurationTicks(), captureProperties.projectingInfo()
                        .map(l -> Config.Server.PROJECT_FROM_FILE_TIMEOUT_TICKS.get()).orElse(0));
                OnePerEntitySounds.playShutterTickingSoundForAll(photographer.asEntity(), cameraID,
                        1f, 1f, duration);
            }

            CameraInstances.createOrUpdate(cameraID, instance -> {
                instance.setPhotographer(photographer);

                int cooldown = calculateCooldownAfterShot(stack, photographer, captureProperties);
                instance.setDeferredCooldown(cooldown);

                captureProperties.projectingInfo().ifPresent(fileLoading -> {
                    instance.waitForProjection(level.getGameTime() + Config.Server.PROJECT_FROM_FILE_TIMEOUT_TICKS.get());
                });
            });

            ExposureServer.exposureRepository().expect(serverPlayer, exposureId);

            addNewFrame(serverLevel, captureProperties, photographer, stack);

            Packets.sendToClient(new StartCaptureS2CP(getCaptureType(stack), captureProperties), serverPlayer);
        }

        return InteractionResultHolder.consume(stack);
    }

    public ResourceLocation getCaptureType(ItemStack stack) {
        return CaptureType.CAMERA;
    }

    protected void onShutterOpen(PhotographerEntity photographer, ServerLevel serverLevel, ItemStack stack) {

    }

    protected void onShutterClosed(PhotographerEntity photographer, ServerLevel serverLevel, ItemStack stack) {
        if (photographer.asEntity() instanceof Player player) {
            int cooldown = CameraInstances.getOptional(stack).map(CameraInstance::getDeferredCooldown).orElse(BASE_COOLDOWN);
            player.getCooldowns().addCooldown(this, cooldown);
        }

        Attachment.FILM.ifPresent(stack, (filmItem, filmStack) -> {
            SoundEvent sound = filmItem.isFull(filmStack)
                    ? Exposure.SoundEvents.FILM_ADVANCE_LAST.get()
                    : Exposure.SoundEvents.FILM_ADVANCE.get();

            float fullness = filmItem.getFullness(filmStack);
            OnePerEntitySounds.play(null, photographer.asEntity(), sound, SoundSource.PLAYERS, 1f, 0.85f + 0.2f * fullness);
        });
    }

    public InteractionResultHolder<ItemStack> openCameraAttachments(@NotNull Player player, ItemStack stack) {
        Preconditions.checkArgument(stack.getItem() instanceof CameraItem, "%s is not a CameraItem.", stack);

        int cameraSlot = getMatchingSlotInInventory(player.getInventory(), stack);
        if (cameraSlot < 0) {
            Exposure.LOGGER.error("Cannot open camera attachments: slot index is not found for item '{}'.", stack);
            return InteractionResultHolder.fail(stack);
        }

        return openCameraAttachments(player, cameraSlot);
    }

    public InteractionResultHolder<ItemStack> openCameraAttachments(@NotNull Player player, int slotIndex) {
        Preconditions.checkArgument(slotIndex >= 0,
                "slotIndex '%s' is invalid. Should be larger than 0", slotIndex);
        ItemStack stack = player.getInventory().getItem(slotIndex);
        Preconditions.checkArgument(stack.getItem() instanceof CameraItem,
                "Item in slotIndex '%s' is not a CameraItem but '%s'.", slotIndex, stack);

        if (getShutter().isOpen(stack)) {
            player.displayClientMessage(Component.translatable("item.exposure.camera.camera_attachments.fail.shutter_open")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            MenuProvider menuProvider = new MenuProvider() {
                @Override
                public @NotNull Component getDisplayName() {
                    return stack.get(DataComponents.CUSTOM_NAME) != null
                            ? stack.getHoverName() : Component.translatable("container.exposure.camera");
                }

                @Override
                public @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
                    return new CameraAttachmentsMenu(containerId, playerInventory, slotIndex);
                }
            };

            PlatformHelper.openMenu(serverPlayer, menuProvider, buffer -> buffer.writeInt(slotIndex));
        }

        playSound(player, player, Exposure.SoundEvents.CAMERA_GENERIC_CLICK.get(), 0.9f, 0.9f, 0.2f);

        return InteractionResultHolder.success(stack);
    }

    protected boolean shouldFlashFire(ItemStack stack, int lightLevel) {
        if (Attachment.FLASH.isEmpty(stack))
            return false;

        return switch (CameraSetting.FLASH_MODE.getOrDefault(stack)) {
            case OFF -> false;
            case ON -> true;
            case AUTO -> lightLevel < 8;
        };
    }

    protected boolean tryUseFlash(PhotographerEntity photographer, ItemStack stack) {
        Entity entity = photographer.asEntity();
        Level level = entity.level();
        BlockPos playerHeadPos = entity.blockPosition().above();
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

        if (flashPos == null) {
            return false;
        }

        level.setBlock(flashPos, Exposure.Blocks.FLASH.get().defaultBlockState()
                .setValue(FlashBlock.WATERLOGGED, level.getFluidState(flashPos)
                        .isSourceOfType(Fluids.WATER)), Block.UPDATE_ALL_IMMEDIATE);
        playSound(null, entity, getFlashSound(), 1f, 1f, 0f);

        entity.gameEvent(GameEvent.PRIME_FUSE);

        // Send particles to other players:
        if (entity instanceof ServerPlayer serverPlayer) {
            serverPlayer.awardStat(Exposure.Stats.FLASHES_TRIGGERED);

            Vec3 pos = entity.position();
            pos = pos.add(0, 1, 0).add(entity.getLookAngle().multiply(0.5, 0, 0.5));
            ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(ParticleTypes.FLASH, false,
                    pos.x, pos.y, pos.z, 0, 0, 0, 0, 0);
            for (ServerPlayer pl : serverPlayer.serverLevel().players()) {
                if (!pl.equals(serverPlayer)) {
                    pl.connection.send(packet);
                    RandomSource r = serverPlayer.serverLevel().getRandom();
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


    public void addNewFrame(ServerLevel level, CaptureProperties captureProperties, PhotographerEntity photographer, ItemStack stack) {
        Frame frame = createFrame(level, captureProperties, photographer, stack);
        addFrameToFilm(stack, frame);
        onFrameAdded(level, photographer, stack, frame);
    }

    public Frame createFrame(ServerLevel level, CaptureProperties captureProperties, PhotographerEntity photographer, ItemStack stack) {
        Entity cameraHolder = photographer.asEntity();
        boolean projecting = captureProperties.projectingInfo().isPresent();

        CompoundTag tag = new CompoundTag();
        List<EntityInFrame> entitiesInFrame;

        tag.putFloat(FrameTag.SHUTTER_SPEED_MS, CameraSetting.SHUTTER_SPEED.getOrDefault(stack).getDurationMilliseconds());

        tag.put(FrameTag.TIMESTAMP, LongTag.valueOf(UnixTimestamp.Seconds.now()));

        if (projecting) {
            tag.putBoolean(FrameTag.PROJECTED, true);
            entitiesInFrame = Collections.emptyList();
        } else {
            if (isInSelfieMode(stack)) {
                tag.putBoolean(FrameTag.SELFIE, true);
            }

            if (captureProperties.flash()) {
                tag.putBoolean(FrameTag.FLASH, true);
            }

            double zoom = CameraSetting.ZOOM.getOrDefault(stack);
            int focalLength = (int) getFocalRange(level.registryAccess(), stack).focalLengthFromZoom(zoom);
            tag.putInt(FrameTag.FOCAL_LENGTH, focalLength);

            tag.putInt(FrameTag.LIGHT_LEVEL, captureProperties.lightLevel());

            captureProperties.chromaticChannel().ifPresent(channel ->
                    tag.putString(FrameTag.COLOR_CHANNEL, channel.getSerializedName()));

            // Position
            ListTag pos = new ListTag();
            pos.addAll(List.of(
                    DoubleTag.valueOf(cameraHolder.position().x()),
                    DoubleTag.valueOf(cameraHolder.position().y()),
                    DoubleTag.valueOf(cameraHolder.position().z())));
            tag.put(FrameTag.POSITION, pos);
            tag.put(FrameTag.PITCH, FloatTag.valueOf(cameraHolder.getXRot()));
            tag.put(FrameTag.YAW, FloatTag.valueOf(cameraHolder.getYRot()));

            // Environment
            tag.putInt(FrameTag.DAY_TIME, (int) level.getDayTime());
            tag.putString(FrameTag.DIMENSION, level.dimension().location().toString());
            BlockPos blockPos = cameraHolder.blockPosition();
            level.getBiome(blockPos).unwrapKey().map(ResourceKey::location)
                    .ifPresent(biome -> tag.putString(FrameTag.BIOME, biome.toString()));
            int surfaceHeight = level.getHeight(Heightmap.Types.WORLD_SURFACE, cameraHolder.getBlockX(), cameraHolder.getBlockZ());
            level.updateSkyBrightness();
            int skyLight = level.getBrightness(LightLayer.SKY, blockPos);
            if (cameraHolder.isUnderWater())
                tag.putBoolean(FrameTag.UNDERWATER, true);
            if (cameraHolder.getBlockY() < Math.min(level.getSeaLevel(), surfaceHeight) && skyLight == 0)
                tag.putBoolean(FrameTag.IN_CAVE, true);
            else if (!cameraHolder.isUnderWater()) {
                Biome.Precipitation precipitation = level.getBiome(blockPos).value().getPrecipitationAt(blockPos);
                if (level.isThundering() && precipitation != Biome.Precipitation.NONE)
                    tag.putString(FrameTag.WEATHER, precipitation == Biome.Precipitation.SNOW ? "Snowstorm" : "Thunder");
                else if (level.isRaining() && precipitation != Biome.Precipitation.NONE)
                    tag.putString(FrameTag.WEATHER, precipitation == Biome.Precipitation.SNOW ? "Snow" : "Rain");
                else
                    tag.putString(FrameTag.WEATHER, "Clear");
            }

            addStructuresInfo(level, blockPos, tag);

            List<LivingEntity> capturedEntities = getEntitiesInFrame(photographer, level, stack);
            capturedEntities.forEach(entity -> entityCaptured(photographer, stack, entity));

            entitiesInFrame = capturedEntities.stream()
                    .limit(Exposure.MAX_ENTITIES_IN_FRAME)
                    .map(entity -> EntityInFrame.of(cameraHolder, entity, customDataTag -> {
                        //TODO: modifyEntityInFrameData event
                    }))
                    .toList();
        }

        ExposureIdentifier identifier = ExposureIdentifier.id(captureProperties.exposureID());
        return new Frame(identifier, captureProperties.filmType(), new Photographer(photographer), entitiesInFrame, FrameTag.of(tag));
    }

    public void addFrameToFilm(ItemStack stack, Frame frame) {
        Attachment.FILM.ifPresentOrElse(stack, (filmItem, filmStack) -> {
            ItemStack updatedFilmStack = filmStack.copy();
            filmItem.addFrame(updatedFilmStack, frame);
            Attachment.FILM.set(stack, updatedFilmStack);
        }, () -> Exposure.LOGGER.error("Cannot add frame: no film attachment is present."));
    }

    public void onFrameAdded(ServerLevel level, PhotographerEntity photographer, ItemStack stack, Frame frame) {
        ExposureServer.frameHistory().add(photographer.asEntity(), frame);

        if (photographer.getOwnerPlayer() instanceof ServerPlayer serverPlayer) {
            serverPlayer.awardStat(Exposure.Stats.FILM_FRAMES_EXPOSED);
            //TODO: advancement trigger
            //Exposure.CriteriaTriggers.FILM_FRAME_EXPOSED.trigger(player, new ItemAndStack<>(cameraStack), frame, entities);
        }
    }

    public List<LivingEntity> getEntitiesInFrame(PhotographerEntity photographer, ServerLevel level, ItemStack stack) {
        float zoom = CameraSetting.ZOOM.getOrDefault(stack);
        double fov = getFocalRange(level.registryAccess(), stack).fovFromZoom(zoom) * getCropFactor();

        return EntitiesInFrame.get(photographer.asEntity(), fov, isInSelfieMode(stack));
    }

    protected void entityCaptured(PhotographerEntity photographer, ItemStack stack, LivingEntity entity) {
        if (photographer.asEntity() instanceof ServerPlayer player && entity instanceof EnderMan enderMan) {
            boolean lookingAtAngryEnderMan = player.equals(enderMan.getTarget()) && enderMan.isLookingAtMe(player);

            if (lookingAtAngryEnderMan) {
                // I wanted to implement this in a predicate,
                // but it's tricky because EntitySubPredicates do not get the player in their 'match' method.
                // So it's just easier to hardcode it like this.
                Exposure.CriteriaTriggers.PHOTOGRAPH_ENDERMAN_EYES.get().trigger(player);
            }
        }
    }

    protected void addStructuresInfo(ServerLevel level, BlockPos pos, CompoundTag frame) {
        Map<Structure, LongSet> allStructuresAt = level.structureManager().getAllStructuresAt(pos);

        List<Structure> inside = new ArrayList<>();

        for (Structure structure : allStructuresAt.keySet()) {
            StructureStart structureAt = level.structureManager().getStructureAt(pos, structure);
            if (structureAt.isValid()) {
                inside.add(structure);
            }
        }

        Registry<Structure> structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        ListTag structuresTag = new ListTag();

        for (Structure structure : inside) {
            ResourceLocation key = structures.getKey(structure);
            if (key != null)
                structuresTag.add(StringTag.valueOf(key.toString()));
        }

        if (!structuresTag.isEmpty()) {
            frame.put("Structures", structuresTag);
        }
    }

    public void handleProjectionResult(ItemStack stack, PhotographerEntity photographer, ServerLevel level,
                                       CameraInstance.ProjectionState projectionState, Optional<TranslatableError> error) {
        StoredItemStack filter = Attachment.FILTER.get(stack);
        if (filter.isEmpty()) return;
        if (!(filter.getItem() instanceof InterplanarProjectorItem interplanarProjector)) return;
        if (!interplanarProjector.isConsumable(filter.getForReading())) return;

        if (projectionState == CameraInstance.ProjectionState.FAILED) {
            ItemStack filterStack = filter.getCopy().transmuteCopy(Exposure.Items.BROKEN_INTERPLANAR_PROJECTOR.get());
            error.ifPresent(err -> filterStack.set(Exposure.DataComponents.INTERPLANAR_PROJECTOR_ERROR_CODE, err.code()));
            Attachment.FILTER.set(stack, filterStack);
            playSound(null, photographer.asEntity(), Exposure.SoundEvents.BSOD.get(), 1f, 1f, 0);
            return;
        }

        ItemStack filterStack = filter.getCopy();
        filterStack.shrink(1);
        Attachment.FILTER.set(stack, filterStack);

        if (projectionState == CameraInstance.ProjectionState.TIMED_OUT) {
            photographer.playCameraSound(Exposure.SoundEvents.INTERPLANAR_PROJECT.get(), 0.4f, 0.6f, 0f);
            deactivate(photographer, stack);
        } else if (projectionState == CameraInstance.ProjectionState.SUCCESSFUL) {
            photographer.playCameraSound(Exposure.SoundEvents.INTERPLANAR_PROJECT.get(), 0.8f, 1.1f, 0f);
            Entity entity = photographer.asEntity();
            for (int i = 0; i < 16; i++) {
                level.sendParticles(ParticleTypes.PORTAL, entity.getX(), entity.getY() + 1.2, entity.getZ(), 2,
                        entity.getRandom().nextGaussian() * 0.3, entity.getRandom().nextGaussian() * 0.3, entity.getRandom().nextGaussian() * 0.3, 0.01);
            }
        }
    }

    // --

    public void playSound(@Nullable Player excludedPlayer, @NotNull Entity origin, SoundEvent sound,
                          float volume, float pitch, float pitchVariety) {
        if (pitchVariety > 0f)
            pitch = pitch - (pitchVariety / 2f) + (origin.getRandom().nextFloat() * pitchVariety);
        origin.level().playSound(excludedPlayer, origin, sound, SoundSource.PLAYERS, volume, pitch);
    }

    protected int getMatchingSlotInInventory(Inventory inventory, ItemStack stack) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).equals(stack)) {
                return i;
            }
        }
        return -1;
    }
}
