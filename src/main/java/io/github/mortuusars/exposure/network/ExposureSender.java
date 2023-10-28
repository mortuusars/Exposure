package io.github.mortuusars.exposure.network;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.network.packet.ExposureDataPartPacket;
import io.github.mortuusars.exposure.storage.ExposureSavedData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import org.jetbrains.annotations.Nullable;

public class ExposureSender {
    private static final int PART_SIZE = 30_000;
    public static void sendToServer(String id, ExposureSavedData exposureData) {
        send(id, exposureData, NetworkDirection.PLAY_TO_SERVER, null);
    }

    public static void sendToClient(String id, ExposureSavedData exposureData, ServerPlayer player) {
        send(id, exposureData, NetworkDirection.PLAY_TO_CLIENT, player);
    }

    private static void send(String id, ExposureSavedData exposureData, NetworkDirection direction, @Nullable ServerPlayer player) {
        byte[] bytes = exposureData.getPixels();
        byte[][] parts = splitToParts(bytes, PART_SIZE);

        int offset = 0;

        for (byte[] part : parts) {
            ExposureDataPartPacket packet = new ExposureDataPartPacket(id,
                    exposureData.getWidth(), exposureData.getHeight(), exposureData.getType(), offset, part);

            if (direction == NetworkDirection.PLAY_TO_SERVER)
                Packets.sendToServer(packet);
            else {
                Preconditions.checkArgument(player != null, "Cannot send packet to null player.");
                Packets.sendToClient(packet, player);
            }

            offset += PART_SIZE;
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static byte[][] splitToParts(byte[] input, int size) {
        int parts = (int)Math.ceil(input.length / (double)size);
        byte[][] output = new byte[parts][];

        for(int part = 0; part < parts; part++) {
            int start = part * size;
            int length = Math.min(input.length - start, size);

            byte[] bytes = new byte[length];
            System.arraycopy(input, start, bytes, 0, length);
            output[part] = bytes;
        }

        return output;
    }
}
