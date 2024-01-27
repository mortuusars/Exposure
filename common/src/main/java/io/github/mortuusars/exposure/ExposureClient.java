package io.github.mortuusars.exposure;

import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderClient;
import io.github.mortuusars.exposure.gui.screen.camera.ViewfinderControlsScreen;
import io.github.mortuusars.exposure.item.AlbumItem;
import io.github.mortuusars.exposure.render.ExposureRenderer;
import io.github.mortuusars.exposure.data.storage.ClientsideExposureStorage;
import io.github.mortuusars.exposure.data.storage.IExposureStorage;
import io.github.mortuusars.exposure.data.transfer.ExposureReceiver;
import io.github.mortuusars.exposure.data.transfer.ExposureSender;
import io.github.mortuusars.exposure.data.transfer.IExposureReceiver;
import io.github.mortuusars.exposure.data.transfer.IExposureSender;
import io.github.mortuusars.exposure.item.CameraItemClientExtensions;
import io.github.mortuusars.exposure.item.StackedPhotographsItem;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

public class ExposureClient {
    private static final IExposureStorage exposureStorage = new ClientsideExposureStorage();
    private static final ExposureRenderer exposureRenderer = new ExposureRenderer();

    private static IExposureSender exposureSender;
    private static IExposureReceiver exposureReceiver;

    public static void init() {
        exposureSender = new ExposureSender((packet, player) -> Packets.sendToServer(packet));
        exposureReceiver = new ExposureReceiver(exposureStorage);

        ItemProperties.register(Exposure.Items.CAMERA.get(), new ResourceLocation("camera_state"), CameraItemClientExtensions::itemPropertyFunction);
        ItemProperties.register(Exposure.Items.STACKED_PHOTOGRAPHS.get(), new ResourceLocation("count"),
                (stack, clientLevel, livingEntity, seed) ->
                        stack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem ?
                                stackedPhotographsItem.getPhotographsCount(stack) / 100f : 0f);
        ItemProperties.register(Exposure.Items.ALBUM.get(), new ResourceLocation("photos"),
                (stack, clientLevel, livingEntity, seed) ->
                        stack.getItem() instanceof AlbumItem albumItem ? albumItem.getPhotographsCount(stack) / 100f : 0f);
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

    public static void onScreenAdded(Screen screen) {
        if (ViewfinderClient.isOpen() && !(screen instanceof ViewfinderControlsScreen)) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null)
                CameraInHand.deactivate(player);
        }
    }
}
