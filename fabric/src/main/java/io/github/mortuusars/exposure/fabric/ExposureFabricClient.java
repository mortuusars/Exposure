package io.github.mortuusars.exposure.fabric;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.gui.component.PhotographTooltip;
import io.github.mortuusars.exposure.gui.screen.LightroomScreen;
import io.github.mortuusars.exposure.gui.screen.album.AlbumScreen;
import io.github.mortuusars.exposure.gui.screen.album.LecternAlbumScreen;
import io.github.mortuusars.exposure.gui.screen.camera.CameraAttachmentsScreen;
import io.github.mortuusars.exposure.item.AlbumItem;
import io.github.mortuusars.exposure.item.CameraItemClientExtensions;
import io.github.mortuusars.exposure.item.StackedPhotographsItem;
import io.github.mortuusars.exposure.network.fabric.PacketsImpl;
import io.github.mortuusars.exposure.render.PhotographEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

public class ExposureFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Exposure.initClient();

        MenuScreens.register(Exposure.MenuTypes.CAMERA.get(), CameraAttachmentsScreen::new);
        MenuScreens.register(Exposure.MenuTypes.ALBUM.get(), AlbumScreen::new);
        MenuScreens.register(Exposure.MenuTypes.LECTERN_ALBUM.get(), LecternAlbumScreen::new);
        MenuScreens.register(Exposure.MenuTypes.LIGHTROOM.get(), LightroomScreen::new);

        ItemProperties.register(Exposure.Items.CAMERA.get(), new ResourceLocation("camera_state"), CameraItemClientExtensions::itemPropertyFunction);
        ItemProperties.register(Exposure.Items.STACKED_PHOTOGRAPHS.get(), new ResourceLocation("count"),
                (pStack, pLevel, pEntity, pSeed) -> {
                    if (pStack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem) {
                        return stackedPhotographsItem.getPhotographsCount(pStack) / 100f;
                    }
                    return 0f;
                });
        ItemProperties.register(Exposure.Items.ALBUM.get(), new ResourceLocation("photos"),
                (stack, clientLevel, livingEntity, seed) ->
                        stack.getItem() instanceof AlbumItem albumItem ? albumItem.getPhotographsCount(stack) / 100f : 0f);

        ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, out) ->
                out.accept(new ModelResourceLocation("exposure", "camera_gui", "inventory")));

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new ExposureFabricClientReloadListener());
        EntityRendererRegistry.register(Exposure.EntityTypes.PHOTOGRAPH.get(), PhotographEntityRenderer::new);
        TooltipComponentCallback.EVENT.register(data -> data instanceof PhotographTooltip photographTooltip ? photographTooltip : null);

        PacketsImpl.registerS2CPackets();
    }
}
