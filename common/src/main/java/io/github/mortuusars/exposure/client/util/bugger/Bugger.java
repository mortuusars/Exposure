package io.github.mortuusars.exposure.client.util.bugger;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.JsonOps;
import io.github.mortuusars.exposure.client.gui.screen.test.TestImageScreen;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.mixin.client.BuggerScreenRenderLinesInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.ProblemReporter;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Bugger {
    public static int page = -1;

    private static int zoom;
    private static int scroll;

    public static boolean onKeyPress(int key, int scanCode, int modifiers) {
        if (key == InputConstants.KEY_UP) up();
        if (key == InputConstants.KEY_DOWN) down();
        if (key == InputConstants.KEY_INSERT) zoom = 0;
        if (key == InputConstants.KEY_HOME) scroll = 0;
        if (key == InputConstants.KEY_LEFT) page = Mth.clamp(page - 1, -1, 1);
        if (key == InputConstants.KEY_RIGHT) page = Mth.clamp(page + 1, -1, 1);

        if (key == InputConstants.KEY_END) test();
        return false;
    }

    public static boolean onKeyRepeat(int key, int scanCode, int modifiers) {
        if (key == InputConstants.KEY_UP) up();
        if (key == InputConstants.KEY_DOWN) down();
        if (key == InputConstants.KEY_LEFT) page = Mth.clamp(page - 1, -1, 1);
        if (key == InputConstants.KEY_RIGHT) page = Mth.clamp(page + 1, -1, 1);
        return false;
    }

    public static boolean onKeyRelease(int key, int scanCode, int modifiers) {
        return false;
    }

    private static void test() {
        Minecrft.get().setScreen(new TestImageScreen());
    }

    // --

    private static void up() {
        if (Minecraft.getInstance().hasControlDown()) {
            boolean shift = Minecraft.getInstance().hasShiftDown();
            zoom = shift ? zoom + 5 : zoom + 1;
        } else {
            boolean shift = Minecraft.getInstance().hasShiftDown();
            scroll = Math.max(shift ? scroll - 5 : scroll - 1, 0);
        }
    }

    private static void down() {
        if (Minecraft.getInstance().hasControlDown()) {
            boolean shift = Minecraft.getInstance().hasShiftDown();
            zoom = shift ? zoom - 5 : zoom - 1;
        } else {
            boolean shift = Minecraft.getInstance().hasShiftDown();
            scroll = Math.max(shift ? scroll + 5 : scroll + 1, 0);
        }
    }

    public static void renderMainPage(GuiGraphics guiGraphics) {
        float scale = (zoom + 100) / 100f;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(scale, scale);
        List<String> leftLines = collectLeftLines().stream().skip(scroll).toList();
        ((BuggerScreenRenderLinesInvoker) Minecraft.getInstance().getDebugOverlay()).drawLines(guiGraphics, leftLines, true);
        List<String> rightLines = collectRightLines().stream().skip(scroll).toList();
        ((BuggerScreenRenderLinesInvoker) Minecraft.getInstance().getDebugOverlay()).drawLines(guiGraphics, rightLines, false);
        guiGraphics.pose().popMatrix();
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
        List<String> tagLines = getTagPageLines();

        int maxScroll = Math.max(tagLines.size() - 8, 0);
        scroll = Mth.clamp(scroll, 0, maxScroll);

        List<String> lines = tagLines.stream().skip(scroll).toList();

        float scale = (zoom + 100) / 100f;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(scale, scale);
        ((BuggerScreenRenderLinesInvoker) Minecrft.get().getDebugOverlay()).drawLines(guiGraphics, lines, true);
        guiGraphics.pose().popMatrix();
    }

    private static @NotNull List<String> getTagPageLines() {
        @Nullable HitResult hitResult = Minecrft.get().hitResult;
        if (hitResult == null || hitResult.getType() == HitResult.Type.MISS) {
            ItemStack itemInHand = getItemInHand();

            JsonElement json = ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, itemInHand).result().orElse(new JsonObject());
            String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(json);

            jsonString = JsonSyntaxHighlighter.highlight(jsonString);

            List<String> lines = new ArrayList<>(Arrays.stream(jsonString.split("\n")).toList());
            lines.addFirst("");
            lines.addFirst(itemInHand.getHoverName().getString());
            return lines;
        } else if (hitResult instanceof BlockHitResult blockHitResult) {
            BlockPos blockPos = blockHitResult.getBlockPos();
            @Nullable BlockEntity blockEntity = Minecrft.level().getBlockEntity(blockPos);
            if (blockEntity != null) {
                CompoundTag beTag = blockEntity.saveWithFullMetadata(Minecrft.level().registryAccess());
                JsonElement json = CompoundTag.CODEC.encodeStart(JsonOps.INSTANCE, beTag).result().orElse(new JsonObject());

                String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(json);

                jsonString = JsonSyntaxHighlighter.highlight(jsonString);

                List<String> lines = new ArrayList<>(Arrays.stream(jsonString.split("\n")).toList());
                lines.addFirst("");
                lines.addFirst(blockEntity.getBlockState().getBlock().getName().getString());
                return lines;
            } else {
                return List.of(Minecrft.level().getBlockState(blockPos).getBlock().getName().getString());
            }
        } else if (hitResult instanceof EntityHitResult entityHitResult) {
            Entity entity = entityHitResult.getEntity();

            TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
            entity.save(output);
            CompoundTag entityTag = output.buildResult();

            JsonElement json = CompoundTag.CODEC.encodeStart(JsonOps.INSTANCE, entityTag).result().orElse(new JsonObject());

            String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(json);

            jsonString = JsonSyntaxHighlighter.highlight(jsonString);

            List<String> lines = new ArrayList<>(Arrays.stream(jsonString.split("\n")).toList());
            lines.addFirst("");
            lines.addFirst(entity.getName().getString());
            return lines;
        }

        return Collections.emptyList();
    }
}
