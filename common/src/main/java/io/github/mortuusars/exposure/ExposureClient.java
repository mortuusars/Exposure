package io.github.mortuusars.exposure;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderClient;
import io.github.mortuusars.exposure.data.storage.ClientsideExposureStorage;
import io.github.mortuusars.exposure.data.storage.IExposureStorage;
import io.github.mortuusars.exposure.data.transfer.ExposureReceiver;
import io.github.mortuusars.exposure.data.transfer.ExposureSender;
import io.github.mortuusars.exposure.data.transfer.IExposureReceiver;
import io.github.mortuusars.exposure.data.transfer.IExposureSender;
import io.github.mortuusars.exposure.gui.screen.camera.ViewfinderControlsScreen;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.render.ExposureRenderer;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class ExposureClient {
    private static final IExposureStorage exposureStorage = new ClientsideExposureStorage();
    private static final ExposureRenderer exposureRenderer = new ExposureRenderer();

    private static IExposureSender exposureSender;
    private static IExposureReceiver exposureReceiver;

    @Nullable
    private static KeyMapping openViewfinderControlsKey = null;

    public static void init() {
        exposureSender = new ExposureSender((packet, player) -> Packets.sendToServer(packet));
        exposureReceiver = new ExposureReceiver(exposureStorage);
    }

    public static IExposureStorage getExposureStorage() {
        return exposureStorage;
    }

    public static IExposureSender getExposureSender() {
        return exposureSender;
    }

    public static IExposureReceiver getExposureReceiver() {
        return exposureReceiver;
    }

    public static ExposureRenderer getExposureRenderer() {
        return exposureRenderer;
    }

    public static void registerKeymappings(Function<KeyMapping, KeyMapping> registerFunction) {
        KeyMapping keyMapping = new KeyMapping("key.exposure.camera_controls",
                InputConstants.UNKNOWN.getValue(), "category.exposure");

        openViewfinderControlsKey = registerFunction.apply(keyMapping);
    }

    public static void onScreenAdded(Screen screen) {
        if (ViewfinderClient.isOpen() && !(screen instanceof ViewfinderControlsScreen)) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null)
                CameraInHand.deactivate(player);
        }
    }

    public static KeyMapping getViewfinderControlsKey() {
        Preconditions.checkState(openViewfinderControlsKey != null,
                "Viewfinder Controls key mapping was not registered");

        return openViewfinderControlsKey.isUnbound() ? Minecraft.getInstance().options.keyShift : openViewfinderControlsKey;
    }
}
