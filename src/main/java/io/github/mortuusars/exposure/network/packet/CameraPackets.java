package io.github.mortuusars.exposure.network.packet;

public class CameraPackets {

//    public void toBuffer(FriendlyByteBuf friendlyByteBuf) {
//        friendlyByteBuf.writeUtf(id);
//    }
//
//    public static CameraPacket fromBuffer(FriendlyByteBuf buffer) {
//        return new CameraPacket(buffer.readUtf());
//    }
//
//    @SuppressWarnings("UnusedReturnValue")
//    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
//        NetworkEvent.Context context = contextSupplier.get();
//        @Nullable ServerPlayer player = context.getSender();
//
//        if (player == null)
//            throw new IllegalStateException("Cannot handle QueryExposureDataPacket: Player was null");
//
//        Optional<ExposureSavedData> exposureSavedData = new ServersideExposureStorage().getOrQuery(id);
//
//        if (exposureSavedData.isEmpty())
//            Exposure.LOGGER.error("Cannot get exposure data with an id '" + id + "'. Result is null.");
//        else {
//            ExposureSender.sendToClient(id, exposureSavedData.get(), player);
//        }
//
//        return true;
//    }
}
