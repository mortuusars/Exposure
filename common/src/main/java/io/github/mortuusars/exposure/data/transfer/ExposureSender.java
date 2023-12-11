package io.github.mortuusars.exposure.data.transfer;

import io.github.mortuusars.exposure.data.storage.ExposureSavedData;
import io.github.mortuusars.exposure.network.packet.ExposureDataPartPacket;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class ExposureSender implements IExposureSender {
    private static final int PART_SIZE = 30_000;

    private final BiConsumer<ExposureDataPartPacket, @Nullable Player> packetSender;

    public ExposureSender(BiConsumer<ExposureDataPartPacket, @Nullable Player> packetSender) {
        this.packetSender = packetSender;
    }

    public void send(String id, ExposureSavedData exposureData) {
        sendTo(null, id, exposureData);
    }

    @Override
    public void sendTo(@Nullable Player player, String id, ExposureSavedData exposureData) {
        byte[][] parts = splitToParts(exposureData.getPixels(), PART_SIZE);
        int offset = 0;

        for (byte[] part : parts) {
            ExposureDataPartPacket packet = new ExposureDataPartPacket(id,
                    exposureData.getWidth(), exposureData.getHeight(), exposureData.getProperties(), offset, part);

            packetSender.accept(packet, player);

            offset += PART_SIZE;
        }
    }

    @SuppressWarnings("SameParameterValue")
    private byte[][] splitToParts(byte[] input, int partSize) {
        int parts = (int)Math.ceil(input.length / (double)partSize);
        byte[][] output = new byte[parts][];

        for(int part = 0; part < parts; part++) {
            int start = part * partSize;
            int length = Math.min(input.length - start, partSize);

            byte[] bytes = new byte[length];
            System.arraycopy(input, start, bytes, 0, length);
            output[part] = bytes;
        }

        return output;
    }
}
