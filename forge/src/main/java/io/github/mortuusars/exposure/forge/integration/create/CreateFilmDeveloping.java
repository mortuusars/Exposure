package io.github.mortuusars.exposure.forge.integration.create;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.camera.infrastructure.FilmType;
import io.github.mortuusars.exposure.item.FilmRollItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateFilmDeveloping {
    public static final String CURRENT_STEP_TAG = "CurrentDevelopingStep";
    private static final Map<FilmType, List<FluidStack>> cache = new HashMap<>();

    public static List<FluidStack> getFillingSteps(FilmType filmType) {
        if (cache.containsKey(filmType))
            return cache.get(filmType);

        List<String> steps = filmType == FilmType.COLOR ? Config.Common.CREATE_SPOUT_DEVELOPING_STEPS_COLOR.get()
                : Config.Common.CREATE_SPOUT_DEVELOPING_STEPS_BW.get();

        List<FluidStack> fluidStacks = loadStacks(steps);

        if (!fluidStacks.isEmpty()) {
            cache.put(filmType, fluidStacks);
            return fluidStacks;
        } else {
            LogUtils.getLogger().warn("Create Film Developing should have at least one step. Defaults will be loaded.");

            List<String> defaultSteps = filmType == FilmType.COLOR ? Config.Common.CREATE_SPOUT_DEVELOPING_STEPS_COLOR.getDefault()
                    : Config.Common.CREATE_SPOUT_DEVELOPING_STEPS_BW.getDefault();

            List<FluidStack> defaultFluidStacks = loadStacks(defaultSteps);

            if (defaultFluidStacks.isEmpty())
                throw new IllegalStateException("Failed to load default fluid stacks. Something isn't right.");

            cache.put(filmType, defaultFluidStacks);
            return defaultFluidStacks;
        }
    }

    public static @Nullable FluidStack getNextRequiredFluid(ItemStack stack) {
        if (!(stack.getItem() instanceof FilmRollItem filmRollItem))
            throw new IllegalArgumentException("Filling to develop film can only be used on FilmRollItem. Got: " + stack);

        List<FluidStack> fillingSteps = getFillingSteps(filmRollItem.getType());

        CompoundTag tag = stack.getTag();
        if (tag == null || tag.isEmpty())
            return fillingSteps.get(0);

        int nextStep = tag.getInt(CURRENT_STEP_TAG) + 1;
        if (nextStep > fillingSteps.size())
            return null;

        return fillingSteps.get(Math.max(1, nextStep) - 1);
    }

    public static ItemStack fillFilmStack(ItemStack stack, int requiredAmount, FluidStack availableFluid) {
        if (!(stack.getItem() instanceof FilmRollItem filmRollItem))
            throw new IllegalArgumentException("Filling to develop film can only be used on FilmRollItem. Got: " + stack);

        @Nullable FluidStack requiredFluid = getNextRequiredFluid(stack);
        if (requiredFluid == null)
            throw new IllegalStateException("Cannot fill if fluid is not required anymore. This should have been handled in previous step.");

        FilmType filmType = filmRollItem.getType();
        List<FluidStack> fillingSteps = getFillingSteps(filmType);

        int nextStep = getNextStep(stack);

        ItemStack result;

        if (requiredAmount == 0 || nextStep == fillingSteps.size()) {
            result = filmType.createDevelopedItemStack();

            if (stack.getTag() != null)
                result.setTag(stack.getOrCreateTag().copy());

            result.getOrCreateTag().remove(CURRENT_STEP_TAG);
        }
        else {
            result = filmType.createItemStack();

            if (stack.getTag() != null)
                result.setTag(stack.getOrCreateTag().copy());

            result.getOrCreateTag().putInt(CURRENT_STEP_TAG, nextStep);
        }

        availableFluid.shrink(requiredAmount);
        stack.shrink(1);
        return result;
    }

    public static int getNextStep(ItemStack stack) {
        return stack.getTag() != null ? stack.getTag().getInt(CURRENT_STEP_TAG) + 1 : 1;
    }

    public static void clearCachedData() {
        cache.clear();
    }

    private static List<FluidStack> loadStacks(List<String> strings) {
        List<FluidStack> stacks = new ArrayList<>();

        for (String step : strings) {
            @Nullable FluidStack fluidStack = getFluidStack(step);
            if (fluidStack != null)
                stacks.add(fluidStack);
        }

        return stacks;
    }

    private static @Nullable FluidStack getFluidStack(String serializedString) {
        try {
            CompoundTag tag = TagParser.parseTag(serializedString);
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(tag);
            if (!fluidStack.isEmpty())
                return fluidStack;
            else
                LogUtils.getLogger().warn("FluidStack [" + serializedString + "] was loaded empty.");
        } catch (CommandSyntaxException e) {
            LogUtils.getLogger().error("[" + serializedString + "] failed to load: " + e);
        }

        return null;
    }
}
