package io.github.mortuusars.exposure.item;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import io.github.mortuusars.exposure.*;
import io.github.mortuusars.exposure.block.FlashBlock;
import io.github.mortuusars.exposure.camera.CameraClient;
import io.github.mortuusars.exposure.camera.capture.*;
import io.github.mortuusars.exposure.client.capture.converter.ImageConverter;
import io.github.mortuusars.exposure.client.snapshot.Captor;
import io.github.mortuusars.exposure.client.snapshot.SnapShot;
import io.github.mortuusars.exposure.client.snapshot.capturing.component.CaptureComponents;
import io.github.mortuusars.exposure.client.snapshot.capturing.method.BackgroundScreenshotCaptureMethod;
import io.github.mortuusars.exposure.client.snapshot.capturing.method.FileCaptureMethod;
import io.github.mortuusars.exposure.client.snapshot.capturing.method.InvertedFallbackCaptureMethod;
import io.github.mortuusars.exposure.client.snapshot.capturing.method.ScreenshotCaptureMethod;
import io.github.mortuusars.exposure.client.snapshot.converter.Converter;
import io.github.mortuusars.exposure.client.snapshot.processing.Processor;
import io.github.mortuusars.exposure.client.snapshot.saving.FileSaver;
import io.github.mortuusars.exposure.core.*;
import io.github.mortuusars.exposure.core.camera.*;
import io.github.mortuusars.exposure.camera.capture.component.*;
import io.github.mortuusars.exposure.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.core.EntitiesInFrame;
import io.github.mortuusars.exposure.core.frame.FrameProperties;
import io.github.mortuusars.exposure.core.frame.Photographer;
import io.github.mortuusars.exposure.item.component.EntityInFrame;
import io.github.mortuusars.exposure.item.component.ExposureFrame;
import io.github.mortuusars.exposure.item.component.StoredItemStack;
import io.github.mortuusars.exposure.item.component.camera.ShutterState;
import io.github.mortuusars.exposure.menu.CameraAttachmentsMenu;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.OnFrameAddedS2CP;
import io.github.mortuusars.exposure.network.packet.client.StartExposureS2CP;
import io.github.mortuusars.exposure.network.packet.server.OpenCameraAttachmentsInCreativePacketC2SP;
import io.github.mortuusars.exposure.sound.OnePerEntitySounds;
import io.github.mortuusars.exposure.util.ChromaticChannel;
import io.github.mortuusars.exposure.util.Fov;
import io.github.mortuusars.exposure.util.LevelUtil;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class CameraItem extends Item {
    private static final String ID_OF_LAST_SHOT = "id_of_last_shot";
    private static final String FLASH_HAS_FIRED_ON_LAST_SHOT = "flash_has_fired_on_last_shot";
    private static final String LIGHT_LEVEL_ON_LAST_SHOT = "light_level_on_last_shot";
    protected final List<ShutterSpeed> shutterSpeeds;
    protected final List<AttachmentType> attachments;

    public CameraItem(Properties properties) {
        super(properties);
        shutterSpeeds = ImmutableList.copyOf(defineShutterSpeeds());
        attachments = ImmutableList.copyOf(defineAttachments());
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

    protected List<AttachmentType> defineAttachments() {
        return List.of(
                AttachmentType.FILM,
                AttachmentType.FLASH,
                AttachmentType.LENS,
                AttachmentType.FILTER
        );
    }

    public List<ShutterSpeed> getShutterSpeeds(ItemStack stack) {
        return shutterSpeeds;
    }

    public List<AttachmentType> getAttachments(ItemStack stack) {
        return attachments;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 1000;
    }

    public boolean isBarVisible(@NotNull ItemStack stack) {
        if (!Config.Client.CAMERA_SHOW_FILM_BAR_ON_ITEM.get())
            return false;

        StoredItemStack filmStack = getAttachment(stack, AttachmentType.FILM);
        return filmStack.getItem() instanceof FilmRollItem filmRollItem && filmRollItem.isBarVisible(filmStack.getForReading());
    }

    public int getBarWidth(@NotNull ItemStack stack) {
        if (!Config.Client.CAMERA_SHOW_FILM_BAR_ON_ITEM.get())
            return 0;

        StoredItemStack filmStack = getAttachment(stack, AttachmentType.FILM);
        return filmStack.getItem() instanceof FilmRollItem filmRollItem ? filmRollItem.getBarWidth(filmStack.getForReading()) : 0;
    }

    public int getBarColor(@NotNull ItemStack stack) {
        if (!Config.Client.CAMERA_SHOW_FILM_BAR_ON_ITEM.get())
            return 0;

        StoredItemStack filmStack = getAttachment(stack, AttachmentType.FILM);
        return filmStack.getItem() instanceof FilmRollItem filmRollItem ? filmRollItem.getBarColor(filmStack.getForReading()) : 0;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY)
            return false;

        if (otherStack.isEmpty() && Config.Common.CAMERA_GUI_RIGHT_CLICK_ATTACHMENTS_SCREEN.get()) {
            if (!(slot.container instanceof Inventory)) {
                return false; // Cannot open when not in player's inventory
            }

            if (player.isCreative() && player.level().isClientSide()/* && CameraItemClientExtensions.isInCreativeModeInventory()*/) {
                Packets.sendToServer(new OpenCameraAttachmentsInCreativePacketC2SP(slot.getContainerSlot()));
                return true;
            }

            openCameraAttachmentsMenu(player, slot.getContainerSlot());
            return true;
        }

//        if (PlatformHelper.canShear(otherStack) && !isTooltipRemoved(stack)) {
//            if (otherStack.isDamageableItem() && player instanceof ServerPlayer serverPlayer) {
//                // broadcasting break event is expecting item to be in hand,
//                // but making it work for carried items would be too much work for such small feature.
//                // No one will ever notice it anyway.
//                otherStack.hurtAndBreak(1, serverPlayer.serverLevel(),
//                        serverPlayer, item -> serverPlayer.onEquippedItemBroken(item, EquipmentSlot.MAINHAND));
//            }
//
//            if (player.level().isClientSide)
//                player.playSound(SoundEvents.SHEEP_SHEAR);
//
//            setTooltipRemoved(stack, true);
//            return true;
//        }
//
//        if (isTooltipRemoved(stack) && (otherStack.getItem() instanceof BookItem || otherStack.getItem() instanceof WritableBookItem
//                || otherStack.getItem() instanceof WrittenBookItem || otherStack.getItem() instanceof KnowledgeBookItem)) {
//            setTooltipRemoved(stack, false);
//            if (player.level().isClientSide)
//                player.playSound(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT);
//            return true;
//        }

        if (Config.Common.CAMERA_GUI_RIGHT_CLICK_HOTSWAP.get()) {
            for (AttachmentType attachmentType : getAttachments(stack)) {
                if (attachmentType.matches(otherStack)) {
                    StoredItemStack currentAttachment = getAttachment(stack, attachmentType);

                    if (otherStack.getCount() > 1 && !currentAttachment.isEmpty()) {
                        if (player.level().isClientSide())
                            playCameraSound(player, Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 0.9f, 1f);
                        return true; // Cannot swap when holding more than one item
                    }

                    setAttachment(stack, attachmentType, otherStack.split(1));

                    ItemStack returnedStack = !currentAttachment.isEmpty() ? currentAttachment.getCopy() : otherStack;
                    access.set(returnedStack);

                    attachmentType.sound().playOnePerPlayer(player, false);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag tooltipFlag) {
        if (Config.Client.CAMERA_SHOW_FILM_FRAMES_IN_TOOLTIP.get()) {
            StoredItemStack filmStack = getAttachment(stack, AttachmentType.FILM);
            if (filmStack.getItem() instanceof FilmRollItem filmRollItem) {
                int exposed = filmRollItem.getStoredFramesCount(filmStack.getForReading());
                int max = filmRollItem.getMaxFrameCount(filmStack.getForReading());
                components.add(Component.translatable("item.exposure.camera.tooltip.film_roll_frames", exposed, max));
            }
        }

        if (/*!isTooltipRemoved(stack) &&*/ Config.Client.CAMERA_SHOW_TOOLTIP_DETAILS.get()) {
            boolean rClickAttachments = Config.Common.CAMERA_GUI_RIGHT_CLICK_ATTACHMENTS_SCREEN.get();
            boolean rClickHotswap = Config.Common.CAMERA_GUI_RIGHT_CLICK_HOTSWAP.get();

            if (rClickAttachments || rClickHotswap) {
                if (Screen.hasShiftDown()) {
                    if (rClickAttachments)
                        components.add(Component.translatable("item.exposure.camera.tooltip.details_attachments_screen"));
                    if (rClickHotswap)
                        components.add(Component.translatable("item.exposure.camera.tooltip.details_hotswap"));
                    // components.add(Component.translatable("item.exposure.camera.tooltip.details_remove_tooltip"));
                } else
                    components.add(Component.translatable("tooltip.exposure.hold_for_details"));
            }
        }
    }

    public boolean isActive(ItemStack stack) {
        return stack.get(Exposure.DataComponents.CAMERA_ACTIVE) != null;
    }

    public void setActive(ItemStack stack, boolean active) {
        setOrRemoveBooleanComponent(stack, Exposure.DataComponents.CAMERA_ACTIVE, active);
    }

    public void activate(Player player, ItemStack stack) {
        if (!isActive(stack)) {
            setActive(stack, true);
            player.gameEvent(GameEvent.EQUIP); // Sends skulk vibrations
            playCameraSound(player, Exposure.SoundEvents.VIEWFINDER_OPEN.get(), 0.35f, 0.9f, 0.2f);
        }
    }

    public void deactivate(Player player, ItemStack stack) {
        if (isActive(stack)) {
            setActive(stack, false);
            player.gameEvent(GameEvent.EQUIP); // Sends skulk vibrations
            playCameraSound(player, Exposure.SoundEvents.VIEWFINDER_CLOSE.get(), 0.35f, 0.9f, 0.2f);
        }
    }

    public boolean isInSelfieMode(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.SELFIE_MODE, false);
    }

    public void setSelfieMode(ItemStack stack, boolean active) {
        setOrRemoveBooleanComponent(stack, Exposure.DataComponents.SELFIE_MODE, active);
    }

    public void setSelfieModeWithEffects(Player player, ItemStack stack, boolean selfie) {
        if (isInSelfieMode(stack) != selfie) {
            setSelfieMode(stack, selfie);
            player.level().playSound(player, player, Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), SoundSource.PLAYERS, 1f, 1.5f);
        }
    }

    public ShutterState getShutterState(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.SHUTTER_STATE, ShutterState.CLOSED);
    }

    public void setShutterState(ItemStack stack, ShutterState shutterState) {
        stack.set(Exposure.DataComponents.SHUTTER_STATE, shutterState);
    }

    public void openShutter(ServerPlayer player, Level level, ItemStack stack, ShutterSpeed shutterSpeed) {
        setShutterState(stack, ShutterState.open(level.getGameTime(), shutterSpeed));

        player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        playCameraSound(null, player, Exposure.SoundEvents.SHUTTER_OPEN.get(), 0.7f, 1.1f, 0.2f);
    }

    public void closeShutter(ServerPlayer player, ItemStack stack) {
        ShutterState shutterState = getShutterState(stack);
        long closeTick = shutterState.getCloseTick();
        boolean flashHasFired = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe().getBoolean(FLASH_HAS_FIRED_ON_LAST_SHOT);

        setShutterState(stack, ShutterState.closed());

        if (player.level().getGameTime() - closeTick >= 30 /*1.5 sec*/) {
            // Skip effects if shutter was closed long ago
            return;
        }

        player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        player.getCooldowns().addCooldown(this, flashHasFired ? 10 : 2);
        playCameraSound(null, player, Exposure.SoundEvents.SHUTTER_CLOSE.get(), 0.7f, 1.1f, 0.2f);

        StoredItemStack filmStack = getAttachment(stack, AttachmentType.FILM);
        if (filmStack.getItem() instanceof FilmRollItem filmRollItem) {
            float fullness = filmRollItem.getFullness(filmStack.getForReading());
            boolean isFull = fullness == 1f;

            if (isFull)
                OnePerEntitySounds.play(null, player, Exposure.SoundEvents.FILM_ADVANCE_LAST.get(), SoundSource.PLAYERS, 1f, 1f);
            else {
                OnePerEntitySounds.play(null, player, Exposure.SoundEvents.FILM_ADVANCING.get(), SoundSource.PLAYERS,
                        1f, 0.9f + 0.1f * fullness);
            }
        }
    }

    @SuppressWarnings("unused")
    public void playCameraSound(@NotNull Player player, SoundEvent sound, float volume, float pitch) {
        playCameraSound(player, sound, volume, pitch, 0f);
    }

    public void playCameraSound(@NotNull Player player, SoundEvent sound, float volume, float pitch, float pitchVariety) {
        playCameraSound(player, player, sound, volume, pitch, pitchVariety);
    }

    public void playCameraSound(@Nullable Player player, @NotNull Entity origin, SoundEvent sound,
                                float volume, float pitch, float pitchVariety) {
        if (pitchVariety > 0f)
            pitch = pitch - (pitchVariety / 2f) + (origin.getRandom().nextFloat() * pitchVariety);
        origin.level().playSound(player, origin, sound, SoundSource.PLAYERS, volume, pitch);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        if (!(entity instanceof Player player))
            return;

        ShutterState shutterState = getShutterState(stack);

        if (shutterState.isOpen() && level.getGameTime() >= shutterState.getCloseTick() && player instanceof ServerPlayer serverPlayer) {
            closeShutter(serverPlayer, stack);
        }

        boolean inOffhand = player.getOffhandItem().equals(stack);
        boolean inHand = isSelected || inOffhand;

        if (!inHand) {
            deactivate(player, stack);

            if (level.isClientSide() && Viewfinder.isOpen()) {
                Viewfinder.close();
            }
        }
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null) {
            InteractionHand hand = context.getHand();

            //TODO: both hands
//            if (hand == InteractionHand.MAIN_HAND && Camera.getCamera(player)
//                    .filter(c -> c instanceof CameraInHand<?>)
//                    .map(c -> ((CameraInHand<?>) c).getHand() == InteractionHand.OFF_HAND).orElse(false)) {
//                return InteractionResult.PASS;
//            }

            return useCamera(player, hand);
        }
        return InteractionResult.CONSUME; // To not play attack animation.
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        //TODO: both hands
//        if (hand == InteractionHand.MAIN_HAND && Camera.getCamera(player)
//                .filter(c -> c instanceof CameraInHand<?>)
//                .map(c -> ((CameraInHand<?>) c).getHand() == InteractionHand.OFF_HAND).orElse(false)) {
//            return InteractionResultHolder.pass(player.getItemInHand(hand));
//        }

        useCamera(player, hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    public InteractionResult useCamera(Player player, InteractionHand hand) {

        if (player.level().isClientSide) {

            SnapShot snapshot = SnapShot.create()
                    .captureWith(Captor.builder()
                            .method(new InvertedFallbackCaptureMethod(
                                    new FileCaptureMethod("D:\\test.png"),
                                    new ScreenshotCaptureMethod(),
                                    err -> player.displayClientMessage(Component.translatable(err.casualTranslationKey()), true)))
                            .addComponents(
                                    CaptureComponents.hideGui(),
                                    CaptureComponents.forceFirstPerson())
                            .create())
                    .then(image -> image
                            .apply(Converter.DITHERED_MAP_COLORS::convert)
                            .thenConsume(palettedImage -> new FileSaver(
                                    new File("D:\\snapshot_test\\" + player.level().getGameTime() + ".png"))
                                    .save(palettedImage)))
                    .build();

            ExposureClient.snapshot().enqueue(snapshot);


//            SnapShot.builder()
//                    .captor(Captor.builder()
//                            .method(CaptureMethods.screenshot())
//                            .addComponents(
//                                    CaptureComponents.HIDE_GUI,
//                                    CaptureComponents.FORCE_FIRST_PERSON,
//                                    CaptureComponents.DISABLE_POST_EFFECT,
//                                    CaptureComponents.optional(brightnessStops != 0, () -> CaptureComponents.gammaModification(brightnessStops)))
//                            .process(Processor.builder()
//                                    .addSteps(
//                                            ProcessingSteps.crop(Crop.SQUARE, getCropFactor()),
//                                            ProcessingSteps.optional(film.getType() == FilmType.BLACK_AND_WHITE, () -> ProcessingSteps.blackAndWhite()),
//                                            ))
//                            .convert(Converter.DITHERED_MAP_COLORS)
//                            .save(new FileSaver("D:/image.png"));
        }

        if (true) {
            return InteractionResult.SUCCESS;
        }

        //  aaaaaaaaaaaa


        if (player.getCooldowns().isOnCooldown(this))
            return InteractionResult.FAIL;

        ItemStack cameraStack = player.getItemInHand(hand);
        if (cameraStack.isEmpty() || cameraStack.getItem() != this)
            return InteractionResult.PASS;

        boolean active = isActive(cameraStack);

        if (!active && player.isSecondaryUseActive()) {
            if (getShutterState(cameraStack).isOpen()) {
                player.displayClientMessage(Component.translatable("item.exposure.camera.camera_attachments.fail.shutter_open")
                        .withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }

            int cameraSlot = getMatchingSlotInInventory(player.getInventory(), cameraStack);
            if (cameraSlot < 0)
                return InteractionResult.FAIL;

            openCameraAttachmentsMenu(player, cameraSlot);
            return InteractionResult.SUCCESS;
        }

        if (!active) {
            activate(player, cameraStack);
            player.getCooldowns().addCooldown(this, 4);

            if (player.level().isClientSide) {
                CameraClient.setActiveCameraAccessor(CameraAccessors.ofHand(hand));
                // Release use key after activating. Otherwise, if right click is still held - camera will take a shot
                CameraItemClientExtensions.releaseUseButton();
            }

            return InteractionResult.CONSUME; // Consume to not play animation
        }


        // All server-side below this point

        if (!(player instanceof ServerPlayer serverPlayer))
            return InteractionResult.CONSUME;

        playCameraSound(null, player, Exposure.SoundEvents.CAMERA_RELEASE_BUTTON_CLICK.get(), 0.3f, 1f, 0.1f);

        StoredItemStack filmStack = getAttachment(cameraStack, AttachmentType.FILM);

        if (filmStack.isEmpty() || !(filmStack.getItem() instanceof FilmRollItem filmItem))
            return InteractionResult.FAIL;

        boolean exposingFilm = filmItem.canAddFrame(filmStack.getForReading());

        if (!exposingFilm)
            return InteractionResult.FAIL;

        if (getShutterState(cameraStack).isOpen())
            return InteractionResult.FAIL;

        int lightLevel = LevelUtil.getLightLevelAt(player.level(), player.blockPosition());
        boolean shouldFlashFire = shouldFlashFire(player, cameraStack, lightLevel);
        ShutterSpeed shutterSpeed = getShutterSpeed(cameraStack);

//        if (PlatformHelper.fireShutterOpeningEvent(player, cameraStack, lightLevel, shouldFlashFire))
//            return InteractionResult.FAIL; // Canceled

        boolean flashHasFired = shouldFlashFire && tryUseFlash(player, cameraStack);

        openShutter(serverPlayer, player.level(), cameraStack, shutterSpeed);

        if (shutterSpeed.shouldCauseTickingSound()) {
            OnePerEntitySounds.playShutterTickingSoundForAllPlayers(CameraAccessors.ofHand(hand), player,
                    1f, 1f, shutterSpeed.getDurationTicks());
        }

        String exposureId = createExposureId(player);

        CustomData.update(DataComponents.CUSTOM_DATA, cameraStack, tag -> {
            tag.putString(ID_OF_LAST_SHOT, exposureId);
            tag.putBoolean(FLASH_HAS_FIRED_ON_LAST_SHOT, flashHasFired);
            tag.putInt(LIGHT_LEVEL_ON_LAST_SHOT, lightLevel);
        });

        ExposureServer.awaitExposure(exposureId, filmItem.getType(), player.getScoreboardName());

        CameraAccessor cameraAccessor = CameraAccessors.ofHand(hand);
        Packets.sendToClient(new StartExposureS2CP(exposureId, cameraAccessor, flashHasFired, lightLevel), serverPlayer);

        return InteractionResult.CONSUME; // Consume to not play swing animation
    }

    public ExposureFrameClientData getClientSideFrameData(Player player, ItemStack cameraStack) {
        //TODO: figure out how to know when image was loaded
        boolean projectingFile = hasInterplanarProjectorFilter(cameraStack);

        List<UUID> entitiesInFrame;

        if (projectingFile) {
            entitiesInFrame = Collections.emptyList();
        } else {
            entitiesInFrame = EntitiesInFrame.get(player, Viewfinder.getCurrentFov(), Exposure.MAX_ENTITIES_IN_FRAME, isInSelfieMode(cameraStack))
                    .stream()
                    .map(Entity::getUUID)
                    .toList();
        }

        CompoundTag extraData = new CompoundTag();
        //TODO: get additional data event

        return new ExposureFrameClientData(projectingFile, entitiesInFrame, extraData);
    }

    public void exposeFrameClientside(Player player, Camera camera, String exposureId, boolean flashHasFired) {
//        Preconditions.checkState(player.level().isClientSide, "Should only be called on client.");

//        if (PlatformHelper.fireShutterOpeningEvent(player, cameraStack, lightLevelBeforeShot, flashHasFired))
//            return; // Canceled

//        frame.getAdditionalData().update(tag -> tag.put)

//        // Base properties. It's easier to add them client-side.
//        frame.putString(FrameProperties.TIMESTAMP, Util.getFilenameFormattedDateTime());
//
//        if (!projectingFile) {
//            frame.putInt(FrameProperties.FOCAL_LENGTH, Mth.ceil(getZoom(cameraStack)));
//            frame.putInt(FrameProperties.LIGHT_LEVEL, lightLevelBeforeShot);
//            frame.putFloat(FrameProperties.SUN_ANGLE, player.level().getSunAngle(0));
//            if (flashHasFired)
//                frame.putBoolean(FrameProperties.FLASH, true);
//            if (isInSelfieMode(cameraStack))
//                frame.putBoolean(FrameProperties.SELFIE, true);
//
//            if (ExposureClient.isShaderActive()) {
//                // Chromatic only for black and white:
//                boolean isBW = getAttachment(cameraStack, AttachmentType.FILM).getItem() instanceof IFilmItem filmItem
//                        && filmItem.getType() == ExposureType.BLACK_AND_WHITE;
//
//                if (isBW) {
//                    ItemStack filterStack = getAttachment(cameraStack, AttachmentType.FILTER);
//                    ColorChannel.fromStack(filterStack).ifPresent(channel -> {
//                        frame.putBoolean(FrameProperties.CHROMATIC, true);
//                        frame.putString(FrameProperties.CHROMATIC_CHANNEL, channel.getSerializedName());
//                    });
//                }
//            }
//        }


//        ExposureClient.captureManager().enqueue();

        startCapture(player, camera.getItemStack(), exposureId, flashHasFired);
    }

    protected void startCapture(Player player, ItemStack cameraStack, String exposureId, boolean flashHasFired) {
        Capture capture;

        //TODO: Get film properties here and pass them to createXXXCapture

        StoredItemStack filterStack = getAttachment(cameraStack, AttachmentType.FILTER);

        if (filterStack.getItem() instanceof InterplanarProjectorItem projector && projector.isAllowed()) {
            String filepath = projector.getFilepath(filterStack.getForReading()).orElse("");
            boolean dither = projector.getMode(filterStack.getForReading()) == InterplanarProjectorMode.DITHERED;

            capture = createFileCapture(player, cameraStack, exposureId, filepath, dither)
                    .onCapturingFailed(() -> {
                        Capture regularCapture = createRegularCapture(player, cameraStack, exposureId, flashHasFired);
                        regularCapture.onImageCaptured(() -> {
                            Minecraft.getInstance().execute(() -> {
                                player.level().playSound(player, player, Exposure.SoundEvents.INTERPLANAR_PROJECT.get(),
                                        SoundSource.PLAYERS, 0.8f, 0.6f);
                                for (int i = 0; i < 32; ++i) {
                                    player.level().addParticle(ParticleTypes.PORTAL, player.getX(), player.getY() + player.getRandom().nextDouble() * 2.0, player.getZ(), player.getRandom().nextGaussian(), 0.0, player.getRandom().nextGaussian());
                                }
                            });
                        });
                        ExposureClient.captureManager().enqueue(regularCapture);
                    })
                    .onImageCaptured(() -> {
                        Minecraft.getInstance().execute(() -> {
                            player.level().playSound(player, player, Exposure.SoundEvents.INTERPLANAR_PROJECT.get(),
                                    SoundSource.PLAYERS, 0.8f, 1.1f);
                            for (int i = 0; i < 32; ++i) {
                                player.level().addParticle(ParticleTypes.PORTAL, player.getX(), player.getY() + player.getRandom().nextDouble() * 2.0, player.getZ(), player.getRandom().nextGaussian(), 0.0, player.getRandom().nextGaussian());
                            }
                        });
                    });
        } else {
            capture = createRegularCapture(player, cameraStack, exposureId, flashHasFired);
            if (flashHasFired) {
                capture.onImageCaptured(() -> spawnClientsideFlashEffects(player, cameraStack));
            }
        }

        ExposureClient.captureManager().enqueue(capture);
    }

    protected Capture createRegularCapture(Player player, ItemStack cameraStack, String exposureId, boolean flash) {
        StoredItemStack filmStack = getAttachment(cameraStack, AttachmentType.FILM);
        if (filmStack.isEmpty() || !(filmStack.getItem() instanceof FilmRollItem filmItem)) {
            throw new IllegalStateException("Film attachment should be present at the time of capture.");
        }

        int frameSize = filmItem.getFrameSize(filmStack.getForReading());
        float brightnessStops = getShutterSpeed(cameraStack).getStopsDifference(ShutterSpeed.DEFAULT);

        Capture capture = new BackgroundScreenshotCapture()
                .setAsyncProcessing(false)
                .setFilmType(filmItem.getType())
                .setSize(frameSize)
                .setBrightnessStops(brightnessStops)
                .setConverter(ImageConverter.DITHERED_MAP_COLORS);

        capture.addComponent(new BaseComponent());
        capture.addComponent(new ExposureUploaderComponent(exposureId));

        if (flash) {
            capture.addComponent(new FlashComponent());
        }
        if (brightnessStops != 0) {
            capture.addComponent(new BrightnessComponent(brightnessStops));
        }
        if (filmItem.getType() == ExposureType.BLACK_AND_WHITE) {
            StoredItemStack filterStack = getAttachment(cameraStack, AttachmentType.FILTER);
            ChromaticChannel.fromStack(filterStack.getForReading()).ifPresentOrElse(
                    channel -> capture.addComponent(new SelectiveChannelBlackAndWhiteComponent(channel)),
                    () -> capture.addComponent(new BlackAndWhiteComponent()));
        }

        return capture;
    }

    protected Capture createFileCapture(Player player, ItemStack cameraStack, String exposureId,
                                        String filepath, boolean dither) {
        StoredItemStack filmStack = getAttachment(cameraStack, AttachmentType.FILM);
        if (filmStack.isEmpty() || !(filmStack.getItem() instanceof FilmRollItem filmItem)) {
            throw new IllegalStateException("Film attachment should be present at the time of capture.");
        }

        ExposureType exposureType = filmItem.getType();
        int frameSize = filmItem.getFrameSize(filmStack.getForReading());

        Capture capture = new FileCapture(filepath,
                error -> player.displayClientMessage(error.getCasualTranslation().withStyle(ChatFormatting.RED), false))
                .setFilmType(exposureType)
                .setSize(frameSize)
                .addComponent(new ExposureUploaderComponent(exposureId))
                .setConverter(dither ? ImageConverter.DITHERED_MAP_COLORS : ImageConverter.NEAREST_MAP_COLORS)
                .cropFactor(1)
                .setAsyncCapturing(true);

        if (exposureType == ExposureType.BLACK_AND_WHITE) {
            capture.addComponent(new BlackAndWhiteComponent());
        }

        return capture;
    }

    public ExposureFrame createExposureFrame(ServerPlayer player, ItemStack cameraStack, ExposureFrameClientData dataFromClient) {
        CompoundTag cameraCustomData = cameraStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        String id = cameraCustomData.getString(ID_OF_LAST_SHOT);

        if (StringUtil.isBlank(id)) {
            Exposure.LOGGER.error("Cannot create an exposure frame: '{}' is missing or empty in Camera's custom data.", ID_OF_LAST_SHOT);
            return ExposureFrame.EMPTY;
        }

        ExposureType type = getAttachment(cameraStack, AttachmentType.FILM).getItem() instanceof IFilmItem filmItem
                ? filmItem.getType() : ExposureType.COLOR;

        ExposureFrameTag tag = new ExposureFrameTag();

        if (cameraCustomData.getBoolean(FLASH_HAS_FIRED_ON_LAST_SHOT)) {
            tag.putBoolean(ExposureFrameTag.FLASH, true);
        }

        int lightLevel = cameraCustomData.getInt(LIGHT_LEVEL_ON_LAST_SHOT);
        if (lightLevel >= 0) {
            tag.putInt(ExposureFrameTag.LIGHT_LEVEL, lightLevel);
        }

        // Chromatic channel
        ChromaticChannel.fromStack(getAttachment(cameraStack, AttachmentType.FILTER).getForReading())
                .ifPresent(channel -> tag.putString(ExposureFrameTag.CHROMATIC_CHANNEL, channel.getSerializedName()));

        // Do not forget to add data from client:
        if (dataFromClient.loadingFromFile()) {
            tag.putBoolean(ExposureFrameTag.FROM_FILE, true);
        }
        tag.merge(dataFromClient.extraData());


        //TODO: ... other properties
        Level level = player.level();
//
//        ListTag pos = new ListTag();
//        pos.add(IntTag.valueOf(player.blockPosition().getX()));
//        pos.add(IntTag.valueOf(player.blockPosition().getY()));
//        pos.add(IntTag.valueOf(player.blockPosition().getZ()));
//        frame.put(FrameProperties.POSITION, pos);
//
//        frame.putInt(FrameProperties.DAYTIME, (int) level.getDayTime());
//
//        frame.putString(FrameProperties.DIMENSION, player.level().dimension().location().toString());
//
//        player.level().getBiome(player.blockPosition()).unwrapKey().map(ResourceKey::location)
//                .ifPresent(biome -> frame.putString(FrameProperties.BIOME, biome.toString()));
//
//        int surfaceHeight = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, player.getBlockX(), player.getBlockZ());
//        level.updateSkyBrightness();
//        int skyLight = level.getBrightness(LightLayer.SKY, player.blockPosition());
//
//        if (player.isUnderWater())
//            frame.putBoolean(FrameProperties.UNDERWATER, true);
//
//        if (player.getBlockY() < surfaceHeight && skyLight < 2)
//            frame.putBoolean(FrameProperties.IN_CAVE, true);
//        else if (!player.isUnderWater()) {
//            Biome.Precipitation precipitation = level.getBiome(player.blockPosition()).value().getPrecipitationAt(player.blockPosition());
//            if (level.isThundering() && precipitation != Biome.Precipitation.NONE)
//                frame.putString(FrameProperties.WEATHER, precipitation == Biome.Precipitation.SNOW ? "Snowstorm" : "Thunder");
//            else if (level.isRaining() && precipitation != Biome.Precipitation.NONE)
//                frame.putString(FrameProperties.WEATHER, precipitation == Biome.Precipitation.SNOW ? "Snow" : "Rain");
//            else
//                frame.putString(FrameProperties.WEATHER, "Clear");
//        }
//
//        addStructuresInfo(player, frame);
//
//        if (!capturedEntities.isEmpty()) {
//            ListTag entities = new ListTag();
//
//            for (Entity entity : capturedEntities) {
//                if (entity instanceof EnderMan enderMan && player.equals(enderMan.getTarget()) && enderMan.isLookingAtMe(player)) {
//                    // I wanted to implement this in a predicate,
//                    // but it's tricky because EntitySubPredicates do not get the player in their 'match' method.
//                    // So it's just easier to hardcode it like this.
//                    Exposure.CriteriaTriggers.PHOTOGRAPH_ENDERMAN_EYES.trigger(player);
//                }
//
//                CompoundTag entityInfoTag = createEntityInFrameTag(entity, player, cameraStack);
//                if (entityInfoTag.isEmpty())
//                    continue;
//
//                entities.add(entityInfoTag);
//
//                // Duplicate entity exposureId as a separate field in the tag.
//                // Can then be used by FTBQuests nbt matching (it's hard to match from a list), for example.

//                frame.putBoolean(entityInfoTag.getString(FrameProperties.ENTITY_ID), true);
//            }
//
//            if (!entities.isEmpty())
//                frame.put(FrameProperties.ENTITIES_IN_FRAME, entities);
//        }

        //TODO: add entities separately

        List<Entity> capturedEntitiesOnClient = dataFromClient.getCapturedEntities(player.serverLevel());
        List<Entity> capturedEntities = crossCheckCapturedEntities(player, cameraStack, capturedEntitiesOnClient);

        List<EntityInFrame> entitiesInFrame = new ArrayList<>();

        //TODO: modifyFrameData event
//        PlatformHelper.fireModifyFrameDataEvent(player, cameraStack, frame, entities);
        //TODO: modifyEntityInFrameData event


        return new ExposureFrame(new ExposureIdentifier(id), type, new Photographer(player), entitiesInFrame, CustomData.of(tag));
    }

    /**
     * Returns an intersection of entities on both sides, i.e. if entity is captured on client but not on server - discard it.
     */
    public List<Entity> crossCheckCapturedEntities(ServerPlayer player, ItemStack cameraStack, List<Entity> entitiesOnClient) {
        if (entitiesOnClient.isEmpty()) {
            return entitiesOnClient;
        }

        FocalRange focalRange = getFocalRange(cameraStack);
        double zoom = getZoomPercentage(cameraStack);
        double fov = Mth.map(zoom, 0, 1, Fov.focalLengthToFov(focalRange.min()), Fov.focalLengthToFov(focalRange.max()));

        List<Entity> entitiesOnServer = EntitiesInFrame.get(player, fov, Exposure.MAX_ENTITIES_IN_FRAME, isInSelfieMode(cameraStack));

        ArrayList<Entity> entities = new ArrayList<>(entitiesOnClient);
        entities.retainAll(entitiesOnServer);
        return entities;
    }

    public void addFrame(ServerPlayer player, ItemStack cameraStack, ExposureFrame exposureFrame) {
        //TODO: modifyEntityInFrameData event
//        PlatformHelper.fireModifyFrameDataEvent(player, cameraStack, frame, entities);

        player.awardStat(Exposure.Stats.FILM_FRAMES_EXPOSED);
        //TODO: advancement trigger
//        Exposure.CriteriaTriggers.FILM_FRAME_EXPOSED.trigger(player, new ItemAndStack<>(cameraStack), frame, entities);

        addFrameToFilm(cameraStack, exposureFrame);
        onFrameAdded(player, cameraStack, exposureFrame);
//        PlatformHelper.fireFrameAddedEvent(player, cameraStack, exposureFrame);
        Packets.sendToClient(new OnFrameAddedS2CP(exposureFrame), player);
    }

    public void onFrameAdded(ServerPlayer player, ItemStack cameraStack, ExposureFrame frame) {
        if (frame.isFromFile()) {
            StoredItemStack filterStack = getAttachment(cameraStack, AttachmentType.FILTER);
            if (!filterStack.isEmpty() && filterStack.getItem() instanceof InterplanarProjectorItem interplanarProjector) {
                // Player sound to other players
                player.level().playSound(player, player, Exposure.SoundEvents.INTERPLANAR_PROJECT.get(),
                        SoundSource.PLAYERS, 0.8f, 1f);

                if (interplanarProjector.isConsumable(filterStack.getForReading())) {
                    ItemStack filterCopy = filterStack.getCopy();
                    filterCopy.shrink(1);
                    setAttachment(cameraStack, AttachmentType.FILTER, filterCopy);
                }
            }
        }
    }

    public void addFrameToFilm(ItemStack cameraStack, ExposureFrame frame) {
        StoredItemStack filmStack = getAttachment(cameraStack, AttachmentType.FILM);
        if (filmStack.isEmpty() || !(filmStack.getItem() instanceof FilmRollItem filmItem)) {
            Exposure.LOGGER.error("Cannot add frame: no film attachment is present.");
            return;
        }

        ItemStack filmStackCopy = filmStack.getCopy();

        filmItem.addFrame(filmStackCopy, frame);
        setAttachment(cameraStack, AttachmentType.FILM, filmStackCopy);
    }

    protected boolean shouldFlashFire(Player player, ItemStack cameraStack, int lightLevel) {
        if (!hasFlash(cameraStack))
            return false;

        return switch (getFlashMode(cameraStack)) {
            case OFF -> false;
            case ON -> true;
            case AUTO -> lightLevel < 8;
        };
    }

    public boolean tryUseFlash(Player player, ItemStack cameraStack) {
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
        level.playSound(null, player, Exposure.SoundEvents.FLASH.get(), SoundSource.PLAYERS, 1f, 1f);

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

    protected void addStructuresInfo(@NotNull ServerPlayer player, CompoundTag frame) {
        Map<Structure, LongSet> allStructuresAt = player.serverLevel().structureManager().getAllStructuresAt(player.blockPosition());

        List<Structure> inside = new ArrayList<>();

        for (Structure structure : allStructuresAt.keySet()) {
            StructureStart structureAt = player.serverLevel().structureManager().getStructureAt(player.blockPosition(), structure);
            if (structureAt.isValid()) {
                inside.add(structure);
            }
        }

        Registry<Structure> structures = player.serverLevel().registryAccess().registryOrThrow(Registries.STRUCTURE);
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

    protected CompoundTag createEntityInFrameTag(Entity entity, Player photographer, ItemStack cameraStack) {
        CompoundTag tag = new CompoundTag();
        ResourceLocation entityRL = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());

        tag.putString(FrameProperties.ENTITY_ID, entityRL.toString());

        ListTag pos = new ListTag();
        pos.add(IntTag.valueOf((int) entity.getX()));
        pos.add(IntTag.valueOf((int) entity.getY()));
        pos.add(IntTag.valueOf((int) entity.getZ()));
        tag.put(FrameProperties.ENTITY_POSITION, pos);

        tag.putFloat(FrameProperties.ENTITY_DISTANCE, photographer.distanceTo(entity));

        if (entity instanceof Player player)
            tag.putString(FrameProperties.ENTITY_PLAYER_NAME, player.getScoreboardName());

        return tag;
    }

    public void openCameraAttachmentsMenu(Player player, int cameraSlotIndex) {
        ItemStack stack = player.getInventory().getItem(cameraSlotIndex);
        Preconditions.checkState(stack.getItem() instanceof CameraItem,
                "Cannot open Camera Attachments UI: " + stack + " is not a CameraItem.");

        if (player instanceof ServerPlayer serverPlayer) {
            MenuProvider menuProvider = new MenuProvider() {
                @Override
                public @NotNull Component getDisplayName() {
                    return stack.get(DataComponents.CUSTOM_NAME) != null
                            ? stack.getHoverName() : Component.translatable("container.exposure.camera");
                }

                @Override
                public @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
                    return new CameraAttachmentsMenu(containerId, playerInventory, cameraSlotIndex);
                }
            };

            PlatformHelper.openMenu(serverPlayer, menuProvider, buffer -> buffer.writeInt(cameraSlotIndex));
        }
    }

    protected int getMatchingSlotInInventory(Inventory inventory, ItemStack stack) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).equals(stack)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * This method is called only server-side and then gets sent to client in a packet
     * because gameTime is different between client/server, and IDs won't match.
     */
    protected String createExposureId(Player player) {
        // Filtering just to avoid potential issues with IDs. Although it may not be necessary anymore
        String playerName = Util.sanitizeName(player.getName().getString(), ResourceLocation::validPathChar);
        return playerName + "_" + player.level().getGameTime();
    }

    public FocalRange getFocalRange(ItemStack cameraStack) {
        StoredItemStack lensStack = getAttachment(cameraStack, AttachmentType.LENS);
        if (lensStack.isEmpty()) {
            return getDefaultFocalRange();
        }

        return FocalRange.ofStack(lensStack.getForReading());
    }

    public FocalRange getDefaultFocalRange() {
        return FocalRange.getDefault();
    }

    /**
     * This method is called after we take a screenshot. Otherwise, due to the delays (flash, etc) - particles would be captured as well.
     */
    @SuppressWarnings("unused")
    public void spawnClientsideFlashEffects(@NotNull Player player, ItemStack cameraStack) {
        Preconditions.checkState(player.level().isClientSide, "This methods should only be called client-side.");
        Level level = player.level();
        Vec3 pos = player.position();
        Vec3 lookAngle = player.getLookAngle();
        pos = pos.add(0, 1, 0).add(lookAngle.multiply(0.8f, 0.8f, 0.8f));

        RandomSource r = level.getRandom();
        for (int i = 0; i < 3; i++) {
            level.addParticle(ParticleTypes.END_ROD,
                    pos.x + r.nextFloat() - 0.5f,
                    pos.y + r.nextFloat() + 0.15f,
                    pos.z + r.nextFloat() - 0.5f,
                    lookAngle.x * 0.025f + r.nextFloat() * 0.025f,
                    lookAngle.y * 0.025f + r.nextFloat() * 0.025f,
                    lookAngle.z * 0.025f + r.nextFloat() * 0.025f);
        }
    }

    // ---

//    @SuppressWarnings("unused")
//    public List<AttachmentType> getAttachmentTypes(ItemStack cameraStack) {
//        return ATTACHMENTS;
//    }
//
//    public Optional<AttachmentType> getAttachmentTypeForSlot(ItemStack cameraStack, int slot) {
//        List<AttachmentType> attachmentTypes = getAttachmentTypes(cameraStack);
//        for (AttachmentType attachmentType : attachmentTypes) {
//            if (attachmentType.slot() == slot)
//                return Optional.of(attachmentType);
//        }
//        return Optional.empty();
//    }
//
//    public Optional<ItemAndStack<FilmRollItem>> getFilm(ItemStack cameraStack) {
//        return getAttachment(cameraStack, FILM_ATTACHMENT).map(ItemAndStack::new);
//    }
//
//    public void setFilm(ItemStack cameraStack, ItemStack filmStack) {
//        setAttachment(cameraStack, FILM_ATTACHMENT, filmStack);
//    }

    public StoredItemStack getAttachment(ItemStack stack, AttachmentType type) {
        return stack.getOrDefault(type.componentType(), StoredItemStack.EMPTY);
    }

    public void setAttachment(ItemStack stack, AttachmentType type, ItemStack attachmentStack) {
        if (attachmentStack.isEmpty()) {
            stack.remove(type.componentType());
        } else {
            stack.set(type.componentType(), new StoredItemStack(attachmentStack));
        }
    }

    public boolean hasFlash(ItemStack stack) {
        return !getAttachment(stack, AttachmentType.FLASH).isEmpty();
    }

    public ShutterSpeed getShutterSpeed(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.SHUTTER_SPEED, ShutterSpeed.DEFAULT);
    }

    public void setShutterSpeed(ItemStack stack, ShutterSpeed shutterSpeed) {
        stack.set(Exposure.DataComponents.SHUTTER_SPEED, shutterSpeed);
    }

    public double getZoomPercentage(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.ZOOM, 0.0);
    }

    public void setZoomPercentage(ItemStack stack, double zoom) {
        stack.set(Exposure.DataComponents.ZOOM, Mth.clamp(zoom, 0.0, 1.0));
    }

    public CompositionGuide getCompositionGuide(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.COMPOSITION_GUIDE, CompositionGuides.NONE);
    }

    public void setCompositionGuide(ItemStack stack, CompositionGuide compositionGuide) {
        stack.set(Exposure.DataComponents.COMPOSITION_GUIDE, compositionGuide);
    }

    public FlashMode getFlashMode(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.FLASH_MODE, FlashMode.OFF);
    }

    public void setFlashMode(ItemStack stack, FlashMode flashMode) {
        stack.set(Exposure.DataComponents.FLASH_MODE, flashMode);
    }

    public boolean hasInterplanarProjectorFilter(ItemStack cameraStack) {
        StoredItemStack filter = getAttachment(cameraStack, AttachmentType.FILTER);
        return !filter.isEmpty() && filter.getItem() instanceof InterplanarProjectorItem;
    }

    protected void setOrRemoveBooleanComponent(ItemStack stack, DataComponentType<Boolean> component, boolean value) {
        if (value) {
            stack.set(component, true);
        } else {
            stack.remove(component);
        }
    }
}
