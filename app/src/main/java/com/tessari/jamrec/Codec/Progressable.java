package com.tessari.jamrec.Codec;

public interface Progressable {
    interface OnProgressChangedListener{
        void onPorgressChanged(float percentage);
    }

    void setOnProgressChangedListener(OnProgressChangedListener listener);
}
