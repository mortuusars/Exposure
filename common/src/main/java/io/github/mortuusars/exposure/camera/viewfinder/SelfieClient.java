package io.github.mortuusars.exposure.camera.viewfinder;

import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.server.CameraSelfieModeC2SP;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;

public class SelfieClient {
    public static void update(ItemAndStack<CameraItem> camera, InteractionHand activeHand, boolean effects) {
        if (Minecraft.getInstance().player == null)
            return;

        boolean selfieMode = Minecraft.getInstance().options.getCameraType() == CameraType.THIRD_PERSON_FRONT;

        if (effects)
            camera.getItem().setSelfieModeWithEffects(Minecraft.getInstance().player, camera.getStack(), selfieMode);
        else
            camera.getItem().setSelfieMode(camera.getStack(), selfieMode);

        Packets.sendToServer(new CameraSelfieModeC2SP(activeHand, selfieMode, effects));
    }
}
