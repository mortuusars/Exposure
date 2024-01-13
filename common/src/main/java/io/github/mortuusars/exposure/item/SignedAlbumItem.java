package io.github.mortuusars.exposure.item;

public class SignedAlbumItem extends AlbumItem {
    public SignedAlbumItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isEditable() {
        return false;
    }
}
