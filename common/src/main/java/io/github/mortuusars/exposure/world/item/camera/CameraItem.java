package io.github.mortuusars.exposure.world.item.camera;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.*;
import io.github.mortuusars.exposure.world.block.FlashBlock;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.camera.*;
import io.github.mortuusars.exposure.world.camera.capture.CaptureType;
import io.github.mortuusars.exposure.world.camera.component.FocalRange;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.world.camera.capture.CaptureProperties;
import io.github.mortuusars.exposure.world.camera.capture.ProjectionInfo;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.world.camera.frame.*;
import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.data.Lenses;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.item.FilmItem;
import io.github.mortuusars.exposure.world.item.FilmRollItem;
import io.github.mortuusars.exposure.world.item.InterplanarProjectorItem;
import io.github.mortuusars.exposure.world.item.component.StoredItemStack;
import io.github.mortuusars.exposure.world.inventory.CameraAttachmentsMenu;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.CaptureStartS2CP;
import io.github.mortuusars.exposure.network.packet.server.OpenCameraAttachmentsInCreativePacketC2SP;
import io.github.mortuusars.exposure.server.CameraInstance;
import io.github.mortuusars.exposure.server.CameraInstances;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import io.github.mortuusars.exposure.world.level.LevelUtil;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import io.github.mortuusars.exposure.util.*;
import io.github.mortuusars.exposure.world.sound.Sound;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public ResourceLocation getCaptureType(ItemStack stack) {
        return CaptureType.CAMERA;
    }

    public float getCropFactor() {
        return Exposure.CROP_FACTOR;
    }

    public FocalRange getFocalRange(RegistryAccess registryAccess, ItemStack stack) {
        return Attachment.LENS.map(stack, lensStack -> Lenses.getFocalRangeOrDefault(registryAccess, lensStack))
                .orElse(FocalRange.getDefault());
    }

    public float getFov(Level level, ItemStack stack) {
        double zoom = CameraSettings.ZOOM.getOrDefault(stack);
        FocalRange focalRange = getFocalRange(level.registryAccess(), stack);
        return (float) focalRange.fovFromZoom(zoom);
    }

    public Holder<ColorPalette> getColorPalette(RegistryAccess registryAccess, ItemStack stack) {
        ResourceKey<ColorPalette> key = Attachment.FILM.map(stack, FilmItem::getColorPaletteId).orElse(ColorPalettes.DEFAULT);
        return ColorPalettes.get(registryAccess, key);
    }

    protected Optional<ColorChannel> getChromaticChannel(ItemStack stack) {
        return Attachment.FILTER.map(stack, ColorChannel::fromFilterStack).orElse(Optional.empty());
    }

    protected Optional<ProjectionInfo> getProjectionInfo(ItemStack stack) {
        return Attachment.FILTER.map(stack, (filterItem, filterStack) ->
                        filterItem instanceof InterplanarProjectorItem projectorItem
                                ? projectorItem.getProjectingInfo(filterStack)
                                : Optional.<ProjectionInfo>empty())
                .orElse(Optional.empty());
    }

    // --

    public CameraId getOrCreateID(ItemStack stack) {
        if (!stack.has(Exposure.DataComponents.CAMERA_ID)) {
            stack.set(Exposure.DataComponents.CAMERA_ID, CameraId.create());
        }
        return stack.get(Exposure.DataComponents.CAMERA_ID);
    }

    public boolean isInSelfieMode(ItemStack stack) {
        return CameraSettings.SELFIE_MODE.getOrDefault(stack);
    }

    public boolean isActive(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.CAMERA_ACTIVE, false);
    }

    public void setActive(ItemStack stack, boolean active) {
        stack.set(Exposure.DataComponents.CAMERA_ACTIVE, active);
    }

    public boolean isDisassembled(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.CAMERA_DISASSEMBLED, false);
    }

    public void setDisassembled(ItemStack stack, boolean disassembled) {
        stack.set(Exposure.DataComponents.CAMERA_DISASSEMBLED, disassembled);
    }

    public long getLastActionTime(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.CAMERA_LAST_ACTION_TIME, -1L);
    }

    public void setLastActionTime(ItemStack stack, long lastActionTime) {
        stack.set(Exposure.DataComponents.CAMERA_LAST_ACTION_TIME, lastActionTime);
    }

    public void actionPerformed(ItemStack stack, CameraHolder holder) {
        setLastActionTime(stack, holder.asEntity().level().getGameTime());
        holder.asEntity().gameEvent(GameEvent.ITEM_INTERACT_FINISH);
    }

    public @NotNull InteractionResultHolder<ItemStack> activateInHand(Player player, ItemStack stack, @NotNull InteractionHand hand) {
        player.setActiveExposureCamera(new CameraInHand(player, getOrCreateID(stack), hand));
        if (player.level().isClientSide) {
            Minecrft.releaseUseButton(); // Releasing use key to not take a shot immediately, if right click is still held.
        }
        return activate(player, stack);
    }

    public @NotNull InteractionResultHolder<ItemStack> activate(Entity entity, ItemStack stack) {
        setActive(stack, true);
        setDisassembled(stack, false);
        Sound.play(entity, getViewfinderOpenSound(), entity.getSoundSource(), 0.35f, 0.9f, 0.2f);
        entity.gameEvent(GameEvent.EQUIP);
        return InteractionResultHolder.consume(stack);
    }

    public @NotNull InteractionResultHolder<ItemStack> deactivate(Entity entity, ItemStack stack) {
        setActive(stack, false);
        CameraSettings.SELFIE_MODE.set(stack, false);
        Sound.play(entity, getViewfinderCloseSound(), entity.getSoundSource(), 0.35f, 0.9f, 0.2f);
        entity.gameEvent(GameEvent.EQUIP);
        return InteractionResultHolder.consume(stack);
    }

    public int calculateCooldownAfterShot(ItemStack stack, CaptureProperties captureProperties) {
        if (captureProperties.projection().isPresent()) return PROJECT_COOLDOWN;
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
            boolean rClickAttachments = Config.Server.CAMERA_GUI_RIGHT_CLICK_OPEN_ATTACHMENTS.get();
            boolean rClickHotswap = Config.Server.CAMERA_GUI_RIGHT_CLICK_HOTSWAP.get();

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
            player.playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 0.9f, 1f);
            player.displayClientMessage(Component.translatable("item.exposure.camera.camera_attachments.fail.shutter_open")
                    .withStyle(ChatFormatting.RED), true);
            return true;
        }

        if (otherStack.isEmpty() && Config.Server.CAMERA_GUI_RIGHT_CLICK_OPEN_ATTACHMENTS.get()) {
            if (!(slot.container instanceof Inventory)) {
                return false; // Cannot open when not in player's inventory
            }

            if (player.isCreative() && player.level().isClientSide()) {
                Packets.sendToServer(new OpenCameraAttachmentsInCreativePacketC2SP(slot.getContainerSlot()));
                return true;
            }

            openCameraAttachments(player, slot.getContainerSlot(), true);
            return true;
        }

        if (Config.Server.CAMERA_GUI_RIGHT_CLICK_HOTSWAP.get()) {
            for (Attachment<?> attachment : getAttachments()) {
                if (attachment.matches(otherStack)) {
                    StoredItemStack currentAttachment = attachment.get(stack);

                    if (otherStack.getCount() > 1 && !currentAttachment.isEmpty()) {
                        player.playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 0.9f, 1f);
                        return true; // Cannot swap when holding more than one item
                    }

                    attachment.set(stack, otherStack.split(1));

                    ItemStack returnedStack = !currentAttachment.isEmpty() ? currentAttachment.getCopy() : otherStack;
                    access.set(returnedStack);

                    attachment.playInsertSoundSided(player);
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

        boolean matchesActive = player.getActiveExposureCameraOptional()
                .map(camera -> camera.idMatches(getOrCreateID(stack)))
                .orElse(false);
        if (isActive(stack) && !matchesActive) {
            setActive(stack, false);
        }

        CameraInstances.ifPresent(stack, instance -> {
            CameraInstance.ProjectionState state = instance.getProjectionState(level);
            switch (state) {
                case SUCCESSFUL, FAILED, TIMED_OUT -> {
                    handleProjectionResult(serverLevel, player, stack, state, instance.getProjectionError(level));
                    instance.stopWaitingForProjection();
                }
            }
        });
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (hand == InteractionHand.MAIN_HAND
                && player.getOffhandItem().getItem() instanceof CameraItem offhandCameraItem
                && offhandCameraItem.isActive(player.getOffhandItem())) {
            return InteractionResultHolder.pass(stack);
        }

        if (!isActive(stack)) {
            return player.isSecondaryUseActive()
                    ? openCameraAttachments(player, stack, false)
                    : activateInHand(player, stack, hand);
        }

        return release(player, stack);
    }

    public @NotNull InteractionResultHolder<ItemStack> release(CameraHolder holder, ItemStack stack) {
        Entity entity = holder.asEntity();
        Level level = entity.level();

        Sound.playSided(entity, getReleaseButtonSound(), entity.getSoundSource(), 0.3f, 1f, 0.1f);

        if (level.isClientSide
                || getShutter().isOpen(stack)
                || Attachment.FILM.isEmpty(stack)
                || !CameraInstances.canReleaseShutter(CameraId.ofStack(stack))) {
            return InteractionResultHolder.consume(stack);
        }

        ItemAndStack<FilmRollItem> film = Attachment.FILM.get(stack).getItemAndStackCopy();

        if (!film.getItem().canAddFrame(film.getItemStack()))
            return InteractionResultHolder.consume(stack);

        if (level instanceof ServerLevel serverLevel) {
            if (!(holder.getPlayerExecutingExposure() instanceof ServerPlayer serverPlayer)) {
                Exposure.LOGGER.error("Cannot start capture: photographer '{}' does not have valid executing player.", holder);
                return InteractionResultHolder.consume(stack);
            }

            int lightLevel = LevelUtil.getLightLevelAt(level, entity.blockPosition());
            boolean shouldFlashFire = shouldFlashFire(stack, lightLevel);
            ShutterSpeed shutterSpeed = CameraSettings.SHUTTER_SPEED.getOrDefault(stack);

            boolean flashHasFired = shouldFlashFire && tryUseFlash(entity, serverLevel, stack);

            getShutter().open(holder, serverLevel, stack, shutterSpeed);

            CameraId cameraId = getOrCreateID(stack);

            String exposureId = ExposureIdentifier.createId(serverPlayer);

            CaptureProperties captureProperties = new CaptureProperties.Builder(exposureId)
                    .setCameraHolder(holder)
                    .setCameraID(cameraId)
                    .setShutterSpeed(CameraSettings.SHUTTER_SPEED.getOrDefault(stack))
                    .setFilmType(film.getItem().getType())
                    .setFrameSize(film.getItem().getFrameSize(film.getItemStack()))
                    .setCropFactor(getCropFactor())
                    .setColorPalette(getColorPalette(level.registryAccess(), stack))
                    .setFlash(flashHasFired)
                    .setProjectingInfo(getProjectionInfo(stack))
                    .setChromaticChannel(getChromaticChannel(stack))
                    .extraData(tag -> tag.putInt("light_level", lightLevel))
                    .build();

            if (shutterSpeed.shouldCauseTickingSound() || captureProperties.projection().isPresent()) {
                int duration = Math.max(shutterSpeed.getDurationTicks(), captureProperties.projection()
                        .map(l -> Config.Server.PROJECT_TIMEOUT_TICKS.get()).orElse(0));
                Sound.playShutterTicking(entity, cameraId, duration);
            }

            CameraInstances.createOrUpdate(cameraId, instance -> {
                int cooldown = calculateCooldownAfterShot(stack, captureProperties);
                instance.setDeferredCooldown(cooldown);

                captureProperties.projection().ifPresent(fileLoading -> {
                    instance.waitForProjection(level.getGameTime() + Config.Server.PROJECT_TIMEOUT_TICKS.get());
                });
            });

            ExposureServer.exposureRepository().expect(serverPlayer, exposureId);

            addNewFrame(serverLevel, captureProperties, holder, stack);

            Packets.sendToClient(new CaptureStartS2CP(getCaptureType(stack), captureProperties), serverPlayer);
        }

        return InteractionResultHolder.consume(stack);
    }

    protected void onShutterOpen(CameraHolder holder, ServerLevel serverLevel, ItemStack stack) {

    }

    protected void onShutterClosed(CameraHolder holder, ServerLevel serverLevel, ItemStack stack) {
        if (holder instanceof Player player) {
            int cooldown = CameraInstances.getOptional(stack).map(CameraInstance::getDeferredCooldown).orElse(BASE_COOLDOWN);
            player.getCooldowns().addCooldown(this, cooldown);
        }

        Attachment.FILM.ifPresent(stack, (filmItem, filmStack) -> {
            SoundEvent sound = filmItem.isFull(filmStack)
                    ? Exposure.SoundEvents.FILM_ADVANCE_LAST.get()
                    : Exposure.SoundEvents.FILM_ADVANCE.get();

            float fullness = filmItem.getFullness(filmStack);
            String id = holder.asEntity().getId() + getOrCreateID(stack).uuid().toString();
            Sound.playUnique(id, holder.asEntity(), sound, SoundSource.PLAYERS, 1f, 0.85f + 0.2f * fullness);
        });
    }

    public InteractionResultHolder<ItemStack> openCameraAttachments(@NotNull Player player, ItemStack stack, boolean openedFromGUI) {
        Preconditions.checkArgument(stack.getItem() instanceof CameraItem, "%s is not a CameraItem.", stack);

        int cameraSlot = getMatchingSlotInInventory(player.getInventory(), stack);
        if (cameraSlot < 0) {
            Exposure.LOGGER.error("Cannot open camera attachments: slot index is not found for item '{}'.", stack);
            return InteractionResultHolder.fail(stack);
        }

        return openCameraAttachments(player, cameraSlot, openedFromGUI);
    }

    public InteractionResultHolder<ItemStack> openCameraAttachments(@NotNull Player player, int slotIndex, boolean openedFromGUI) {
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
                    return new CameraAttachmentsMenu(containerId, playerInventory, slotIndex, openedFromGUI);
                }
            };

            PlatformHelper.openMenu(serverPlayer, menuProvider, buffer -> {
                buffer.writeInt(slotIndex);
                buffer.writeBoolean(openedFromGUI);
            });
        }

        stack.set(Exposure.DataComponents.CAMERA_DISASSEMBLED, true);

        Sound.play(player, Exposure.SoundEvents.CAMERA_GENERIC_CLICK.get(), SoundSource.PLAYERS, 0.9f, 0.9f, 0.2f);

        return InteractionResultHolder.success(stack);
    }

    protected boolean shouldFlashFire(ItemStack stack, int lightLevel) {
        if (Attachment.FLASH.isEmpty(stack))
            return false;

        return switch (CameraSettings.FLASH_MODE.getOrDefault(stack)) {
            case OFF -> false;
            case ON -> true;
            case AUTO -> lightLevel < 8;
        };
    }

    protected boolean tryUseFlash(Entity entity, ServerLevel serverLevel, ItemStack stack) {
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

        Sound.play(entity, getFlashSound(), entity.getSoundSource());

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

    public void addNewFrame(ServerLevel level, CaptureProperties captureProperties, CameraHolder holder, ItemStack stack) {
        boolean projecting = captureProperties.projection().isPresent();

        float fov = getFov(level, stack);

        List<BlockPos> positionsInFrame = projecting ? Collections.emptyList() : getPositionsInFrame(holder.asEntity(), fov);
        List<LivingEntity> entitiesInFrame = projecting ? Collections.emptyList() : getEntitiesInFrame(holder, level, stack);

        Frame frame = createFrame(holder, level, stack, captureProperties, positionsInFrame, entitiesInFrame);
        addFrameToFilm(stack, frame);
        onFrameAdded(holder, level, stack, frame, positionsInFrame, entitiesInFrame);
    }

    public Frame createFrame(CameraHolder holder, ServerLevel level, ItemStack stack, CaptureProperties captureProperties,
                             List<BlockPos> positionsInFrame, List<LivingEntity> entitiesInFrame) {
        return Frame.create()
                .setIdentifier(ExposureIdentifier.id(captureProperties.exposureId()))
                .setType(captureProperties.filmType())
                .setPhotographer(new Photographer(holder))
                .setEntitiesInFrame(entitiesInFrame.stream()
                        .limit(Exposure.MAX_ENTITIES_IN_FRAME)
                        .map(entity -> EntityInFrame.of(holder.asEntity(), entity, data -> {
                            //TODO: addCustomEntityInFrameData event
                        }))
                        .toList())
                .addExtraData(Frame.SHUTTER_SPEED, CameraSettings.SHUTTER_SPEED.getOrDefault(stack))
                .addExtraData(Frame.TIMESTAMP, UnixTimestamp.Seconds.now())
                .updateExtraData(data -> addFrameExtraData(holder, level, stack, captureProperties, data, positionsInFrame, entitiesInFrame))
                .toImmutable();
    }

    protected void addFrameExtraData(CameraHolder holder, ServerLevel level, ItemStack stack, CaptureProperties captureProperties,
                                     ExtraData data, List<BlockPos> positionsInFrame, List<LivingEntity> entitiesInFrame) {
        Entity cameraHolder = holder.asEntity();
        boolean projecting = captureProperties.projection().isPresent();

        if (projecting) {
            data.put(Frame.PROJECTED, true);
            return;
        }

        if (isInSelfieMode(stack)) {
            data.put(Frame.SELFIE, true);
        }
        if (captureProperties.flash()) {
            data.put(Frame.FLASH, true);
        }

        double zoom = CameraSettings.ZOOM.getOrDefault(stack);
        FocalRange focalRange = getFocalRange(level.registryAccess(), stack);
        int focalLength = (int) focalRange.focalLengthFromZoom(zoom);
        data.put(Frame.FOCAL_LENGTH, focalLength);

        captureProperties.extraData().get(CaptureProperties.LIGHT_LEVEL)
                .ifPresent(lightLevel -> data.put(Frame.LIGHT_LEVEL, lightLevel));

        captureProperties.singleChannel().ifPresent(channel ->
                data.put(Frame.COLOR_CHANNEL, channel));

        data.put(Frame.POSITION, cameraHolder.position());
        data.put(Frame.PITCH, cameraHolder.getXRot());
        data.put(Frame.YAW, cameraHolder.getYRot());

        data.put(Frame.DAY_TIME, (int) level.getDayTime());
        data.put(Frame.DIMENSION, level.dimension().location());

        BlockPos blockPos = cameraHolder.blockPosition();

        int surfaceHeight = level.getHeight(Heightmap.Types.WORLD_SURFACE, cameraHolder.getBlockX(), cameraHolder.getBlockZ());
        level.updateSkyBrightness();
        int skyLight = level.getBrightness(LightLayer.SKY, blockPos);

        if (cameraHolder.isUnderWater()) {
            data.put(Frame.UNDERWATER, true);
        }
        if (cameraHolder.getBlockY() < Math.min(level.getSeaLevel(), surfaceHeight) && skyLight == 0) {
            data.put(Frame.IN_CAVE, true);
        } else if (!cameraHolder.isUnderWater()) {
            Biome.Precipitation precipitation = level.getBiome(blockPos).value().getPrecipitationAt(blockPos);
            if (level.isThundering() && precipitation != Biome.Precipitation.NONE)
                data.put(Frame.WEATHER, precipitation == Biome.Precipitation.SNOW ? "Snowstorm" : "Thunder");
            else if (level.isRaining() && precipitation != Biome.Precipitation.NONE)
                data.put(Frame.WEATHER, precipitation == Biome.Precipitation.SNOW ? "Snow" : "Rain");
            else
                data.put(Frame.WEATHER, "Clear");
        }

        // Most common biome:
        positionsInFrame.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .flatMap(pos -> level.getBiome(pos).unwrapKey().map(ResourceKey::location))
                .ifPresent(biome -> data.put(Frame.BIOME, biome));

        List<ResourceLocation> structures = positionsInFrame.stream()
                .map(pos -> LevelUtil.getStructuresAt(level, pos))
                .flatMap(List::stream)
                .collect(Collectors.toSet()) // Remove duplicates
                .stream()
                .toList();
        if (!structures.isEmpty()) {
            data.put(Frame.STRUCTURES, structures);
        }

        //TODO: addExtraData event
    }

    /**
     * Fires 5 rays from camera and obtains positions where they landed. <br>
     * First ray is in the center (equals to look direction).<br>
     * Next are: top left, top right, bottom left, bottom right.
     * These 4 are roughly in positions where rule of thirds cross points are.
     */
    public List<BlockPos> getPositionsInFrame(Entity cameraHolder, float fov) {
        Vec3 eyePos = cameraHolder.getEyePosition();
        Vec3 lookDirection = Vec3.directionFromRotation(cameraHolder.getXRot(), cameraHolder.getYRot());
        // offset roughly corresponds to rule of thirds distance
        float offsetDegrees = (float) ((fov * getCropFactor()) / 4.3);

        return Vec3Util.getCrossVectors(lookDirection, offsetDegrees).stream()
                .map(direction -> {
                    Vec3 endPos = eyePos.add(direction.scale(400));
                    return cameraHolder.level().clip(
                            new ClipContext(eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, cameraHolder));
                })
                .filter(hit -> hit.getType() != HitResult.Type.MISS)
                .map(BlockHitResult::getBlockPos)
                .toList();
    }

    public void addFrameToFilm(ItemStack stack, Frame frame) {
        Attachment.FILM.ifPresentOrElse(stack, (filmItem, filmStack) -> {
            ItemStack updatedFilmStack = filmStack.copy();
            filmItem.addFrame(updatedFilmStack, frame);
            Attachment.FILM.set(stack, updatedFilmStack);
        }, () -> Exposure.LOGGER.error("Cannot add frame: no film attachment is present."));
    }

    public void onFrameAdded(CameraHolder holder, ServerLevel level, ItemStack stack, Frame frame,
                             List<BlockPos> positionsInFrame, List<LivingEntity> entitiesInFrame) {
        ExposureServer.frameHistory().add(holder.asEntity(), frame);

        entitiesInFrame.forEach(entity -> entityCaptured(holder, stack, entity));

        holder.getPlayerAwardedForExposure()
                .filter(player -> player instanceof ServerPlayer)
                .ifPresent(player -> {
                    ServerPlayer serverPlayer = (ServerPlayer) player;
                    serverPlayer.awardStat(Exposure.Stats.FILM_FRAMES_EXPOSED);
                    Exposure.CriteriaTriggers.FRAME_EXPOSED.get().trigger(
                            serverPlayer, holder, stack, frame, positionsInFrame, entitiesInFrame);
                });
    }

    public List<LivingEntity> getEntitiesInFrame(CameraHolder cameraHolder, ServerLevel level, ItemStack stack) {
        float zoom = CameraSettings.ZOOM.getOrDefault(stack);
        double fov = getFocalRange(level.registryAccess(), stack).fovFromZoom(zoom) * getCropFactor();

        return EntitiesInFrame.get(cameraHolder.asEntity(), fov, isInSelfieMode(stack));
    }

    protected void entityCaptured(CameraHolder cameraHolder, ItemStack stack, LivingEntity entity) {
        if (cameraHolder.asEntity() instanceof ServerPlayer player && entity instanceof EnderMan enderMan) {
            boolean lookingAtAngryEnderMan = player.equals(enderMan.getTarget()) && enderMan.isLookingAtMe(player);

            if (lookingAtAngryEnderMan) {
                // I wanted to implement this in a predicate,
                // but it's tricky because EntitySubPredicates do not get the player in their 'match' method.
                // So it's just easier to hardcode it like this.
                Exposure.CriteriaTriggers.PHOTOGRAPH_ENDERMAN_EYES.get().trigger(player);
            }
        }
    }

    public void handleProjectionResult(ServerLevel level, CameraHolder holder, ItemStack stack,
                                       CameraInstance.ProjectionState projectionState, Optional<TranslatableError> error) {
        StoredItemStack filter = Attachment.FILTER.get(stack);
        if (filter.isEmpty()) return;
        if (!(filter.getItem() instanceof InterplanarProjectorItem interplanarProjector)) return;
        if (!interplanarProjector.isConsumable(filter.getForReading())) return;

        Entity entity = holder.asEntity();

        if (projectionState == CameraInstance.ProjectionState.FAILED) {
            ItemStack filterStack = filter.getCopy().transmuteCopy(Exposure.Items.BROKEN_INTERPLANAR_PROJECTOR.get());
            error.ifPresent(err -> filterStack.set(Exposure.DataComponents.INTERPLANAR_PROJECTOR_ERROR_CODE, err.code()));
            Attachment.FILTER.set(stack, filterStack);
            Sound.play(entity, Exposure.SoundEvents.BSOD.get());
            if (getShutter().isOpen(stack)) {
                getShutter().close(holder, level, stack);
            }
            return;
        }

        ItemStack filterStack = filter.getCopy();
        filterStack.shrink(1);
        Attachment.FILTER.set(stack, filterStack);

        if (projectionState == CameraInstance.ProjectionState.TIMED_OUT) {
            Sound.play(entity, Exposure.SoundEvents.INTERPLANAR_PROJECT.get(), entity.getSoundSource(), 0.4f, 0.6f);
        } else if (projectionState == CameraInstance.ProjectionState.SUCCESSFUL) {
            holder.getServerPlayerAwardedForExposure()
                    .ifPresent(player -> Exposure.CriteriaTriggers.SUCCESSFULLY_PROJECT_IMAGE.get().trigger(player));
            Sound.play(entity, Exposure.SoundEvents.INTERPLANAR_PROJECT.get(), entity.getSoundSource(), 0.8f, 1.1f);
            for (int i = 0; i < 16; i++) {
                level.sendParticles(ParticleTypes.PORTAL, entity.getX(), entity.getY() + 1.2, entity.getZ(), 2,
                        entity.getRandom().nextGaussian() * 0.3, entity.getRandom().nextGaussian() * 0.3, entity.getRandom().nextGaussian() * 0.3, 0.01);
            }
        }
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
}

