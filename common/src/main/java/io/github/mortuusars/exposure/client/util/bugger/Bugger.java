package io.github.mortuusars.exposure.client.util.bugger;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.JsonOps;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.mixin.client.BuggerScreenRenderLinesInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Bugger {
    public static int page = -1;

    private static int zoom;
    private static int scroll;

    public static boolean onKeyPress(int key, int scanCode) {
        if (key == InputConstants.KEY_UP) up();
        if (key == InputConstants.KEY_DOWN) down();
        if (key == InputConstants.KEY_INSERT) zoom = 0;
        if (key == InputConstants.KEY_HOME) scroll = 0;
        if (key == InputConstants.KEY_LEFT) page = Mth.clamp(page - 1, -1, 1);
        if (key == InputConstants.KEY_RIGHT) page = Mth.clamp(page + 1, -1, 1);
        return false;
    }

    public static boolean onKeyRepeat(int key, int scanCode) {
        if (key == InputConstants.KEY_UP) up();
        if (key == InputConstants.KEY_DOWN) down();
        if (key == InputConstants.KEY_LEFT) page = Mth.clamp(page - 1, -1, 1);
        if (key == InputConstants.KEY_RIGHT) page = Mth.clamp(page + 1, -1, 1);
        return false;
    }

    public static boolean onKeyRelease(int key, int scanCode) {
        return false;
    }

    private static void up() {
        if (Screen.hasControlDown()) {
            boolean shift = Screen.hasShiftDown();
            zoom = shift ? zoom + 5 : zoom + 1;
        } else {
            boolean shift = Screen.hasShiftDown();
            scroll = Math.max(shift ? scroll - 5 : scroll - 1, 0);
        }
    }

    private static void down() {
        if (Screen.hasControlDown()) {
            boolean shift = Screen.hasShiftDown();
            zoom = shift ? zoom - 5 : zoom - 1;
        } else {
            boolean shift = Screen.hasShiftDown();
            scroll = Math.max(shift ? scroll + 5 : scroll + 1, 0);
        }
    }

    public static void renderMainPage(GuiGraphics guiGraphics) {
        float scale = (zoom + 100) / 100f;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, scale);
        List<String> leftLines = collectLeftLines().stream().skip(scroll).toList();
        ((BuggerScreenRenderLinesInvoker) Minecraft.getInstance().getDebugOverlay()).drawLines(guiGraphics, leftLines, true);
        List<String> rightLines = collectRightLines().stream().skip(scroll).toList();
        ((BuggerScreenRenderLinesInvoker) Minecraft.getInstance().getDebugOverlay()).drawLines(guiGraphics, rightLines, false);
        guiGraphics.pose().popPose();
    }

    private static List<String> collectLeftLines() {
        List<String> lines = new ArrayList<>();

        return lines;
    }

    private static List<String> collectRightLines() {
        List<String> lines = new ArrayList<>();

        return lines;
    }

    private static ItemStack getItemInHand() {
        @Nullable LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return ItemStack.EMPTY;

        ItemStack mainHandItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        return mainHandItem.isEmpty() ? player.getItemInHand(InteractionHand.OFF_HAND) : mainHandItem;
    }

    public static List<String> splitString(String text, int size) {
        List<String> ret = new ArrayList<>((text.length() + size - 1) / size);

        for (int start = 0; start < text.length(); start += size) {
            ret.add(text.substring(start, Math.min(text.length(), start + size)));
        }
        return ret;
    }

    public static void renderTagPage(GuiGraphics guiGraphics) {
        ItemStack itemInHand = getItemInHand();

        JsonElement json = ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, itemInHand).result().orElse(new JsonObject());
        String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(json);

        jsonString = JsonSyntaxHighlighter.highlight(jsonString);

        List<String> lines = new ArrayList<>(Arrays.stream(jsonString.split("\n")).skip(scroll).toList());
        lines.addFirst("");
        lines.addFirst(itemInHand.getHoverName().getString());

        float scale = (zoom + 100) / 100f;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, scale);
        ((BuggerScreenRenderLinesInvoker) Minecrft.get().getDebugOverlay()).drawLines(guiGraphics, lines, true);
        guiGraphics.pose().popPose();
    }
}
