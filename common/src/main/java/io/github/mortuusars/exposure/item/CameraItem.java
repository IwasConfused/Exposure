package io.github.mortuusars.exposure.item;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.block.FlashBlock;
import io.github.mortuusars.exposure.camera.viewfinder.OldViewfinder;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.core.*;
import io.github.mortuusars.exposure.core.camera.*;
import io.github.mortuusars.exposure.core.camera.component.FlashMode;
import io.github.mortuusars.exposure.core.camera.component.FocalRange;
import io.github.mortuusars.exposure.core.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.core.frame.CaptureData;
import io.github.mortuusars.exposure.core.frame.FileProjectingInfo;
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
import io.github.mortuusars.exposure.network.packet.client.StartCaptureS2CP;
import io.github.mortuusars.exposure.network.packet.common.DeactivateActiveCameraCommonPacket;
import io.github.mortuusars.exposure.network.packet.server.OpenCameraAttachmentsInCreativePacketC2SP;
import io.github.mortuusars.exposure.server.CameraInstance;
import io.github.mortuusars.exposure.server.CameraInstances;
import io.github.mortuusars.exposure.sound.OnePerEntitySounds;
import io.github.mortuusars.exposure.util.*;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
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
import net.minecraft.util.Mth;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.entity.EntityTypeTest;
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
    protected final Shutter shutter;
    protected final List<Attachment<?>> attachments;

    public CameraItem(Shutter shutter, Properties properties) {
        super(properties);
        this.shutter = shutter;
        this.attachments = List.of(Attachment.FILM, Attachment.FLASH, Attachment.LENS, Attachment.FILTER);
        //TODO: shutter on open on close
//        shutter.onClosed((entity, stack) -> {});
    }

    // --

    public Shutter getShutter() {
        return shutter;
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

    public FocalRange getDefaultFocalRange() {
        return FocalRange.getDefault();
    }

    public FocalRange getFocalRange(ItemStack stack) {
        return Attachment.LENS.mapOrElse(stack, FocalRange::ofStack, this::getDefaultFocalRange);
    }

    // --

    public UUID getOrCreateID(ItemStack stack) {
        if (!stack.has(Exposure.DataComponents.CAMERA_ID)) {
            stack.set(Exposure.DataComponents.CAMERA_ID, UUID.randomUUID());
        }
        return stack.get(Exposure.DataComponents.CAMERA_ID);
    }

    public boolean isInSelfieMode(ItemStack stack) {
        return Setting.SELFIE.getOrDefault(stack, false);
    }

    public void setActive(ItemStack stack, boolean active) {
        stack.set(Exposure.DataComponents.CAMERA_VIEWFINDER_OPEN, active);
    }

    public boolean isActive(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.CAMERA_VIEWFINDER_OPEN, false);
    }

    public @NotNull InteractionResultHolder<ItemStack> activate(LivingEntity entity, ItemStack stack, @NotNull InteractionHand hand) {
        setActive(stack, true);

        if (entity instanceof ActiveCameraHolder cameraHolder) {
            cameraHolder.setActiveExposureCamera(new CameraInHand(entity, hand));
        }

        entity.gameEvent(GameEvent.EQUIP); // Sends skulk vibrations

        @Nullable Player excludedPlayer = entity instanceof Player player ? player : null;
        playSound(excludedPlayer, entity, getViewfinderOpenSound(), 0.35f, 0.9f, 0.2f);

        if (entity.level().isClientSide) {
            Minecrft.releaseUseButton(); // Releasing use key to not take a shot immediately, if right click is still held.
        }

        return InteractionResultHolder.consume(stack);
    }

    public @NotNull InteractionResultHolder<ItemStack> deactivate(LivingEntity entity, ItemStack stack) {
        setActive(stack, false);
        if (entity instanceof ActiveCameraHolder cameraHolder) {
            cameraHolder.removeActiveExposureCamera();
        }
        @Nullable Player excludedPlayer = entity instanceof Player player ? player : null;
        playSound(excludedPlayer, entity, getViewfinderCloseSound(), 0.35f, 0.9f, 0.2f);
        entity.gameEvent(GameEvent.EQUIP); // Sends skulk vibrations
        return InteractionResultHolder.consume(stack);
    }

    // --

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY) return false;

        if (otherStack.isEmpty() && Config.Common.CAMERA_GUI_RIGHT_CLICK_ATTACHMENTS_SCREEN.get()) {
            if (!(slot.container instanceof Inventory)) {
                return false; // Cannot open when not in player's inventory
            }

            if (player.isCreative() && player.level().isClientSide()) {
                Packets.sendToServer(new OpenCameraAttachmentsInCreativePacketC2SP(slot.getContainerSlot()));
                return true;
            }

            openCameraAttachments(player, stack);
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
        if (!(entity instanceof Player player)) return; // It should always be player. Not casting just to be safe.

        getShutter().tick(player, stack);

        if (player instanceof ServerPlayer serverPlayer) {
            CameraInstances.ifPresent(stack, instance -> instance.tick(player, stack));

            boolean isHolding = isSelected || slotId == Inventory.SLOT_OFFHAND;
            if (isActive(stack) && (!isHolding || !player.activeExposureCameraMatches(stack))) {
                deactivate(player, stack);
                Packets.sendToClient(DeactivateActiveCameraCommonPacket.INSTANCE, serverPlayer);
            }
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (hand == InteractionHand.MAIN_HAND
                && player.getOffhandItem().getItem() instanceof OldCameraItem offhandCameraItem
                && offhandCameraItem.isActive(player.getOffhandItem())) {
            return InteractionResultHolder.pass(stack);
        }

        if (!isActive(stack)) {
            return player.isSecondaryUseActive() ? openCameraAttachments(player, stack) : activate(player, stack, hand);
        }

        return release(level, player, hand, stack);
    }

    public @NotNull InteractionResultHolder<ItemStack> release(Level level, Player player, InteractionHand hand, ItemStack stack) {
        playSound(player, player, getReleaseButtonSound(), 0.3f, 1f, 0.1f);

        if (getShutter().isOpen(stack) || Attachment.FILM.isEmpty(stack)) {
            return InteractionResultHolder.fail(stack);
        }

        ItemAndStack<FilmRollItem> film = Attachment.FILM.get(stack).getItemAndStackCopy();

        if (!film.getItem().canAddFrame(film.getItemStack()))
            return InteractionResultHolder.fail(stack);

        if (player instanceof ServerPlayer serverPlayer) {
            int lightLevel = LevelUtil.getLightLevelAt(level, player.blockPosition());
            boolean shouldFlashFire = shouldFlashFire(stack, lightLevel);
            ShutterSpeed shutterSpeed = Setting.SHUTTER_SPEED.getOrDefault(stack, ShutterSpeed.DEFAULT);

            boolean flashHasFired = shouldFlashFire && tryUseFlash(player, stack);

            getShutter().open(player, stack, shutterSpeed);

            if (shutterSpeed.shouldCauseTickingSound()) {
                OnePerEntitySounds.playShutterTickingSoundForAllPlayers(CameraAccessors.ofHand(hand), player,
                        1f, 1f, shutterSpeed.getDurationTicks());
            }

            ExposureIdentifier exposureIdentifier = ExposureIdentifier.createId(player);

            CaptureData captureData = new CaptureData(exposureIdentifier,
                    player.getUUID(),
                    getOrCreateID(stack),
                    Setting.SHUTTER_SPEED.get(stack),
                    Optional.empty(),
                    film.getItem().getType(),
                    film.getItem().getFrameSize(film.getItemStack()),
                    Exposure.CROP_FACTOR, //TODO: this.getCropFactor()
                    ColorPalette.MAP_COLORS,
                    flashHasFired,
                    lightLevel,
                    getFileLoadingData(stack),
                    getChromaChannel(stack),
                    new CompoundTag());

            //TODO: use Photographer instead of creator string
            ExposureServer.awaitExposure(exposureIdentifier, captureData.filmType(), player.getScoreboardName());
            CameraInstances.createOrUpdate(getOrCreateID(stack), instance -> instance.setCurrentCaptureData(level, captureData));
            Packets.sendToClient(new StartCaptureS2CP(captureData), serverPlayer);
        }

        return InteractionResultHolder.consume(stack);
    }

    protected Optional<ChromaChannel> getChromaChannel(ItemStack stack) {
        return Attachment.FILTER.map(stack, ChromaChannel::fromFilterStack).orElse(Optional.empty());
    }

    protected Optional<FileProjectingInfo> getFileLoadingData(ItemStack stack) {
        return Attachment.FILTER.map(stack, (filterItem, filterStack) ->
                        filterItem instanceof InterplanarProjectorItem projectorItem
                                ? projectorItem.getFileLoadingData(filterStack)
                                : Optional.<FileProjectingInfo>empty())
                .orElse(Optional.empty());
    }

    public ExposureFrameClientData getClientSideFrameData(Player player, ItemStack stack) {
        List<UUID> entitiesInFrame = EntitiesInFrame.get(player, OldViewfinder.getCurrentFov(), Exposure.MAX_ENTITIES_IN_FRAME, isInSelfieMode(stack))
                .stream()
                .map(Entity::getUUID)
                .toList();

        CompoundTag extraData = new CompoundTag();
        //TODO: get additional data event

        return new ExposureFrameClientData(entitiesInFrame, extraData);
    }

    public InteractionResultHolder<ItemStack> openCameraAttachments(@NotNull Player player, ItemStack stack) {
        Preconditions.checkArgument(stack.getItem() instanceof CameraItem, "%s is not a CameraItem.", stack);

        if (getShutter().isOpen(stack)) {
            player.displayClientMessage(Component.translatable("item.exposure.camera.camera_attachments.fail.shutter_open")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

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

        return InteractionResultHolder.success(stack);
    }

    protected boolean shouldFlashFire(ItemStack stack, int lightLevel) {
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

    public void handleProjectionResult(LivingEntity cameraHolder, ItemStack stack, CameraInstance.ProjectionResult projectionResult) {
        StoredItemStack filter = Attachment.FILTER.get(stack);
        if (filter.isEmpty()) return;
        if (!(filter.getItem() instanceof InterplanarProjectorItem interplanarProjector)) return;
        if (!interplanarProjector.isConsumable(filter.getForReading())) return;

        if (projectionResult == CameraInstance.ProjectionResult.FAILED) {
            //TODO: BSOD
        } else {
            ItemStack filterStack = filter.getCopy();
            filterStack.shrink(1);
            Attachment.FILTER.set(stack, filterStack);
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

    public void playSound(@Nullable Player excludedPlayer, @NotNull Entity origin, SoundEvent sound,
                          float volume, float pitch, float pitchVariety) {
        if (pitchVariety > 0f)
            pitch = pitch - (pitchVariety / 2f) + (origin.getRandom().nextFloat() * pitchVariety);
        origin.level().playSound(excludedPlayer, origin, sound, SoundSource.PLAYERS, volume, pitch);
    }


    public ExposureFrame createExposureFrame(ServerLevel level, Entity cameraHolder, ItemStack stack, ExposureFrameClientData dataFromClient) {
        @Nullable CameraInstance cameraInstance = CameraInstances.get(getOrCreateID(stack));
        if (cameraInstance == null) {
            Exposure.LOGGER.error("Cannot create an exposure frame: Camera Instance with id '{}' does not exists.", getOrCreateID(stack));
            return ExposureFrame.EMPTY;
        }

        Optional<CaptureData> currentCaptureData = cameraInstance.getCurrentCaptureData();
        if (currentCaptureData.isEmpty()) {
            Exposure.LOGGER.error("Cannot create an exposure frame: Camera Instance does not have capture data.");
            return ExposureFrame.EMPTY;
        }
        CaptureData captureData = currentCaptureData.get();

        ExposureFrameTag tag = new ExposureFrameTag();

        if (captureData.flashHasFired()) {
            tag.putBoolean(ExposureFrameTag.FLASH, true);
        }

        tag.putInt(ExposureFrameTag.LIGHT_LEVEL, captureData.lightLevel());

        captureData.chromaChannel().ifPresent(channel ->
                tag.putString(ExposureFrameTag.CHROMATIC_CHANNEL, channel.getSerializedName()));

        captureData.fileProjectingInfo().ifPresent(fileProjectingInfo -> tag.putBoolean(ExposureFrameTag.FROM_FILE, true));

        // Position
        ListTag pos = new ListTag();
        pos.addAll(List.of(
                DoubleTag.valueOf(cameraHolder.position().x()),
                DoubleTag.valueOf(cameraHolder.position().y()),
                DoubleTag.valueOf(cameraHolder.position().z())));
        tag.put(ExposureFrameTag.POSITION, pos);
        tag.put(ExposureFrameTag.PITCH, FloatTag.valueOf(cameraHolder.getXRot()));
        tag.put(ExposureFrameTag.YAW, FloatTag.valueOf(cameraHolder.getYRot()));

        // Situation
        tag.put(ExposureFrameTag.TIMESTAMP, LongTag.valueOf(UnixTimestamp.Seconds.now()));
        tag.putInt(ExposureFrameTag.DAY_TIME, (int) level.getDayTime());
        tag.putString(ExposureFrameTag.DIMENSION, level.dimension().location().toString());
        BlockPos blockPos = cameraHolder.blockPosition();
        level.getBiome(blockPos).unwrapKey().map(ResourceKey::location)
                .ifPresent(biome -> tag.putString(ExposureFrameTag.BIOME, biome.toString()));
        int surfaceHeight = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, cameraHolder.getBlockX(), cameraHolder.getBlockZ());
        level.updateSkyBrightness();
        int skyLight = level.getBrightness(LightLayer.SKY, blockPos);
        if (cameraHolder.isUnderWater())
            tag.putBoolean(ExposureFrameTag.UNDERWATER, true);
        if (cameraHolder.getBlockY() < Math.min(level.getSeaLevel(), surfaceHeight) && skyLight == 0)
            tag.putBoolean(ExposureFrameTag.IN_CAVE, true);
        else if (!cameraHolder.isUnderWater()) {
            Biome.Precipitation precipitation = level.getBiome(blockPos).value().getPrecipitationAt(blockPos);
            if (level.isThundering() && precipitation != Biome.Precipitation.NONE)
                tag.putString(ExposureFrameTag.WEATHER, precipitation == Biome.Precipitation.SNOW ? "Snowstorm" : "Thunder");
            else if (level.isRaining() && precipitation != Biome.Precipitation.NONE)
                tag.putString(ExposureFrameTag.WEATHER, precipitation == Biome.Precipitation.SNOW ? "Snow" : "Rain");
            else
                tag.putString(ExposureFrameTag.WEATHER, "Clear");
        }

        addStructuresInfo(level, blockPos, tag);

        List<EntityInFrame> entitiesInFrame;

        if (captureData.fileProjectingInfo().isPresent()) {
            entitiesInFrame = Collections.emptyList();
        } else {
            if (cameraHolder instanceof ServerPlayer player) {
                boolean lookingAtAngryEnderMan = !level.getEntities(EntityTypeTest.forClass(EnderMan.class),
                                enderMan -> player.equals(enderMan.getTarget()) && enderMan.isLookingAtMe(player)).isEmpty();

                if (lookingAtAngryEnderMan) {
                    // I wanted to implement this in a predicate,
                    // but it's tricky because EntitySubPredicates do not get the player in their 'match' method.
                    // So it's just easier to hardcode it like this.
                    Exposure.CriteriaTriggers.PHOTOGRAPH_ENDERMAN_EYES.get().trigger(player);
                }
            }

            List<Entity> capturedEntities = intersectCapturedEntities(cameraHolder, stack, dataFromClient.getCapturedEntities(level));
            entitiesInFrame = capturedEntities.stream()
                    .map(entity -> EntityInFrame.of(cameraHolder, entity, customDataTag -> {
                        //TODO: modifyEntityInFrameData event
                    }))
                    .toList();
        }

        tag.merge(dataFromClient.extraData());

        //TODO: modifyFrameData event
        //PlatformHelper.fireModifyFrameDataEvent(player, stack, frame, entities);

        return new ExposureFrame(captureData.identifier(), captureData.filmType(), new Photographer(cameraHolder), entitiesInFrame, CustomData.of(tag));
    }

    /**
     * Returns an intersection of entities on both sides, i.e. if entity is captured on client but not on server - discard it.
     */
    public List<Entity> intersectCapturedEntities(Entity photographer, ItemStack stack, List<Entity> entitiesOnClient) {
        if (entitiesOnClient.isEmpty()) {
            return entitiesOnClient;
        }

        FocalRange focalRange = getFocalRange(stack);
        double zoom = Setting.ZOOM.getOrDefault(stack, 0.0);
        double fov = Mth.map(zoom, 0, 1, Fov.focalLengthToFov(focalRange.min()), Fov.focalLengthToFov(focalRange.max()));

        List<Entity> entitiesOnServer = EntitiesInFrame.get(photographer, fov, Exposure.MAX_ENTITIES_IN_FRAME, isInSelfieMode(stack));

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
}
