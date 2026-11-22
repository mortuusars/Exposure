package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ShowExposureCommandS2CP(List<Frame> frames,
                                      boolean negative) implements Packet {
    public static final Identifier ID = Exposure.resource("show_exposure_command");
    public static final CustomPacketPayload.Type<ShowExposureCommandS2CP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ShowExposureCommandS2CP> STREAM_CODEC = StreamCodec.composite(
            Frame.STREAM_CODEC.apply(ByteBufCodecs.list()), ShowExposureCommandS2CP::frames,
            ByteBufCodecs.BOOL, ShowExposureCommandS2CP::negative,
            ShowExposureCommandS2CP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static ShowExposureCommandS2CP identifier(ExposureIdentifier identifier, boolean negative) {
        Frame frame = Frame.create().setIdentifier(identifier).toImmutable();
        return new ShowExposureCommandS2CP(List.of(frame), negative);
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ClientPacketsHandler.showExposure(this);
        return true;
    }
}
