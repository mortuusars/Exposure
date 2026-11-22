package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.network.packet.clientbound.CameraStandSetRotationsS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.ComponentTransferringRecipeDisplayResponseS2CP;
import io.github.mortuusars.exposure.network.packet.serverbound.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

public class C2SPackets {
    public static List<CustomPacketPayload.TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload>> getDefinitions() {
        return List.of(
                new CustomPacketPayload.TypeAndCodec<>(AlbumSignC2SP.TYPE, AlbumSignC2SP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(AlbumSyncNoteC2SP.TYPE, AlbumSyncNoteC2SP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(ActiveCameraSetSettingC2SP.TYPE, ActiveCameraSetSettingC2SP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(OpenCameraAttachmentsInCreativePacketC2SP.TYPE, OpenCameraAttachmentsInCreativePacketC2SP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(ExposureRequestC2SP.TYPE, ExposureRequestC2SP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(ActiveCameraReleaseC2SP.TYPE, ActiveCameraReleaseC2SP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(InterplanarProjectionFinishedC2SP.TYPE, InterplanarProjectionFinishedC2SP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(ExposureDataC2SP.TYPE, ExposureDataC2SP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(CameraStandTurnC2SP.TYPE, CameraStandTurnC2SP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(ComponentTransferringRecipeDisplayC2SP.TYPE, ComponentTransferringRecipeDisplayC2SP.STREAM_CODEC)
        );
    }
}
