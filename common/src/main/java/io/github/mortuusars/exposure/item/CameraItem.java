package io.github.mortuusars.exposure.item;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import io.github.mortuusars.exposure.*;
import io.github.mortuusars.exposure.block.FlashBlock;
import io.github.mortuusars.exposure.camera.CameraClient;
import io.github.mortuusars.exposure.client.snapshot.capturing.action.CaptureActions;
import io.github.mortuusars.exposure.client.snapshot.palettizer.ImagePalettizer;
import io.github.mortuusars.exposure.client.snapshot.processing.Process;
import io.github.mortuusars.exposure.client.snapshot.processing.Processor;
import io.github.mortuusars.exposure.client.snapshot.*;
import io.github.mortuusars.exposure.client.snapshot.capturing.Capture;
import io.github.mortuusars.exposure.client.snapshot.capturing.action.CaptureAction;
import io.github.mortuusars.exposure.client.snapshot.saving.ImageUploader;
import io.github.mortuusars.exposure.core.*;
import io.github.mortuusars.exposure.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.core.EntitiesInFrame;
import io.github.mortuusars.exposure.core.camera.CameraAccessor;
import io.github.mortuusars.exposure.core.camera.CameraAccessors;
import io.github.mortuusars.exposure.core.camera.component.*;
import io.github.mortuusars.exposure.core.frame.FrameProperties;
import io.github.mortuusars.exposure.core.frame.Photographer;
import io.github.mortuusars.exposure.core.image.color.ColorPalette;
import io.github.mortuusars.exposure.item.component.EntityInFrame;
import io.github.mortuusars.exposure.item.component.ExposureFrame;
import io.github.mortuusars.exposure.item.component.StoredItemStack;
import io.github.mortuusars.exposure.item.part.Attachment;
import io.github.mortuusars.exposure.item.part.Setting;
import io.github.mortuusars.exposure.item.part.Shutter;
import io.github.mortuusars.exposure.menu.CameraAttachmentsMenu;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.OnFrameAddedS2CP;
import io.github.mortuusars.exposure.network.packet.client.StartExposureS2CP;
import io.github.mortuusars.exposure.network.packet.server.OpenCameraAttachmentsInCreativePacketC2SP;
import io.github.mortuusars.exposure.server.CameraInstances;
import io.github.mortuusars.exposure.sound.OnePerEntitySounds;
import io.github.mortuusars.exposure.core.ChromaChannel;
import io.github.mortuusars.exposure.util.Fov;
import io.github.mortuusars.exposure.util.LevelUtil;
import io.github.mortuusars.exposure.util.TranslatableError;
import io.github.mortuusars.exposure.util.task.Task;
import io.github.mortuusars.exposure.client.image.PalettizedImage;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
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

import java.util.*;
import java.util.function.Consumer;

public class CameraItem extends Item {
    private static final String ID_OF_LAST_SHOT = "id_of_last_shot";
    private static final String FLASH_HAS_FIRED_ON_LAST_SHOT = "flash_has_fired_on_last_shot";
    private static final String LIGHT_LEVEL_ON_LAST_SHOT = "light_level_on_last_shot";

    protected final List<ShutterSpeed> shutterSpeeds;
    protected final List<Attachment<?>> attachments;
    protected final Shutter shutter;

    public CameraItem(Properties properties) {
        super(properties);
        shutterSpeeds = ImmutableList.copyOf(defineShutterSpeeds());
        attachments = ImmutableList.copyOf(defineAttachments());

        shutter = new Shutter();
        shutter.onClosed(this::onShutterClosed);
    }

    public Shutter getShutter() {
        return shutter;
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

    protected List<Attachment<?>> defineAttachments() {
        return List.of(
                Attachment.FILM,
                Attachment.FLASH,
                Attachment.LENS,
                Attachment.FILTER
        );
    }

    public List<ShutterSpeed> getShutterSpeeds(ItemStack stack) {
        return shutterSpeeds;
    }

    public List<Attachment<?>> getAttachments(ItemStack stack) {
        return attachments;
    }

//    @Override
//    public int getUseDuration(ItemStack stack, LivingEntity entity) {
//        return 1000;
//    }

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
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY)
            return false;

        if (otherStack.isEmpty() && Config.Common.CAMERA_GUI_RIGHT_CLICK_ATTACHMENTS_SCREEN.get()) {
            if (!(slot.container instanceof Inventory)) {
                return false; // Cannot open when not in player's inventory
            }

            if (player.isCreative() && player.level().isClientSide()) {
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
            for (Attachment<?> attachment : getAttachments(stack)) {
                if (attachment.matches(otherStack)) {
                    StoredItemStack currentAttachment = attachment.get(stack);

                    if (otherStack.getCount() > 1 && !currentAttachment.isEmpty()) {
                        if (player.level().isClientSide())
                            playCameraSound(player, Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 0.9f, 1f);
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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag tooltipFlag) {
        if (Config.Client.CAMERA_SHOW_FILM_FRAMES_IN_TOOLTIP.get()) {
            Attachment.FILM.ifPresent(stack, (filmItem, filmStack) -> {
                int exposed = filmItem.getStoredFramesCount(filmStack);
                int max = filmItem.getMaxFrameCount(filmStack);
                components.add(Component.translatable("item.exposure.camera.tooltip.film_roll_frames", exposed, max));
            });
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
        return Setting.ACTIVE.getOrDefault(stack, false);
    }

    public void activate(Entity entity, ItemStack stack) {
        if (!isActive(stack)) {
            Setting.ACTIVE.set(stack, true);
            entity.gameEvent(GameEvent.EQUIP); // Sends skulk vibrations
            @Nullable Player excludedPlayer = entity instanceof Player player ? player : null;
            playCameraSound(excludedPlayer, entity, Exposure.SoundEvents.VIEWFINDER_OPEN.get(), 0.35f, 0.9f, 0.2f);

            if (!entity.level().isClientSide) {
                if (!stack.has(Exposure.DataComponents.CAMERA_ID)) {
                    UUID uuid = UUID.randomUUID();
//                    CameraInstances.addNewCamera(uuid);
                }

            }
        }
    }

    public void deactivate(Entity entity, ItemStack stack) {
        if (isActive(stack)) {
            Setting.ACTIVE.set(stack, false);
            entity.gameEvent(GameEvent.EQUIP); // Sends skulk vibrations
            @Nullable Player excludedPlayer = entity instanceof Player player ? player : null;
            playCameraSound(excludedPlayer, entity, Exposure.SoundEvents.VIEWFINDER_CLOSE.get(), 0.35f, 0.9f, 0.2f);
        }
    }

    public boolean isInSelfieMode(ItemStack stack) {
        return Setting.SELFIE.getOrDefault(stack, false);
    }

//    public ShutterState getShutterState(ItemStack stack) {
//        return stack.getOrDefault(Exposure.DataComponents.SHUTTER_STATE, ShutterState.CLOSED);
//    }
//
//    public void setShutterState(ItemStack stack, ShutterState shutterState) {
//        stack.set(Exposure.DataComponents.SHUTTER_STATE, shutterState);
//    }
//
//    public void openShutter(ServerPlayer player, Level level, ItemStack stack, ShutterSpeed shutterSpeed) {
//        setShutterState(stack, ShutterState.open(level.getGameTime(), shutterSpeed));
//
//        player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
//        playCameraSound(null, player, Exposure.SoundEvents.SHUTTER_OPEN.get(), 0.7f, 1.1f, 0.2f);
//    }
//
//    public void closeShutter(ServerPlayer player, ItemStack stack) {
//        ShutterState shutterState = getShutterState(stack);
//        long closeTick = shutterState.getCloseTick();
//        boolean flashHasFired = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe()
//                .getBoolean(FLASH_HAS_FIRED_ON_LAST_SHOT);
//
//        setShutterState(stack, ShutterState.closed());
//
//        if (player.level().getGameTime() - closeTick >= 30 /*1.5 sec*/) {
//            // Skip effects if shutter was closed long ago
//            return;
//        }
//
//        player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
//        player.getCooldowns().addCooldown(this, flashHasFired ? 10 : 2); // TODO: long cooldown if projector used
//        playCameraSound(null, player, Exposure.SoundEvents.SHUTTER_CLOSE.get(), 0.7f, 1.1f, 0.2f);
//
//        StoredItemStack filmStack = getAttachment(stack, AttachmentType.FILM);
//        if (filmStack.getItem() instanceof FilmRollItem filmRollItem) {
//            float fullness = filmRollItem.getFullness(filmStack.getForReading());
//            boolean isFull = fullness == 1f;
//
//            if (isFull)
//                OnePerEntitySounds.play(null, player, Exposure.SoundEvents.FILM_ADVANCE_LAST.get(), SoundSource.PLAYERS, 1f, 1f);
//            else {
//                OnePerEntitySounds.play(null, player, Exposure.SoundEvents.FILM_ADVANCING.get(), SoundSource.PLAYERS,
//                        1f, 0.9f + 0.1f * fullness);
//            }
//        }
//    }

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

        getShutter().tick(entity, stack);

        boolean inHand = isSelected || player.getOffhandItem().equals(stack);

        if (!inHand) {
            deactivate(player, stack);

            if (level.isClientSide() && Viewfinder.isOpen()) {
                Viewfinder.close();
            }
        }
    }

    protected void onShutterClosed(Entity entity, ItemStack stack) {
        // cooldowns:
        // default   : 2
        // flash     : 10
        // projector : 20

//        player.getCooldowns().addCooldown(this, flashHasFired ? 10 : 2);

        Attachment.FILM.ifPresent(stack, (filmItem, filmStack) -> {
            SoundEvent sound = filmItem.isFull(filmStack)
                    ? Exposure.SoundEvents.FILM_ADVANCE_LAST.get()
                    : Exposure.SoundEvents.FILM_ADVANCE.get();

            float fullness = filmItem.getFullness(filmStack);
            OnePerEntitySounds.play(null, entity, sound, SoundSource.PLAYERS, 1f, 0.9f + 0.1f * fullness);
        });
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
        if (player.getCooldowns().isOnCooldown(this))
            return InteractionResult.FAIL;

        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty() || stack.getItem() != this)
            return InteractionResult.PASS;

        boolean active = isActive(stack);

        if (!active && player.isSecondaryUseActive()) {
            if (getShutter().isOpen(stack)) {
                player.displayClientMessage(Component.translatable("item.exposure.camera.camera_attachments.fail.shutter_open")
                        .withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }

            int cameraSlot = getMatchingSlotInInventory(player.getInventory(), stack);
            if (cameraSlot < 0)
                return InteractionResult.FAIL;

            openCameraAttachmentsMenu(player, cameraSlot);
            return InteractionResult.SUCCESS;
        }

        if (!active) {
            activate(player, stack);
            player.getCooldowns().addCooldown(this, 4);

            if (player.level().isClientSide) {
                //TODO: Pass accessor as method argument, remove all dependency on hand so we can use it not only in hand
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

        StoredItemStack filmStack = Attachment.FILM.get(stack);

        if (filmStack.isEmpty() || !(filmStack.getItem() instanceof FilmRollItem filmItem))
            return InteractionResult.FAIL;

        boolean exposingFilm = filmItem.canAddFrame(filmStack.getForReading());

        if (!exposingFilm)
            return InteractionResult.FAIL;

        if (getShutter().isOpen(stack))
            return InteractionResult.FAIL;

        int lightLevel = LevelUtil.getLightLevelAt(player.level(), player.blockPosition());
        boolean shouldFlashFire = shouldFlashFire(player, stack, lightLevel);
        ShutterSpeed shutterSpeed = Setting.SHUTTER_SPEED.getOrDefault(stack, ShutterSpeed.DEFAULT);

//        if (PlatformHelper.fireShutterOpeningEvent(player, stack, lightLevel, shouldFlashFire))
//            return InteractionResult.FAIL; // Canceled

        boolean flashHasFired = shouldFlashFire && tryUseFlash(player, stack);

        getShutter().open(player, stack, shutterSpeed);

        if (shutterSpeed.shouldCauseTickingSound()) {
            OnePerEntitySounds.playShutterTickingSoundForAllPlayers(CameraAccessors.ofHand(hand), player,
                    1f, 1f, shutterSpeed.getDurationTicks());
        }

        ExposureIdentifier exposureIdentifier = ExposureIdentifier.createId(player);

        //TODO: create persistent serverside camera object and reference it from id stored in item
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putString(ID_OF_LAST_SHOT, exposureIdentifier.getId());
            tag.putBoolean(FLASH_HAS_FIRED_ON_LAST_SHOT, flashHasFired);
            tag.putInt(LIGHT_LEVEL_ON_LAST_SHOT, lightLevel);
        });

        ExposureServer.awaitExposure(exposureIdentifier, filmItem.getType(), player.getScoreboardName());

        CameraAccessor<?> cameraAccessor = CameraAccessors.ofHand(hand);
        Packets.sendToClient(new StartExposureS2CP(exposureIdentifier, cameraAccessor, flashHasFired, lightLevel), serverPlayer);

        return InteractionResult.CONSUME; // Consume to not play swing animation
    }

    private @NotNull Consumer<TranslatableError> printCasualErrorInChat(Player player) {
        return err -> Minecraft.getInstance().execute(() ->
                player.displayClientMessage(err.casual().withStyle(ChatFormatting.RED), false));
    }

    public ExposureFrameClientData getClientSideFrameData(Player player, ItemStack stack) {
        //TODO: figure out how to know when image was loaded
        boolean projectingFile = Attachment.FILTER.map(stack, (filterItem, filterStack) ->
                filterItem instanceof InterplanarProjectorItem).orElse(false);

        List<UUID> entitiesInFrame;

        if (projectingFile) {
            entitiesInFrame = Collections.emptyList();
        } else {
            entitiesInFrame = EntitiesInFrame.get(player, Viewfinder.getCurrentFov(), Exposure.MAX_ENTITIES_IN_FRAME, isInSelfieMode(stack))
                    .stream()
                    .map(Entity::getUUID)
                    .toList();
        }

        CompoundTag extraData = new CompoundTag();
        //TODO: get additional encodedValue event

        return new ExposureFrameClientData(projectingFile, entitiesInFrame, extraData);
    }

    public void exposeFrameClientside(Player player, ItemStack stack, ExposureIdentifier identifier, boolean flashHasFired) {
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

        startCapture(player, stack, identifier, flashHasFired);
    }

    protected void startCapture(Player player, ItemStack stack, ExposureIdentifier identifier, boolean flashHasFired) {
        StoredItemStack filmStack = Attachment.FILM.get(stack);
        if (filmStack.isEmpty() || !(filmStack.getItem() instanceof FilmRollItem filmItem)) {
            throw new IllegalStateException("Film attachment should be present at the time of capture.");
        }
        StoredItemStack filterStack = Attachment.FILTER.get(stack);

        int frameSize = filmItem.getFrameSize(filmStack.getForReading());
        float brightnessStops = Setting.SHUTTER_SPEED.getOrDefault(stack, ShutterSpeed.DEFAULT).getStopsDifference(ShutterSpeed.DEFAULT);

        Processor colorProcessor = chooseColorProcessor(stack, filmStack.getForReading(), filterStack.getForReading());

        Task<PalettizedImage> captureTask = Capture.of(Capture.screenshot(),
                        CaptureActions.hideGui(),
                        CaptureActions.forceRegularOrSelfieCamera(),
                        CaptureActions.disablePostEffect(),
                        CaptureActions.modifyGamma(brightnessStops),
                        CaptureAction.optional(flashHasFired, () -> CaptureActions.flash(player)))
                .handleErrorAndGetResult(printCasualErrorInChat(player))
                .thenAsync(Process.with(
                        Processor.Crop.SQUARE,
                        Processor.Crop.factor(Exposure.CROP_FACTOR),
                        Processor.Resize.to(frameSize),
                        Processor.brightness(brightnessStops),
                        colorProcessor))
                .thenAsync(image -> {
                    PalettizedImage palettizedImage = ImagePalettizer.DITHERED_MAP_COLORS.palettize(image, ColorPalette.MAP_COLORS);
                    image.close();
                    return palettizedImage;
                });

        if (filterStack.getItem() instanceof InterplanarProjectorItem projector && projector.isAllowed()) {
            String filePath = projector.getFilepath(filterStack.getForReading()).orElse("");
            boolean dither = projector.getMode(filterStack.getForReading()) == InterplanarProjectorMode.DITHERED;

            captureTask = captureTask.overridenBy(Capture.of(Capture.file(filePath))
                    .handleErrorAndGetResult(error -> {
                        Minecraft.getInstance().execute(() -> onProjectingFailed(player));
                        printCasualErrorInChat(player).accept(error);
                    })
                    .then(image -> {
                        Minecraft.getInstance().execute(() -> onProjectingSuccess(player));
                        return image;
                    })
                    .thenAsync(Process.with(
                            Processor.Crop.SQUARE,
                            Processor.Resize.to(frameSize),
                            Processor.brightness(brightnessStops),
                            colorProcessor))
                    .thenAsync(image -> {
                        PalettizedImage palettizedImage = (dither
                                ? ImagePalettizer.DITHERED_MAP_COLORS
                                : ImagePalettizer.NEAREST_MAP_COLORS).palettize(image, ColorPalette.MAP_COLORS);
                        image.close();
                        return palettizedImage;
                    }));
        }

        SnapShot.enqueue(captureTask
                .acceptAsync(new ImageUploader(identifier)::upload)
                .onError(printCasualErrorInChat(player)));
    }

    protected void onProjectingSuccess(Player player) {
        player.level().playSound(player, player, Exposure.SoundEvents.INTERPLANAR_PROJECT.get(),
                SoundSource.PLAYERS, 0.8f, 1.1f);
        for (int i = 0; i < 32; ++i) {
            player.level().addParticle(ParticleTypes.PORTAL, player.getX(), player.getY() + player.getRandom().nextDouble() * 2.0, player.getZ(), player.getRandom().nextGaussian(), 0.0, player.getRandom().nextGaussian());
        }
    }

    protected void onProjectingFailed(Player player) {
        player.level().playSound(player, player, Exposure.SoundEvents.INTERPLANAR_PROJECT.get(),
                SoundSource.PLAYERS, 0.8f, 0.6f);
        for (int i = 0; i < 32; ++i) {
            player.level().addParticle(ParticleTypes.PORTAL, player.getX(), player.getY() + player.getRandom().nextDouble() * 2.0, player.getZ(), player.getRandom().nextGaussian(), 0.0, player.getRandom().nextGaussian());
        }
    }

    protected Processor chooseColorProcessor(ItemStack cameraStack, ItemStack filmStack, ItemStack filterStack) {
        ExposureType type = filmStack.getItem() instanceof FilmRollItem film ? film.getType() : ExposureType.COLOR;

        if (type == ExposureType.COLOR) {
            return Processor.EMPTY;
        }

        return ChromaChannel.fromFilterStack(filterStack)
                .map(Processor::singleChannelBlackAndWhite)
                .orElse(Processor.blackAndWhite());
    }

    public ExposureFrame createExposureFrame(ServerPlayer player, ItemStack stack, ExposureFrameClientData dataFromClient) {
        CompoundTag cameraCustomData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        String id = cameraCustomData.getString(ID_OF_LAST_SHOT);

        if (StringUtil.isBlank(id)) {
            Exposure.LOGGER.error("Cannot create an exposure frame: '{}' is missing or empty in Camera's custom encodedValue.", ID_OF_LAST_SHOT);
            return ExposureFrame.EMPTY;
        }

        ExposureType type = Attachment.FILM.map(stack, (filmItem, filmStack) -> filmItem.getType()).orElse(ExposureType.COLOR);

        ExposureFrameTag tag = new ExposureFrameTag();

        if (cameraCustomData.getBoolean(FLASH_HAS_FIRED_ON_LAST_SHOT)) {
            tag.putBoolean(ExposureFrameTag.FLASH, true);
        }

        int lightLevel = cameraCustomData.getInt(LIGHT_LEVEL_ON_LAST_SHOT);
        if (lightLevel >= 0) {
            tag.putInt(ExposureFrameTag.LIGHT_LEVEL, lightLevel);
        }

        // Chromatic channel
        Attachment.FILTER.ifPresent(stack, filterStack -> ChromaChannel.fromFilterStack(filterStack)
                .ifPresent(channel -> tag.putString(ExposureFrameTag.CHROMATIC_CHANNEL, channel.getSerializedName())));

        // Do not forget to add encodedValue from client:
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
//                CompoundTag entityInfoTag = createEntityInFrameTag(entity, player, stack);
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
        List<Entity> capturedEntities = crossCheckCapturedEntities(player, stack, capturedEntitiesOnClient);

        List<EntityInFrame> entitiesInFrame = new ArrayList<>();

        //TODO: modifyFrameData event
//        PlatformHelper.fireModifyFrameDataEvent(player, stack, frame, entities);
        //TODO: modifyEntityInFrameData event


        return new ExposureFrame(ExposureIdentifier.id(id), type, new Photographer(player), entitiesInFrame, CustomData.of(tag));
    }

    /**
     * Returns an intersection of entities on both sides, i.e. if entity is captured on client but not on server - discard it.
     */
    public List<Entity> crossCheckCapturedEntities(ServerPlayer player, ItemStack stack, List<Entity> entitiesOnClient) {
        if (entitiesOnClient.isEmpty()) {
            return entitiesOnClient;
        }

        FocalRange focalRange = getFocalRange(stack);
        double zoom = Setting.ZOOM.getOrDefault(stack, 0.0);
        double fov = Mth.map(zoom, 0, 1, Fov.focalLengthToFov(focalRange.min()), Fov.focalLengthToFov(focalRange.max()));

        List<Entity> entitiesOnServer = EntitiesInFrame.get(player, fov, Exposure.MAX_ENTITIES_IN_FRAME, isInSelfieMode(stack));

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

        ExposureServer.exposureFrameHistory().add(player, exposureFrame);

        onFrameAdded(player, cameraStack, exposureFrame);
//        PlatformHelper.fireFrameAddedEvent(player, cameraStack, exposureFrame);
        Packets.sendToClient(new OnFrameAddedS2CP(exposureFrame), player);
    }

    public void onFrameAdded(ServerPlayer player, ItemStack stack, ExposureFrame frame) {
        if (!frame.isFromFile()) return;

        Attachment.FILTER.ifPresent(stack, (filterItem, filterStack) -> {
            if (filterItem instanceof InterplanarProjectorItem interplanarProjector) {
                // Only playing for other players, to photographer sound will play after capture, and it'll depend on success
                player.level().playSound(player, player, Exposure.SoundEvents.INTERPLANAR_PROJECT.get(),
                        SoundSource.PLAYERS, 0.8f, 1f);

                if (interplanarProjector.isConsumable(filterStack)) {
                    ItemStack filterCopy = filterStack.copy();
                    filterCopy.shrink(1);
                    Attachment.FILTER.set(stack, filterCopy);
                }
            }
        });
    }

    public void addFrameToFilm(ItemStack stack, ExposureFrame frame) {
        Attachment.FILM.ifPresentOrElse(stack, (filmItem, filmStack) -> {
            filmStack = filmStack.copy();
            filmItem.addFrame(filmStack, frame);
            Attachment.FILM.set(stack, filmStack);
        }, () -> Exposure.LOGGER.error("Cannot add frame: no film attachment is present."));
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

    public FocalRange getFocalRange(ItemStack stack) {
        return Attachment.LENS.mapOrElse(stack, FocalRange::ofStack, this::getDefaultFocalRange);
    }

    public FocalRange getDefaultFocalRange() {
        return FocalRange.getDefault();
    }


//    public StoredItemStack getAttachment(ItemStack stack, Attachment type) {
//        return stack.getOrDefault(type.component(), StoredItemStack.EMPTY);
//    }
//
//    public void setAttachment(ItemStack stack, Attachment type, ItemStack attachmentStack) {
//        if (attachmentStack.isEmpty()) {
//            stack.remove(type.component());
//        } else {
//            stack.set(type.component(), new StoredItemStack(attachmentStack));
//        }
//    }
//
//    public ShutterSpeed getShutterSpeed(ItemStack stack) {
//        return stack.getOrDefault(Exposure.DataComponents.SHUTTER_SPEED, ShutterSpeed.DEFAULT);
//    }
//
//    public void setShutterSpeed(ItemStack stack, ShutterSpeed shutterSpeed) {
//        stack.set(Exposure.DataComponents.SHUTTER_SPEED, shutterSpeed);
//    }
//
//    public double getZoomPercentage(ItemStack stack) {
//        return stack.getOrDefault(Exposure.DataComponents.ZOOM, 0.0);
//    }
//
//    public void setZoomPercentage(ItemStack stack, double zoom) {
//        stack.set(Exposure.DataComponents.ZOOM, Mth.clamp(zoom, 0.0, 1.0));
//    }
//
//    public CompositionGuide getCompositionGuide(ItemStack stack) {
//        return stack.getOrDefault(Exposure.DataComponents.COMPOSITION_GUIDE, CompositionGuides.NONE);
//    }
//
//    public void setCompositionGuide(ItemStack stack, CompositionGuide compositionGuide) {
//        stack.set(Exposure.DataComponents.COMPOSITION_GUIDE, compositionGuide);
//    }
//
//    public FlashMode getFlashMode(ItemStack stack) {
//        return stack.getOrDefault(Exposure.DataComponents.FLASH_MODE, FlashMode.OFF);
//    }
//
//    public void setFlashMode(ItemStack stack, FlashMode flashMode) {
//        stack.set(Exposure.DataComponents.FLASH_MODE, flashMode);
//    }
//
//    public boolean hasInterplanarProjectorFilter(ItemStack cameraStack) {
//        StoredItemStack filter = getAttachment(cameraStack, Attachment.FILTER);
//        return !filter.isEmpty() && filter.getItem() instanceof InterplanarProjectorItem;
//    }
}
