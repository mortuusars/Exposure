package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.TrichromeImage;
import io.github.mortuusars.exposure.client.task.ExposureRetrieveTask;
import io.github.mortuusars.exposure.client.capture.palettizer.Palettizer;
import io.github.mortuusars.exposure.client.capture.saving.ExposureUploader;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.util.UnixTimestamp;
import io.github.mortuusars.exposure.util.cycles.task.Result;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.mortaar.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;

public record ClientboundCreateChromaticExposurePacket(String id, List<ExposureIdentifier> layers) implements Packet {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation ID = Exposure.resource("create_chromatic_exposure");
    public static final CustomPacketPayload.Type<ClientboundCreateChromaticExposurePacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ClientboundCreateChromaticExposurePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ClientboundCreateChromaticExposurePacket::id,
            ExposureIdentifier.STREAM_CODEC.apply(ByteBufCodecs.list()), ClientboundCreateChromaticExposurePacket::layers,
            ClientboundCreateChromaticExposurePacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        Client.createChromaticExposure(id, layers);
        return true;
    }

    public static class Client {
        public static void createChromaticExposure(String id, List<ExposureIdentifier> layers) {
            if (id.isEmpty()) {
                LOGGER.error("Cannot create chromatic exposure: identifier is empty.");
                return;
            }
            if (layers.size() != 3) {
                LOGGER.error("Cannot create chromatic exposure: 3 layers required. Provided: '{}'.", layers.size());
                return;
            }
            var colorPalette = ColorPalettes.getDefault(Minecrft.registryAccess());
            ColorPalette palette = colorPalette.value();
            ResourceLocation paletteId = colorPalette.unwrapKey().orElseThrow().location();
            ExposureClient.cycles().addParallelTask(new ExposureRetrieveTask(layers, 20_000)
                    .then(Result::unwrap)
                    .thenAsync(images -> (Image) new TrichromeImage(images.get(0), images.get(1), images.get(2)))
                    .thenAsync(Palettizer.DITHERED.palettizeAndClose(palette))
                    .thenAsync(img -> new ExposureData(img.width(), img.height(), img.pixels(), paletteId,
                            new ExposureData.Tag(ExposureType.COLOR, Minecrft.playerOrThrow().getScoreboardName(),
                                    UnixTimestamp.Seconds.now(), false, false)))
                    .acceptAsync(ExposureUploader.upload(id)));
        }
    }
}
