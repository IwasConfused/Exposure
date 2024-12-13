package io.github.mortuusars.exposure.util;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.core.camera.NewAccessor;
import io.github.mortuusars.exposure.item.part.Attachment;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.item.FilmRollItem;
import io.github.mortuusars.exposure.mixin.MoreDebugScreenRenderLinesInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MoreDebug {
    public static boolean isOnMoreDebugPage;

    public static boolean onKeyPress(int key, int scanCode) {
        boolean shift = Screen.hasShiftDown();
        if (key == InputConstants.KEY_UP) up(shift);
        if (key == InputConstants.KEY_DOWN) down(shift);
        if (key == InputConstants.KEY_LEFT) isOnMoreDebugPage = false;
        if (key == InputConstants.KEY_RIGHT) isOnMoreDebugPage = true;
        return false;
    }

    public static boolean onKeyRelease(int key, int scanCode) {
        return false;
    }

    static int count;

    private static void up(boolean shift) {
        count++;
    }

    private static void down(boolean shift) {
        count--;
    }

    public static void render(GuiGraphics guiGraphics) {
        List<String> leftLines = collectLeftLines();
        ((MoreDebugScreenRenderLinesInvoker) Minecraft.getInstance().getDebugOverlay()).drawLines(guiGraphics, leftLines, true);
        List<String> rightLines = collectRightLines();
        ((MoreDebugScreenRenderLinesInvoker) Minecraft.getInstance().getDebugOverlay()).drawLines(guiGraphics, rightLines, false);
    }

    private static List<String> collectLeftLines() {
        List<String> lines = new ArrayList<>();

        lines.add(NewAccessor.MAIN_HAND.ifInHandOfType(Minecraft.getInstance().player, CameraItem.class)
                .map(inHand -> inHand.map((item, stack, hand) -> item.getAttachment(stack, Attachment.FILM)
                        .mapIf(FilmRollItem.class, (fItem, fStack) ->
                                fItem.getStoredFramesCount(fStack) + "/" + fItem.getMaxFrameCount(fStack))
                        .orElse("No Film in " + hand)))
                .orElse("No Camera"));

        return lines;
    }

    private static List<String> collectRightLines() {
        List<String> lines = new ArrayList<>();

        getItemNbtString(getItemInHand()).ifPresent(str -> {
            lines.add("Item in hand:");

            String text = str.trim().replaceAll(" +", " ").replace("\" :", "\":");
            List<String> strings = splitString(text, 58);
            List<String> visibleStrings = strings.stream().limit(14).toList();

            lines.addAll(visibleStrings);

            if (strings.size() > visibleStrings.size()) {
                lines.add("...");
            }
        });

        return lines;
    }

    private static ItemStack getItemInHand() {
        @Nullable LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return ItemStack.EMPTY;

        ItemStack mainHandItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        return mainHandItem.isEmpty() ? player.getItemInHand(InteractionHand.OFF_HAND) : mainHandItem;
    }

    private static Optional<String> getItemNbtString(ItemStack itemStack) {
        if (itemStack.isEmpty()) return Optional.empty();
        if (Minecraft.getInstance().level == null) return Optional.empty();

        Tag tag = itemStack.save(Minecraft.getInstance().level.registryAccess());
        return Optional.of(NbtUtils.prettyPrint(tag).replace("\n", ""));
    }

    public static List<String> splitString(String text, int size) {
        // Give the list the right capacity to start with. You could use an array
        // instead if you wanted.
        List<String> ret = new ArrayList<String>((text.length() + size - 1) / size);

        for (int start = 0; start < text.length(); start += size) {
            ret.add(text.substring(start, Math.min(text.length(), start + size)));
        }
        return ret;
    }
}
